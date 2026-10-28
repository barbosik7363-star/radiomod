package com.example.radiomod.client;

import com.example.radiomod.block.RadioBlockEntity;
import com.example.radiomod.radio.RadioPlaylist;
import com.example.radiomod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class RadioClientHandler {
    /** Радиус, в котором радио слышно (блоков). */
    public static final double HEAR_RANGE = 10.0;

    public static void tick(RadioBlockEntity be) {
        if (!be.isPlaying()) {
            stop(be);
            return;
        }
        RadioPlaylist.Song song = RadioPlaylist.get(be.getStation(), be.getTrack());
        if (song == null) {
            stop(be);
            return;
        }

        // новый запуск трека на сервере
        if (be.clientPlayId != be.getPlayId()) {
            stopInstance(be);
            be.clientPlayId = be.getPlayId();
            be.clientTicks = 0;
            be.clientLastStart = -100;
        } else {
            be.clientTicks++;
        }

        // трек уже заканчивается — ничего не перезапускаем
        if (be.clientTicks >= song.ticks() - 10) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // звук уже идёт
        if (be.clientInstance instanceof RadioSoundInstance inst && mc.getSoundManager().isActive(inst)) return;

        // не спамим попытками запуска
        if (be.clientTicks - be.clientLastStart < 20) return;

        // запускаем только когда игрок в радиусе слышимости
        // (так звук гарантированно стартует, даже если игрок подошёл позже)
        BlockPos p = be.getBlockPos();
        if (mc.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > HEAR_RANGE * HEAR_RANGE) return;

        stopInstance(be);
        RadioSoundInstance instance = new RadioSoundInstance(ModSounds.get(song.soundName()), be);
        be.clientInstance = instance;
        be.clientLastStart = be.clientTicks;
        mc.getSoundManager().play(instance);
    }

    private static void stopInstance(RadioBlockEntity be) {
        if (be.clientInstance instanceof RadioSoundInstance instance) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
        be.clientInstance = null;
    }

    public static void stop(RadioBlockEntity be) {
        stopInstance(be);
        be.clientPlayId = -1;
        be.clientTicks = 0;
        be.clientLastStart = -100;
    }
}
