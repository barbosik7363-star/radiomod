package com.example.radiomod.registry;

import com.example.radiomod.RadioMod;
import com.example.radiomod.radio.RadioPlaylist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RadioMod.MODID);

    private static final Map<String, RegistryObject<SoundEvent>> MAP = new HashMap<>();

    static {
        for (RadioPlaylist.Song song : RadioPlaylist.all()) {
            String name = song.soundName();
            MAP.put(name, SOUNDS.register(name,
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RadioMod.MODID, name))));
        }
    }

    public static SoundEvent get(String name) {
        return MAP.get(name).get();
    }
}
