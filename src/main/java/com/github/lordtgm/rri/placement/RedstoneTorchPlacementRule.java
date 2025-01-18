package com.github.lordtgm.rri.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedstoneTorchPlacementRule extends BlockPlacementRule {
    public RedstoneTorchPlacementRule() {
        super(Block.REDSTONE_TORCH);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull BlockPlacementRule.PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) return null;
        if (blockFace == BlockFace.BOTTOM) return null;
        if (blockFace == BlockFace.TOP) {
            return Block.REDSTONE_TORCH.withProperty("lit","true").withHandler(placementState.block().handler());
        }
        return Block.REDSTONE_WALL_TORCH
                .withProperty("facing", blockFace.toDirection().name().toLowerCase())
                .withProperty("lit","true")
                .withHandler(placementState.block().handler());
    }
}
