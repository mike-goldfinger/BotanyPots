package net.darkhax.botanypots.common.impl;

import net.darkhax.bookshelf.common.api.service.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Hooks for functionality that can not be implemented in the common project.
 */
public interface IPlatformHooks {

    IPlatformHooks GET = Services.load(IPlatformHooks.class);

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Set<Block> blocks);
}
