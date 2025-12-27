package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Collections;

    public class LeaderboardManager {
        private static final String FILE_NAME = "leaderboard.json";
        private static final int MAX_SCORES = 10; // 只保留前10名

        // 内部类：单条分数记录
        public static class ScoreEntry implements Comparable<ScoreEntry> {
            public String name;
            public int score;

            public ScoreEntry() {} // Json 需要空构造函数
            public ScoreEntry(String name, int score) {
                this.name = name;
                this.score = score;
            }

            @Override
            public int compareTo(ScoreEntry other) {
                return other.score - this.score; // 降序排列 (分数高的在前)
            }
        }

        // 保存分数
        public static void saveScore(String name, int score) {
            ArrayList<ScoreEntry> scores = loadScores();
            scores.add(new ScoreEntry(name, score));
            Collections.sort(scores);

            // 截断，只保留前 N 名
            if (scores.size() > MAX_SCORES) {
                scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
            }

            Json json = new Json();
            FileHandle file = Gdx.files.local(FILE_NAME);
            file.writeString(json.toJson(scores), false);
        }

        // 读取分数列表
        @SuppressWarnings("unchecked")
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

