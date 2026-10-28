package com.example.radiomod.block;

import com.example.radiomod.client.RadioClientHandler;
import com.example.radiomod.radio.RadioPlaylist;
import com.example.radiomod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RadioBlockEntity extends BlockEntity {
    public static final int ACTION_TOGGLE_PLAY = 0;
    public static final int ACTION_NEXT_TRACK = 1;
    public static final int ACTION_PREV_TRACK = 2;
    public static final int ACTION_NEXT_STATION = 3;
    public static final int ACTION_PREV_STATION = 4;
    public static final int ACTION_TOGGLE_REPEAT = 5;
    public static final int ACTION_SET_VOLUME = 6;

    private int station = 0;
    private int track = 0;
    private int volume = 100; // 0..100
    private boolean repeat = false;
    private boolean playing = false;
    private int playId = 0;      // растёт при каждом (пере)запуске трека
    private int ticksPlayed = 0; // только на сервере

    // Клиентские данные звука (тип Object, чтобы не тянуть клиентские классы на сервер)
    public Object clientInstance;
    public int clientPlayId = -1;
    public int clientTicks = 0;        // тиков с начала текущего трека (клиент)
    public int clientLastStart = -100; // когда в последний раз пытались запустить звук

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIO.get(), pos, state);
    }

    // ---------- геттеры ----------
    public int getStation() { return station; }
    public int getTrack() { return track; }
    public float getVolume() { return volume / 100f; }
    public int getVolumePercent() { return volume; }
    public boolean isRepeat() { return repeat; }
    public boolean isPlaying() { return playing; }
    public int getPlayId() { return playId; }

    // ---------- логика (сервер) ----------
    public void handleAction(int action, int value) {
        int stations = RadioPlaylist.stationCount();
        switch (action) {
            case ACTION_TOGGLE_PLAY -> {
                if (playing) {
                    playing = false;
                } else if (RadioPlaylist.get(station, track) != null) {
                    start();
                }
            }
            case ACTION_NEXT_TRACK -> changeTrack(1);
            case ACTION_PREV_TRACK -> changeTrack(-1);
            case ACTION_NEXT_STATION -> changeStation((station + 1) % stations);
            case ACTION_PREV_STATION -> changeStation((station - 1 + stations) % stations);
            case ACTION_TOGGLE_REPEAT -> repeat = !repeat;
            case ACTION_SET_VOLUME -> volume = Math.max(0, Math.min(100, value));
            default -> { return; }
        }
        sync();
    }

    private void changeTrack(int delta) {
        int size = RadioPlaylist.size(station);
        if (size == 0) return;
        track = (track + delta + size) % size;
        if (playing) start(); else ticksPlayed = 0;
    }

    private void changeStation(int newStation) {
        station = newStation;
        track = 0;
        if (playing) {
            if (RadioPlaylist.get(station, track) != null) start();
            else playing = false;
        } else {
            ticksPlayed = 0;
        }
    }

    private void start() {
        playing = true;
        ticksPlayed = 0;
        playId++;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // ---------- тик ----------
    public static void tick(Level level, BlockPos pos, BlockState state, RadioBlockEntity be) {
        if (level.isClientSide) {
            RadioClientHandler.tick(be);
        } else {
            be.serverTick();
        }
    }

    private void serverTick() {
        if (!playing) return;
        RadioPlaylist.Song song = RadioPlaylist.get(station, track);
        if (song == null) {
            playing = false;
            sync();
            return;
        }
        ticksPlayed++;
        if (ticksPlayed >= song.ticks() + 20) { // +1 сек тишины между треками
            if (repeat) {
                start();
            } else {
                int size = RadioPlaylist.size(station);
                track = (track + 1) % size;
                start();
            }
            sync();
        }
    }

    // ---------- NBT / синхронизация ----------
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Station", station);
        tag.putInt("Track", track);
        tag.putInt("Volume", volume);
        tag.putBoolean("Repeat", repeat);
        tag.putBoolean("Playing", playing);
        tag.putInt("PlayId", playId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        station = tag.getInt("Station");
        track = tag.getInt("Track");
        volume = tag.contains("Volume") ? tag.getInt("Volume") : 100;
        repeat = tag.getBoolean("Repeat");
        playing = tag.getBoolean("Playing");
        playId = tag.getInt("PlayId");
        if (station < 0 || station >= RadioPlaylist.stationCount()) station = 0;
        if (RadioPlaylist.get(station, track) == null) track = 0;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---------- остановка звука на клиенте ----------
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) RadioClientHandler.stop(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && level.isClientSide) RadioClientHandler.stop(this);
    }
}
