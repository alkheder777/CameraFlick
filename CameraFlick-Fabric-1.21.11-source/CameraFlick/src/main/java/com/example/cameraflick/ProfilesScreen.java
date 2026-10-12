
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

public final class CameraFlickClient implements ClientModInitializer {
    private static Profiles profiles;
    private static Animation animation;
    private static KeyBinding profile1Key;
    private static KeyBinding profile2Key;

    @Override
    public void onInitializeClient() {
        profiles = Profiles.load();
        animation = new Animation();

        profile1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.camera_flick.profile1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        profile2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.camera_flick.profile2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (profile1Key.wasPressed()) {
                profiles.activeProfile = 1;
                profiles.save();
                animation.start(client, profiles.first);
                tell(client, "Profile 1");
            }

            while (profile2Key.wasPressed()) {
                profiles.activeProfile = 2;
                profiles.save();
                animation.start(client, profiles.second);
                tell(client, "Profile 2");
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                animation.render(MinecraftClient.getInstance()));
    }

    private static void tell(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Camera Flick] " + message), true);
        }
    }

    private static final class Animation {
        private boolean active;
        private long startNanos;
        private float baseYaw;
        private float basePitch;
        private double angle;
        private double smoothness;
        private long outNanos;
        private long holdNanos;
        private long backNanos;

        void start(MinecraftClient client, Profiles.Profile profile) {
            if (client.player == null || active) return;

            baseYaw = client.player.getYaw();
            basePitch = client.player.getPitch();

            angle = profile.angle
                    * (profile.direction == Profiles.Direction.RIGHT ? 1 : -1);

            smoothness = profile.smoothness;
            outNanos = Math.max(1, profile.outMs) * 1_000_000L;
            holdNanos = Math.max(0, profile.delayMs) * 1_000_000L;
            backNanos = Math.max(1, profile.backMs) * 1_000_000L;

            startNanos = System.nanoTime();
            active = true;
        }

        void render(MinecraftClient client) {
            if (!active || client.player == null) return;

            long elapsed = System.nanoTime() - startNanos;
            double offset;

            if (elapsed < outNanos) {
                offset = angle * ease(
                        elapsed / (double) outNanos, smoothness);
            } else if (elapsed < outNanos + holdNanos) {
                offset = angle;
            } else if (elapsed < outNanos + holdNanos + backNanos) {
                double p = (elapsed - outNanos - holdNanos)
                        / (double) backNanos;
                offset = angle * (1.0 - ease(p, smoothness));
            } else {
                client.player.setYaw(baseYaw);
                client.player.setPitch(basePitch);
                active = false;
                return;
            }

            client.player.setYaw((float) (baseYaw + offset));
            client.player.setPitch(basePitch);
        }

        private double ease(double x, double strength) {
            x = Math.max(0, Math.min(1, x));
            double smooth = x * x * (3 - 2 * x);
            return x * (1 - strength) + smooth * strength;
        }
    }
}
