package com.example.cameraflick;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class Profiles {
    enum Direction {
        RIGHT, LEFT
    }

    static final class Profile {
        String name;
        double angle;
        Direction direction;
        int outMs;
        int delayMs;
        int backMs;
        double smoothness;

        Profile(String name, double angle, Direction direction,
                int outMs, int delayMs, int backMs, double smoothness) {
            this.name = name;
            this.angle = angle;
            this.direction = direction;
            this.outMs = outMs;
            this.delayMs = delayMs;
            this.backMs = backMs;
            this.smoothness = smoothness;
        }
    }

    final Profile first = new Profile(
            "Profile 1", 90, Direction.RIGHT, 70, 0, 70, 0.65
    );

    final Profile second = new Profile(
            "Profile 2", 90, Direction.LEFT, 70, 0, 70, 0.65
    );

    int activeProfile = 1;

    private final Path file =
            Path.of("config", "camera_flick_profiles.properties");

    Profile getActive() {
        return activeProfile == 1 ? first : second;
    }

    void switchProfile() {
        activeProfile = activeProfile == 1 ? 2 : 1;
        save();
    }

    static Profiles load() {
        Profiles profiles = new Profiles();
        Properties p = new Properties();

        try {
            if (Files.exists(profiles.file)) {
                try (InputStream in = Files.newInputStream(profiles.file)) {
                    p.load(in);
                }

                profiles.activeProfile = clamp(
                        parseInt(p, "activeProfile", 1), 1, 2
                );

                profiles.readProfile(p, profiles.first, "profile1");
                profiles.readProfile(p, profiles.second, "profile2");
            }

            profiles.save();
        } catch (IOException ignored) {
        }

        return profiles;
    }

    private void readProfile(Properties p, Profile profile, String prefix) {
        profile.angle = clamp(
                parseDouble(p, prefix + ".angle", profile.angle), 1, 360
        );

        profile.outMs = clamp(
                parseInt(p, prefix + ".outMs", profile.outMs), 1, 5000
        );

        profile.delayMs = clamp(
                parseInt(p, prefix + ".delayMs", profile.delayMs), 0, 10000
        );

        profile.backMs = clamp(
                parseInt(p, prefix + ".backMs", profile.backMs), 1, 5000
        );

        profile.smoothness = clamp(
                parseDouble(p, prefix + ".smoothness", profile.smoothness),
                0, 1
        );

        try {
            profile.direction = Direction.valueOf(
                    p.getProperty(
                            prefix + ".direction",
                            profile.direction.name()
                    ).trim().toUpperCase()
            );
        } catch (IllegalArgumentException ignored) {
        }
    }

    void save() {
        try {
            Files.createDirectories(file.getParent());

            Properties p = new Properties();
            p.setProperty("activeProfile", Integer.toString(activeProfile));

            writeProfile(p, first, "profile1");
            writeProfile(p, second, "profile2");

            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Camera Flick - independent profile settings");
            }
        } catch (IOException ignored) {
        }
    }

    private void writeProfile(
            Properties p, Profile profile, String prefix) {
        p.setProperty(prefix + ".angle", Double.toString(profile.angle));
        p.setProperty(prefix + ".direction", profile.direction.name());
        p.setProperty(prefix + ".outMs", Integer.toString(profile.outMs));
        p.setProperty(prefix + ".delayMs", Integer.toString(profile.delayMs));
        p.setProperty(prefix + ".backMs", Integer.toString(profile.backMs));
        p.setProperty(
                prefix + ".smoothness", Double.toString(profile.smoothness)
        );
    }

    private static int parseInt(
            Properties p, String key, int fallback) {
        try {
            return Integer.parseInt(
                    p.getProperty(key, Integer.toString(fallback)).trim()
            );
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static double parseDouble(
            Properties p, String key, double fallback) {
        try {
            return Double.parseDouble(
                    p.getProperty(key, Double.toString(fallback)).trim()
            );
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
