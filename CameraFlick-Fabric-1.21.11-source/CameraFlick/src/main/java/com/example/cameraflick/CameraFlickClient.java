package com.example.cameraflick;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Camera Flick: a client-side, configurable camera rotation animation.
 * Settings are in config/camera_flick.properties. Edit while the game is closed,
 * or use the in-game keybinds to change direction / angle presets.
 */
public final class CameraFlickClient implements ClientModInitializer {
    private static KeyBinding flickKey;
    private static KeyBinding directionKey;
    private static KeyBinding angleKey;
    private static Settings settings;
    private static Animation animation;

    @Override
    public void onInitializeClient() {
        settings = Settings.load();
        animation = new Animation();
        flickKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.camera_flick.activate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC));
        directionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.camera_flick.direction", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN,
                KeyBinding.Category.MISC));
        angleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.camera_flick.angle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN,
                KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (flickKey.wasPressed()) animation.start(client, settings);
            while (directionKey.wasPressed()) {
                settings.direction = settings.direction == Settings.Direction.RIGHT
                        ? Settings.Direction.LEFT : Settings.Direction.RIGHT;
                settings.save();
                tell(client, "Direction: " + settings.direction.name());
            }
            while (angleKey.wasPressed()) {
                settings.angle = settings.angle >= 180 ? 15 : settings.angle + 15;
                settings.save();
                tell(client, "Angle: " + settings.angle + " degrees");
            }
        });

        // Uses wall-clock time so animations can be shorter than a game tick.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> animation.render(MinecraftClient.getInstance(), settings));
    }

    private static void tell(MinecraftClient client, String message) {
        if (client.player != null) client.player.sendMessage(Text.literal("[Camera Flick] " + message), true);
    }

    private static final class Animation {
        private boolean active;
        private long startNanos;
        private float baseYaw;
        private float basePitch;
        private double angle;
        private long outNanos;
        private long holdNanos;
        private long backNanos;
        private Settings.Direction direction;

        void start(MinecraftClient client, Settings s) {
            if (client.player == null || active) return;
            baseYaw = client.player.getYaw();
            basePitch = client.player.getPitch();
            angle = s.angle * (s.direction == Settings.Direction.RIGHT ? 1.0 : -1.0);
            direction = s.direction;
            outNanos = Math.max(1, s.outMs) * 1_000_000L;
            holdNanos = Math.max(0, s.delayMs) * 1_000_000L;
            backNanos = Math.max(1, s.backMs) * 1_000_000L;
            startNanos = System.nanoTime();
            active = true;
        }

        void render(MinecraftClient client, Settings s) {
            if (!active || client.player == null) return;
            long elapsed = System.nanoTime() - startNanos;
            double offset;
            if (elapsed < outNanos) {
                offset = angle * ease(elapsed / (double) outNanos, s.smoothness);
            } else if (elapsed < outNanos + holdNanos) {
                offset = angle;
            } else if (elapsed < outNanos + holdNanos + backNanos) {
                double p = (elapsed - outNanos - holdNanos) / (double) backNanos;
                offset = angle * (1.0 - ease(p, s.smoothness));
            } else {
                offset = 0;
                active = false;
            }
            // Preserve pitch. Yaw is restored to the exact starting value at completion.
            client.player.setYaw((float) (baseYaw + offset));
            client.player.setPitch(basePitch);
        }

        private double ease(double x, double strength) {
            x = Math.max(0, Math.min(1, x));
            // Smoothstep blended with linear motion; strength 0=linear, 1=very smooth.
            double smooth = x * x * (3 - 2 * x);
            return x * (1 - strength) + smooth * strength;
        }
    }
}
