package net.darkhax.botanypots.fabric;

import net.darkhax.botanypots.common.impl.IPlatformHooks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.BiFunction;

public class FabricPlatformHooks implements IPlatformHooks {

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Set<Block> blocks) {
        return FabricBlockEntityTypeBuilder.<T>create(factory::apply).addBlocks(blocks).build();
    }
}
