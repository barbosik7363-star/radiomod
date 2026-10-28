package com.example.radiomod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientHooks {
    public static void openRadioScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new RadioScreen(pos));
    }
}
