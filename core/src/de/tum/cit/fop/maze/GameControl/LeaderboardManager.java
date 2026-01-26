package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Manages leaderboard data, including local saving and cloud upload/fetch.
 */
public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.json";
    private static final int MAX_SCORES = 10;

    private static final String CLOUD_URL = "https://leaderboard-backup.vercel.app/api/leaderboard";

    /**
     * Represents a single score entry in the leaderboard.
     */
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int score;

        /**
         * Default constructor for JSON serialization.
         */
        public ScoreEntry() {
        }

        /**
         * Creates a new score entry.
         * @param name Player name.
         * @param score Player score.
         */
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        /**
         * Compares this score to another for sorting (descending order).
         * @param other The other score entry.
         * @return Comparison result.
         */
        @Override
        public int compareTo(ScoreEntry other) {
            return other.score - this.score;
        }
    }

    public interface LeaderboardCallback {
        void onScoresLoaded(ArrayList<ScoreEntry> scores);

        void onError(String message);
    }

    /**
     * Saves a score locally and attempts to upload it.
     * 
     * @param name      Player name.
     * @param score     Score value.
     * @param callbacks Callback on success.
     */
    public static void saveScore(String name, int score, Runnable callbacks) {
        ArrayList<ScoreEntry> scores = loadScores();
        scores.add(new ScoreEntry(name, score));
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }
        Json json = new Json();
        FileHandle file = Gdx.files.local(FILE_NAME);
        try {
            file.writeString(json.toJson(scores), false);
        } catch (Exception e) {
            Gdx.app.error("Leaderboard", "Local Save Failed", e);
        }

        uploadScoreToCloud(name, score, callbacks);
    }

    /**
     * Saves a score locally.
     * 
     * @param name  Player name.
     * @param score Score value.
     */
    public static void saveScore(String name, int score) {
        saveScore(name, score, null);
    }

    public static void uploadScoreToCloud(String name, int score, Runnable onSuccess) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE")) {
            if (onSuccess != null)
                Gdx.app.postRunnable(onSuccess);
            return;
        }

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(
                com.badlogic.gdx.Net.HttpMethods.POST);
        request.setUrl(CLOUD_URL);
        request.setHeader("Content-Type", "application/json");

        String content = "{\"name\":\"" + name + "\", \"score\":" + score + "}";
        request.setContent(content);

        Gdx.net.sendHttpRequest(request, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String result = httpResponse.getResultAsString();
                Gdx.app.log("Leaderboard", "Upload Response Code: " + statusCode);
                Gdx.app.log("Leaderboard", "Upload Response Body: " + result);

                if (onSuccess != null)
                    Gdx.app.postRunnable(onSuccess);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log("Leaderboard", "Upload Failed: " + t.getMessage());
                if (onSuccess != null)
                    Gdx.app.postRunnable(onSuccess);
            }

            @Override
            public void cancelled() {
                if (onSuccess != null)
                    Gdx.app.postRunnable(onSuccess);
            }
        });
    }

    /**
     * Fetches scores from the cloud (or local fallback).
     * 
     * @param callback Callback for success/error.
     */
    public static void fetchScores(LeaderboardCallback callback) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE")) {
            callback.onScoresLoaded(loadScores());
            return;
        }

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(
                com.badlogic.gdx.Net.HttpMethods.GET);
        request.setUrl(CLOUD_URL);

        Gdx.net.sendHttpRequest(request, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String result = httpResponse.getResultAsString();
                Gdx.app.log("Leaderboard", "Fetch Response Code: " + statusCode);

                if (statusCode != 200) {
                    Gdx.app.error("Leaderboard", "Fetch Failed code: " + statusCode + ", Body: " + result);
                    Gdx.app.postRunnable(() -> callback.onError("Server Error: " + statusCode));
                    return;
                }

                try {
                    Json json = new Json();
                    ArrayList<ScoreEntry> onlineScores = json.fromJson(ArrayList.class, ScoreEntry.class, result);

                    Gdx.app.postRunnable(() -> callback.onScoresLoaded(onlineScores));

                } catch (Exception e) {
                    Gdx.app.error("Leaderboard", "Parse Error", e);
                    Gdx.app.postRunnable(() -> callback.onError("Parse Error"));
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Leaderboard", "Network Error", t);
                Gdx.app.postRunnable(() -> callback.onScoresLoaded(loadScores()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Cancelled"));
            }
        });
    }

    /**
     * Clears the online leaderboard (Admin/Debug function).
     * 
     * @param onSuccess Callback on success.
     */
    public static void clearOnlineLeaderboard(Runnable onSuccess) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE"))
            return;

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(
                com.badlogic.gdx.Net.HttpMethods.DELETE);
        request.setUrl(CLOUD_URL);

        Gdx.net.sendHttpRequest(request, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                Gdx.app.log("Leaderboard", "Clear Success: " + httpResponse.getStatus().getStatusCode());
                if (onSuccess != null)
                    Gdx.app.postRunnable(onSuccess);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log("Leaderboard", "Clear Failed: " + t.getMessage());
            }

            @Override
            public void cancelled() {
            }
        });
    }

    /**
     * Adds a random test score for debugging.
     */
    public static void addDebugEntry() {
        int randomScore = (int) (Math.random() * 5000) + 100;
        String name = "DebugPlayer" + (int) (Math.random() * 100);
        saveScore(name, randomScore);
    }

    /**
     * Loads scores from local file.
     * 
     * @return List of scores.
     */
    public static ArrayList<ScoreEntry> loadScores() {
        FileHandle file = Gdx.files.local(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        Json json = new Json();
        try {
            ArrayList<ScoreEntry> list = json.fromJson(ArrayList.class, ScoreEntry.class, file);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
