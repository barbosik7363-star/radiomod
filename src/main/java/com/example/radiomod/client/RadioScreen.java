package com.example.radiomod.client;

import com.example.radiomod.block.RadioBlockEntity;
import com.example.radiomod.network.Network;
import com.example.radiomod.network.RadioPacket;
import com.example.radiomod.radio.RadioPlaylist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Locale;

public class RadioScreen extends Screen {
    private static final int PANEL_W = 220;
    private static final int PANEL_H = 165;

    private final BlockPos pos;
    private Button playButton;
    private Button repeatButton;
    private VolumeSlider volumeSlider;
    private int lastVolumeSent = 100;

    public RadioScreen(BlockPos pos) {
        super(Component.translatable("gui.radiomod.title"));
        this.pos = pos;
    }

    private RadioBlockEntity getRadio() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        BlockEntity be = mc.level.getBlockEntity(pos);
        return be instanceof RadioBlockEntity r ? r : null;
    }

    private void send(int action, int value) {
        Network.CHANNEL.sendToServer(new RadioPacket(pos, action, value));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - PANEL_H / 2;

        // Частота: < >
        addRenderableWidget(Button.builder(Component.literal("<"),
                b -> send(RadioBlockEntity.ACTION_PREV_STATION, 0)).bounds(cx - 100, top + 22, 30, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),
                b -> send(RadioBlockEntity.ACTION_NEXT_STATION, 0)).bounds(cx + 70, top + 22, 30, 20).build());

        // Треки: |< Play >|
        addRenderableWidget(Button.builder(Component.literal("|<"),
                b -> send(RadioBlockEntity.ACTION_PREV_TRACK, 0)).bounds(cx - 100, top + 82, 30, 20).build());
        playButton = addRenderableWidget(Button.builder(Component.translatable("gui.radiomod.play"),
                b -> send(RadioBlockEntity.ACTION_TOGGLE_PLAY, 0)).bounds(cx - 35, top + 82, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">|"),
                b -> send(RadioBlockEntity.ACTION_NEXT_TRACK, 0)).bounds(cx + 70, top + 82, 30, 20).build());

        // Повтор
        repeatButton = addRenderableWidget(Button.builder(Component.translatable("gui.radiomod.repeat_off"),
                b -> send(RadioBlockEntity.ACTION_TOGGLE_REPEAT, 0)).bounds(cx - 100, top + 108, 200, 20).build());

        // Громкость
        RadioBlockEntity radio = getRadio();
        double vol = radio != null ? radio.getVolume() : 1.0;
        volumeSlider = addRenderableWidget(new VolumeSlider(cx - 100, top + 134, 200, 20, vol));
        lastVolumeSent = volumeSlider.percent();
    }

    @Override
    public void tick() {
        RadioBlockEntity radio = getRadio();
        if (radio == null) {
            onClose();
            return;
        }
        playButton.setMessage(Component.translatable(radio.isPlaying() ? "gui.radiomod.stop" : "gui.radiomod.play"));
        repeatButton.setMessage(Component.translatable(radio.isRepeat() ? "gui.radiomod.repeat_on" : "gui.radiomod.repeat_off"));

        // громкость меняется "вживую" при перетаскивании ползунка
        int p = volumeSlider.percent();
        if (p != lastVolumeSent) {
            lastVolumeSent = p;
            send(RadioBlockEntity.ACTION_SET_VOLUME, p);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int top = this.height / 2 - PANEL_H / 2;

        g.fill(cx - PANEL_W / 2, top, cx + PANEL_W / 2, top + PANEL_H, 0xE0101010);
        g.renderOutline(cx - PANEL_W / 2, top, PANEL_W, PANEL_H, 0xFF707070);
        g.drawCenteredString(this.font, this.title, cx, top + 8, 0xFFFFFF);

        RadioBlockEntity radio = getRadio();
        if (radio != null) {
            double freq = RadioPlaylist.FREQS[radio.getStation()];
            g.drawCenteredString(this.font,
                    String.format(Locale.ROOT, "FM %.1f", freq), cx, top + 28, 0x55FF55);

            RadioPlaylist.Song song = RadioPlaylist.get(radio.getStation(), radio.getTrack());
            if (song != null) {
                String title = this.font.plainSubstrByWidth(song.title(), 190);
                g.drawCenteredString(this.font, title, cx, top + 56, 0xFFFFFF);
                g.drawCenteredString(this.font,
                        (radio.getTrack() + 1) + " / " + RadioPlaylist.size(radio.getStation()),
                        cx, top + 68, 0xAAAAAA);
            } else {
                g.drawCenteredString(this.font, Component.translatable("gui.radiomod.no_signal"),
                        cx, top + 60, 0xFF5555);
            }
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class VolumeSlider extends AbstractSliderButton {
        VolumeSlider(int x, int y, int w, int h, double value) {
            super(x, y, w, h, Component.empty(), value);
            updateMessage();
        }

        int percent() {
            return (int) Math.round(this.value * 100);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.radiomod.volume", percent() + "%"));
        }

        @Override
        protected void applyValue() {
            // значение отправляется из tick() экрана
        }
    }
}
