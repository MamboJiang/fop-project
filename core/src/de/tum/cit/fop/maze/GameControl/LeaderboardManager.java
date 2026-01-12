package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.json";
    private static final int MAX_SCORES = 10;

    private static final String CLOUD_URL = "https://leaderboard-backup.vercel.app/api/leaderboard"; 

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int score;

        public ScoreEntry() {}
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return other.score - this.score; // 降序排列 (分数高的在前)
        }
    }

    public interface LeaderboardCallback {
        void onScoresLoaded(ArrayList<ScoreEntry> scores);
        void onError(String message);
    }

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
        } catch(Exception e) {
            Gdx.app.error("Leaderboard", "Local Save Failed", e);
        }

        uploadScoreToCloud(name, score, callbacks);
    }

    public static void saveScore(String name, int score) {
        saveScore(name, score, null);
    }

    public static void uploadScoreToCloud(String name, int score, Runnable onSuccess) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE")) {
             if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
             return; 
        }

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(com.badlogic.gdx.Net.HttpMethods.POST);
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

                if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
            }
            @Override
            public void failed(Throwable t) {
                Gdx.app.log("Leaderboard", "Upload Failed: " + t.getMessage());
                if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
            }
            @Override
            public void cancelled() {
                if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
            }
        });
    }


    public static void fetchScores(LeaderboardCallback callback) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE")) {
                callback.onScoresLoaded(loadScores());
                return;
        }

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(com.badlogic.gdx.Net.HttpMethods.GET);
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


    public static void clearOnlineLeaderboard(Runnable onSuccess) {
        if (CLOUD_URL.contains("YOUR_VERCEL_URL_HERE")) return;

        com.badlogic.gdx.Net.HttpRequest request = new com.badlogic.gdx.Net.HttpRequest(com.badlogic.gdx.Net.HttpMethods.DELETE);
        request.setUrl(CLOUD_URL);
        
        Gdx.net.sendHttpRequest(request, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                Gdx.app.log("Leaderboard", "Clear Success: " + httpResponse.getStatus().getStatusCode());
                if (onSuccess != null) Gdx.app.postRunnable(onSuccess);
            }
            @Override
            public void failed(Throwable t) {
                Gdx.app.log("Leaderboard", "Clear Failed: " + t.getMessage());
            }
            @Override
            public void cancelled() {}
        });
    }

    public static void addDebugEntry() {
        int randomScore = (int)(Math.random() * 5000) + 100;
        String name = "DebugPlayer" + (int)(Math.random() * 100);
        saveScore(name, randomScore);
    }
    

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
