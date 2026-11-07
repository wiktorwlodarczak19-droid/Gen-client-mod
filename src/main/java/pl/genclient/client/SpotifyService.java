package pl.genclient.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Spotify Connect bridge. Audio continues to play in Spotify; GEN CLIENT reads metadata
 * and sends playback commands through Spotify's official Web API.
 */
public final class SpotifyService {
    private static final String API = "https://api.spotify.com/v1";
    private static final String TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token";
    private static final String AUTHORIZE_ENDPOINT = "https://accounts.spotify.com/authorize";
    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("genclient-spotify.json");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private static final ScheduledExecutorService WORKER = Executors.newScheduledThreadPool(2, task -> {
        Thread thread = new Thread(task, "GEN-CLIENT-Spotify");
        thread.setDaemon(true);
        return thread;
    });
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Object TOKEN_LOCK = new Object();

    private static volatile String clientId = "";
    private static volatile String refreshToken = "";
    private static volatile String accessToken = "";
    private static volatile long accessTokenExpiresAt;
    private static volatile String status = "Spotify niepołączone";
    private static volatile TrackSnapshot snapshot = TrackSnapshot.empty();
    private static volatile ServerSocket callbackSocket;
    private static volatile boolean initialized;

    private SpotifyService() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        load();
        WORKER.scheduleWithFixedDelay(SpotifyService::pollSafely, 2, 4, TimeUnit.SECONDS);
    }

    public static String getClientId() {
        return clientId;
    }

    public static String getStatus() {
        return status;
    }

    public static boolean isConnected() {
        return !refreshToken.isBlank();
    }

    public static TrackSnapshot getSnapshot() {
        return snapshot;
    }

    public static void setClientId(String value) {
        clientId = value == null ? "" : value.trim();
        save();
        status = clientId.isBlank() ? "Wpisz Spotify Client ID" : "Client ID zapisany";
    }

    /** Starts OAuth Authorization Code with PKCE and opens the Spotify consent page. */
    public static synchronized void connect() {
        if (clientId.isBlank()) {
            status = "Najpierw wpisz i zapisz Spotify Client ID";
            return;
        }

        closeCallbackServer();
        try {
            String verifier = randomUrlSafe(64);
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
            String state = randomUrlSafe(24);

            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8888));
            server.setSoTimeout(180_000);
            callbackSocket = server;

            String redirectUri = "http://127.0.0.1:8888";
            String scope = "user-read-playback-state user-modify-playback-state";
            String authorizeUrl = AUTHORIZE_ENDPOINT
                    + "?client_id=" + enc(clientId)
                    + "&response_type=code"
                    + "&redirect_uri=" + enc(redirectUri)
                    + "&state=" + enc(state)
                    + "&scope=" + enc(scope)
                    + "&code_challenge_method=S256"
                    + "&code_challenge=" + enc(challenge);

            status = "Oczekiwanie na logowanie Spotify…";
            Thread callback = new Thread(() -> awaitAuthorization(server, redirectUri, verifier, state),
                    "GEN-CLIENT-Spotify-OAuth");
            callback.setDaemon(true);
            callback.start();

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(authorizeUrl));
                } else {
                    status = "Nie można automatycznie otworzyć przeglądarki";
                }
            } catch (Exception browserError) {
                status = "Otwórz ręcznie stronę logowania Spotify";
                System.err.println("[GEN CLIENT] Could not open Spotify authorization page: "
                        + browserError.getMessage());
            }
        } catch (java.net.BindException exception) {
            status = "Port 8888 jest zajęty — zamknij proces, który go używa, i spróbuj ponownie";
            System.err.println("[GEN CLIENT] Spotify OAuth callback port 8888 is unavailable: "
                    + exception.getMessage());
            closeCallbackServer();
        } catch (Exception exception) {
            status = "Nie udało się uruchomić logowania Spotify";
            System.err.println("[GEN CLIENT] Spotify OAuth startup failed: " + exception.getMessage());
            closeCallbackServer();
        }
    }

    public static void disconnect() {
        synchronized (TOKEN_LOCK) {
            accessToken = "";
            refreshToken = "";
            accessTokenExpiresAt = 0L;
        }
        snapshot = TrackSnapshot.empty();
        status = "Spotify rozłączone";
        save();
    }

    public static void togglePlayback() {
        WORKER.execute(() -> {
            TrackSnapshot current = snapshot;
            sendCommand("PUT", current.isPlaying() ? "/me/player/pause" : "/me/player/play");
        });
    }

    public static void next() {
        WORKER.execute(() -> sendCommand("POST", "/me/player/next"));
    }

    public static void previous() {
        WORKER.execute(() -> sendCommand("POST", "/me/player/previous"));
    }

    public static void shutdown() {
        closeCallbackServer();
        WORKER.shutdownNow();
    }

    private static void pollSafely() {
        if (!isConnected()) return;
        try {
            HttpResponse<String> response = apiRequest("GET", "/me/player");
            if (response.statusCode() == 204 || response.body() == null || response.body().isBlank()) {
                snapshot = TrackSnapshot.empty();
                status = "Otwórz Spotify i rozpocznij odtwarzanie";
                return;
            }
            if (response.statusCode() != 200) {
                status = apiError(response.statusCode(), response.body());
                return;
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject item = root.has("item") && root.get("item").isJsonObject()
                    ? root.getAsJsonObject("item") : null;
            if (item == null || !item.has("name")) {
                snapshot = TrackSnapshot.empty();
                status = "Brak aktualnego utworu";
                return;
            }

            String title = item.get("name").getAsString();
            String artists = readArtists(item);
            int progress = getInt(root, "progress_ms");
            int duration = getInt(item, "duration_ms");
            boolean playing = root.has("is_playing") && root.get("is_playing").getAsBoolean();
            snapshot = new TrackSnapshot(title, artists, playing, progress, duration, true,
                    System.currentTimeMillis());
            status = playing ? "Spotify odtwarza" : "Spotify wstrzymane";
        } catch (Exception exception) {
            status = "Błąd połączenia ze Spotify";
            System.err.println("[GEN CLIENT] Spotify status update failed: " + exception.getMessage());
        }
    }

    private static void sendCommand(String method, String endpoint) {
        if (!isConnected()) {
            status = "Połącz konto Spotify w Style → Spotify";
            return;
        }
        try {
            HttpResponse<String> response = apiRequest(method, endpoint);
            if (response.statusCode() == 204 || response.statusCode() == 202) {
                status = "Polecenie wysłane do Spotify";
                WORKER.schedule(SpotifyService::pollSafely, 450, TimeUnit.MILLISECONDS);
            } else {
                status = apiError(response.statusCode(), response.body());
            }
        } catch (Exception exception) {
            status = "Nie udało się sterować Spotify";
            System.err.println("[GEN CLIENT] Spotify playback command failed: " + exception.getMessage());
        }
    }

    private static HttpResponse<String> apiRequest(String method, String endpoint) throws Exception {
        String token = validAccessToken();
        HttpResponse<String> response = sendApiRequest(method, endpoint, token);
        if (response.statusCode() == 401) {
            refreshAccessToken();
            response = sendApiRequest(method, endpoint, validAccessToken());
        }
        return response;
    }

    private static HttpResponse<String> sendApiRequest(String method, String endpoint, String token)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token);
        switch (method) {
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.noBody());
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.noBody());
            default -> builder.GET();
        }
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String validAccessToken() throws Exception {
        synchronized (TOKEN_LOCK) {
            if (!accessToken.isBlank() && System.currentTimeMillis() < accessTokenExpiresAt - 45_000L) {
                return accessToken;
            }
        }
        refreshAccessToken();
        synchronized (TOKEN_LOCK) {
            if (accessToken.isBlank()) throw new IOException("Spotify access token unavailable");
            return accessToken;
        }
    }

    private static void refreshAccessToken() throws Exception {
        String savedRefresh;
        synchronized (TOKEN_LOCK) {
            savedRefresh = refreshToken;
        }
        if (savedRefresh.isBlank()) throw new IOException("Spotify account is not connected");

        String body = "grant_type=refresh_token"
                + "&refresh_token=" + enc(savedRefresh)
                + "&client_id=" + enc(clientId);
        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Spotify token refresh returned HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        synchronized (TOKEN_LOCK) {
            accessToken = json.get("access_token").getAsString();
            accessTokenExpiresAt = System.currentTimeMillis()
                    + json.get("expires_in").getAsLong() * 1000L;
            if (json.has("refresh_token") && !json.get("refresh_token").isJsonNull()) {
                refreshToken = json.get("refresh_token").getAsString();
            }
        }
        save();
    }

    private static void awaitAuthorization(ServerSocket server, String redirectUri,
                                           String verifier, String expectedState) {
        try (Socket socket = server.accept()) {
            socket.setSoTimeout(15_000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Consume request headers before writing the callback response.
            }

            String target = "/";
            if (requestLine != null) {
                String[] parts = requestLine.split(" ");
                if (parts.length >= 2) target = parts[1];
            }
            URI requestUri = URI.create("http://127.0.0.1" + target);
            Map<String, String> query = parseQuery(requestUri.getRawQuery());
            String returnedState = query.getOrDefault("state", "");
            String code = query.get("code");
            String error = query.get("error");

            if (!MessageDigest.isEqual(expectedState.getBytes(StandardCharsets.UTF_8),
                    returnedState.getBytes(StandardCharsets.UTF_8))) {
                writeCallback(socket.getOutputStream(), "Logowanie odrzucone",
                        "Nieprawidłowy parametr bezpieczeństwa. Możesz zamknąć tę kartę.");
                status = "Logowanie Spotify odrzucone (state mismatch)";
                return;
            }
            if (error != null || code == null || code.isBlank()) {
                writeCallback(socket.getOutputStream(), "Logowanie anulowane",
                        "Spotify nie udzieliło dostępu. Wróć do Minecrafta i spróbuj ponownie.");
                status = "Logowanie Spotify anulowane";
                return;
            }

            exchangeAuthorizationCode(code, redirectUri, verifier);
            writeCallback(socket.getOutputStream(), "Spotify połączone",
                    "Konto zostało połączone z GEN CLIENT. Możesz wrócić do Minecrafta.");
            status = "Spotify połączone";
            pollSafely();
        } catch (Exception exception) {
            status = "Logowanie Spotify nie powiodło się";
            System.err.println("[GEN CLIENT] Spotify callback failed: " + exception.getMessage());
        } finally {
            if (callbackSocket == server) callbackSocket = null;
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void exchangeAuthorizationCode(String code, String redirectUri, String verifier)
            throws IOException, InterruptedException {
        String body = "grant_type=authorization_code"
                + "&code=" + enc(code)
                + "&redirect_uri=" + enc(redirectUri)
                + "&client_id=" + enc(clientId)
                + "&code_verifier=" + enc(verifier);
        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Spotify authorization returned HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        synchronized (TOKEN_LOCK) {
            accessToken = json.get("access_token").getAsString();
            refreshToken = json.get("refresh_token").getAsString();
            accessTokenExpiresAt = System.currentTimeMillis()
                    + json.get("expires_in").getAsLong() * 1000L;
        }
        save();
    }

    private static void writeCallback(OutputStream output, String heading, String message) throws IOException {
        String html = "<!doctype html><html><head><meta charset='utf-8'><title>GEN CLIENT</title>"
                + "<style>body{background:#090b11;color:#f4f4f7;font:16px Segoe UI,Arial;display:grid;"
                + "place-items:center;height:90vh}.card{background:#151923;border:1px solid #414654;"
                + "border-radius:18px;padding:34px 42px;max-width:520px}h1{font-size:24px}p{color:#aeb5c4}</style>"
                + "</head><body><div class='card'><h1>" + htmlEscape(heading) + "</h1><p>"
                + htmlEscape(message) + "</p></div></body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + bytes.length + "\r\nConnection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        output.write(bytes);
        output.flush();
    }

    private static String htmlEscape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> values = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return values;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private static String readArtists(JsonObject item) {
        JsonElement artistsElement = item.get("artists");
        if (artistsElement == null || !artistsElement.isJsonArray()) {
            return item.has("show") && item.get("show").isJsonObject()
                    ? item.getAsJsonObject("show").get("name").getAsString() : "Spotify";
        }
        JsonArray artists = artistsElement.getAsJsonArray();
        StringBuilder result = new StringBuilder();
        for (JsonElement artist : artists) {
            if (!artist.isJsonObject() || !artist.getAsJsonObject().has("name")) continue;
            if (!result.isEmpty()) result.append(", ");
            result.append(artist.getAsJsonObject().get("name").getAsString());
        }
        return result.isEmpty() ? "Spotify" : result.toString();
    }

    private static int getInt(JsonObject object, String key) {
        try {
            return object.has(key) && !object.get(key).isJsonNull()
                    ? Math.max(0, object.get(key).getAsInt()) : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String apiError(int code, String body) {
        if (code == 401) return "Sesja Spotify wygasła — połącz konto ponownie";
        if (code == 403) return "Spotify odrzuciło polecenie — sprawdź Premium i uprawnienia aplikacji";
        if (code == 404) return "Brak aktywnego urządzenia Spotify";
        if (code == 429) return "Spotify ograniczyło żądania — ponawiam za chwilę";
        return "Spotify API zwróciło błąd HTTP " + code;
    }

    private static String randomUrlSafe(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static synchronized void closeCallbackServer() {
        ServerSocket server = callbackSocket;
        callbackSocket = null;
        if (server != null) {
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void load() {
        if (!Files.isRegularFile(CONFIG_FILE)) return;
        try {
            JsonObject json = JsonParser.parseString(Files.readString(CONFIG_FILE)).getAsJsonObject();
            clientId = json.has("clientId") ? json.get("clientId").getAsString() : "";
            refreshToken = json.has("refreshToken") ? json.get("refreshToken").getAsString() : "";
        } catch (Exception exception) {
            System.err.println("[GEN CLIENT] Spotify settings could not be read: " + exception.getMessage());
        }
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("clientId", clientId);
            synchronized (TOKEN_LOCK) {
                json.addProperty("refreshToken", refreshToken);
            }
            Files.writeString(CONFIG_FILE, json.toString());
        } catch (IOException exception) {
            System.err.println("[GEN CLIENT] Spotify settings could not be saved: " + exception.getMessage());
        }
    }

    public record TrackSnapshot(String title, String artists, boolean isPlaying,
                                int progressMs, int durationMs, boolean hasTrack, long receivedAt) {
        public static TrackSnapshot empty() {
            return new TrackSnapshot("", "", false, 0, 0, false, System.currentTimeMillis());
        }

        public int currentProgressMs() {
            if (!isPlaying) return progressMs;
            return Math.min(durationMs, progressMs + (int) Math.max(0L, System.currentTimeMillis() - receivedAt));
        }

        public String progressLabel() {
            return formatTime(currentProgressMs()) + " / " + formatTime(durationMs);
        }

        private static String formatTime(int millis) {
            int seconds = Math.max(0, millis / 1000);
            return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
        }
    }
}
