
package com.example.cameraflick;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class ProfilesScreen extends Screen {
    private final Screen parent;
    private final Profiles profiles;

    private ButtonWidget profileButton;
    private ButtonWidget directionButton;

    private TextFieldWidget angleField;
    private TextFieldWidget outField;
    private TextFieldWidget delayField;
    private TextFieldWidget backField;
    private TextFieldWidget smoothField;

    public ProfilesScreen(Screen parent, Profiles profiles) {
        super(Text.literal("Camera Flick Settings"));
        this.parent = parent;
        this.profiles = profiles;
    }

    private Profiles.Profile selected() {
        return profiles.getActive();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 10;
        int top = 48;
        int row = 29;

        profileButton = addDrawableChild(ButtonWidget.builder(
                profileLabel(), button -> {
                    applyFields();
                    profiles.switchProfile();
                    refreshFields();
                }).dimensions(left, top, 310, 22).build());

        angleField = addField(left, top + row, 145,
                Double.toString(selected().angle));
        outField = addField(left, top + row * 2, 145,
                Integer.toString(selected().outMs));
        delayField = addField(left, top + row * 3, 145,
                Integer.toString(selected().delayMs));
        backField = addField(left, top + row * 4, 145,
                Integer.toString(selected().backMs));
        smoothField = addField(left, top + row * 5, 145,
                Double.toString(selected().smoothness));

        directionButton = addDrawableChild(ButtonWidget.builder(
                directionLabel(), button -> {
                    applyFields();
                    selected().direction =
                            selected().direction == Profiles.Direction.RIGHT
                                    ? Profiles.Direction.LEFT
                                    : Profiles.Direction.RIGHT;
                    directionButton.setMessage(directionLabel());
                    profiles.save();
                }).dimensions(right, top + row, 145, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Save settings"), button -> {
                    applyFields();
                    profiles.save();
                }).dimensions(left, top + row * 6 + 8, 150, 22).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"), button -> {
                    applyFields();
                    profiles.save();
                    close();
                }).dimensions(right, top + row * 6 + 8, 145, 22).build());
    }

    private TextFieldWidget addField(
            int x, int y, int width, String value) {
        TextFieldWidget field = new TextFieldWidget(
                textRenderer, x, y, width, 20, Text.empty());
        field.setText(value);
        addDrawableChild(field);
        return field;
    }

    private Text profileLabel() {
        return Text.literal("Editing Profile " + profiles.activeProfile
                + " (click to switch)");
    }

    private Text directionLabel() {
        return Text.literal("Direction: " + selected().direction.name());
    }

    private void refreshFields() {
        profileButton.setMessage(profileLabel());
        directionButton.setMessage(directionLabel());
        angleField.setText(Double.toString(selected().angle));
        outField.setText(Integer.toString(selected().outMs));
        delayField.setText(Integer.toString(selected().delayMs));
        backField.setText(Integer.toString(selected().backMs));
        smoothField.setText(Double.toString(selected().smoothness));
    }

    private void applyFields() {
        Profiles.Profile p = selected();

        p.angle = parseDouble(angleField.getText(), p.angle, 1, 360);
        p.outMs = (int) parseDouble(outField.getText(), p.outMs, 1, 5000);
        p.delayMs = (int) parseDouble(delayField.getText(), p.delayMs, 0, 10000);
        p.backMs = (int) parseDouble(backField.getText(), p.backMs, 1, 5000);
        p.smoothness = parseDouble(smoothField.getText(), p.smoothness, 0, 1);

        profiles.save();
    }

    private double parseDouble(
            String value, double fallback, double min, double max) {
        try {
            double number = Double.parseDouble(value.trim());
            if (!Double.isFinite(number)) return fallback;
            return Math.max(min, Math.min(max, number));
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    @Override
    public void render(
            DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                textRenderer, title, width / 2, 20, 0xFFFFFF);

        int x = width / 2 - 155;
        int y = 48 + 29;

        context.drawTextWithShadow(
                textRenderer, "Angle (1-360 degrees)", x, y - 10, 0xFFFFFF);
        context.drawTextWithShadow(
                textRenderer, "Out duration (1-5000 ms)", x, y + 19, 0xFFFFFF);
        context.drawTextWithShadow(
                textRenderer, "Hold delay (0-10000 ms)", x, y + 48, 0xFFFFFF);
        context.drawTextWithShadow(
                textRenderer, "Return duration (1-5000 ms)", x, y + 77, 0xFFFFFF);
        context.drawTextWithShadow(
                textRenderer, "Smoothness (0-1)", x, y + 106, 0xFFFFFF);
    }

    @Override
    public void close() {
        profiles.save();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
