package com.example.radiomod.client;

import com.example.radiomod.block.RadioBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class RadioSoundInstance extends AbstractTickableSoundInstance {
    private final RadioBlockEntity be;

    public RadioSoundInstance(SoundEvent sound, RadioBlockEntity be) {
        super(sound, SoundSource.RECORDS, RandomSource.create());
        this.be = be;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.looping = false;
        this.delay = 0;
        this.volume = be.getVolume();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (be.isRemoved()) {
            stop();
            return;
        }
        this.volume = be.getVolume();
    }
}
