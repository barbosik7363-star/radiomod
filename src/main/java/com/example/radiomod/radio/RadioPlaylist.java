package com.example.radiomod.radio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Список песен и раскладка по FM-частотам.
 *
 * Чтобы добавить песню: положи .ogg (МОНО!) в assets/radiomod/sounds/songs/,
 * впиши её в sounds.json и добавь строку в ALL ниже (длительность в тиках = секунды * 20).
 * Чтобы добавить частоту: допиши число в FREQS.
 * Песни раскидываются по частотам случайно, но с фиксированным seed, поэтому
 * раскладка одинаковая на сервере и у всех игроков. Одна песня есть только на одной частоте.
 */
public final class RadioPlaylist {
    public record Song(String title, String soundName, int ticks) {}

    public static final double[] FREQS = {90.5, 102.7};

    private static final Song[] ALL = {
            new Song("Napoleon Bonaparte is back", "napoleon", 488),
            new Song("Bomboclat Wallahi", "bomboclat", 830),
            new Song("Track 3", "track3", 358),
    };

    private static final long SEED = 20260601L; // поменяй число, чтобы раскидать песни иначе

    private static final List<List<Song>> STATIONS = build();

    private static List<List<Song>> build() {
        List<Song> shuffled = new ArrayList<>(List.of(ALL));
        Collections.shuffle(shuffled, new Random(SEED));
        List<List<Song>> result = new ArrayList<>();
        for (int i = 0; i < FREQS.length; i++) result.add(new ArrayList<>());
        for (int i = 0; i < shuffled.size(); i++) {
            result.get(i % FREQS.length).add(shuffled.get(i));
        }
        return result;
    }

    public static Song[] all() { return ALL; }

    public static int stationCount() { return FREQS.length; }

    public static int size(int station) {
        if (station < 0 || station >= STATIONS.size()) return 0;
        return STATIONS.get(station).size();
    }

    public static Song get(int station, int track) {
        if (station < 0 || station >= STATIONS.size()) return null;
        List<Song> list = STATIONS.get(station);
        if (track < 0 || track >= list.size()) return null;
        return list.get(track);
    }

    private RadioPlaylist() {}
}
