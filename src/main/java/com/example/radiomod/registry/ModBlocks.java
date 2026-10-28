package com.example.radiomod.registry;

import com.example.radiomod.RadioMod;
import com.example.radiomod.block.RadioBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RadioMod.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RadioMod.MODID);

    public static final RegistryObject<Block> RADIO = BLOCKS.register("radio", RadioBlock::new);
    public static final RegistryObject<Item> RADIO_ITEM =
            ITEMS.register("radio", () -> new BlockItem(RADIO.get(), new Item.Properties()));
}
