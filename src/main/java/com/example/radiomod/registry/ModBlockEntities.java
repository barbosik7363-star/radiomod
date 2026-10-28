package com.example.radiomod.registry;

import com.example.radiomod.RadioMod;
import com.example.radiomod.block.RadioBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RadioMod.MODID);

    public static final RegistryObject<BlockEntityType<RadioBlockEntity>> RADIO =
            BLOCK_ENTITIES.register("radio", () ->
                    BlockEntityType.Builder.of(RadioBlockEntity::new, ModBlocks.RADIO.get()).build(null));
}
