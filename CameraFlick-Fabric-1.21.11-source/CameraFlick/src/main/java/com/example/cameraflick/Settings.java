package com.example.cameraflick;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class Settings {
    enum Direction { RIGHT, LEFT }
    double angle = 45;
    int outMs = 70;
    int delayMs = 0;
    int backMs = 70;
    double smoothness = 0.65;
    Direction direction = Direction.RIGHT;
    private final Path file = Path.of("config", "camera_flick.properties");

    static Settings load() {
        Settings s = new Settings();
        Properties p = new Properties();
        try {
            if (Files.exists(s.file)) {
                try (InputStream in = Files.newInputStream(s.file)) { p.load(in); }
                s.angle = clamp(parseDouble(p, "angle", 45), 1, 360);
                s.outMs = clamp(parseInt(p, "outMs", 70), 1, 5000);
                s.delayMs = clamp(parseInt(p, "delayMs", 0), 0, 10000);
                s.backMs = clamp(parseInt(p, "backMs", 70), 1, 5000);
                s.smoothness = clamp(parseDouble(p, "smoothness", 0.65), 0, 1);
                try { s.direction = Direction.valueOf(p.getProperty("direction", "RIGHT").trim().toUpperCase()); }
                catch (IllegalArgumentException ignored) { s.direction = Direction.RIGHT; }
            }
            s.save();
        } catch (IOException ignored) { }
        return s;
    }

    void save() {
        try {
            Files.createDirectories(file.getParent());
            Properties p = new Properties();
            p.setProperty("angle", Double.toString(angle));
            p.setProperty("direction", direction.name());
            p.setProperty("outMs", Integer.toString(outMs));
            p.setProperty("delayMs", Integer.toString(delayMs));
            p.setProperty("backMs", Integer.toString(backMs));
            p.setProperty("smoothness", Double.toString(smoothness));
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Camera Flick settings. Times are milliseconds; smoothness is 0.0-1.0.");
            }
        } catch (IOException ignored) { }
    }

    private static int parseInt(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback)).trim()); }
        catch (RuntimeException e) { return fallback; }
    }
    private static double parseDouble(Properties p, String key, double fallback) {
        try { return Double.parseDouble(p.getProperty(key, Double.toString(fallback)).trim()); }
        catch (RuntimeException e) { return fallback; }
    }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}
