package com.example.radiomod.registry;

import com.example.radiomod.RadioMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RadioMod.MODID);

    public static final RegistryObject<CreativeModeTab> RADIO_TAB = TABS.register("radio_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RadioMod.MODID))
                    .icon(() -> new ItemStack(ModBlocks.RADIO_ITEM.get()))
                    .displayItems((params, output) -> output.accept(ModBlocks.RADIO_ITEM.get()))
                    .build());
}
