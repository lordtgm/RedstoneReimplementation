package com.github.lordtgm.rri.units;

import com.github.lordtgm.rri.Location;
import com.github.lordtgm.rri.RedstoneUnit;
import com.github.lordtgm.rri.Utils;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

import java.util.Arrays;
import java.util.List;

public class RedstoneTorchUnit extends RedstoneUnit {
    public RedstoneTorchUnit(Location location) {
        super(location);
        setLit((RedstoneUnit.getRedstoneUnit(getLocation().relative(getRoot().vec())).getPowerLevel(true) == 0));
    }

    private Direction getRoot() {
        Block block = getLocation().getBlock();
        if (Utils.sameBlockType(block, Block.REDSTONE_TORCH)) {
            return Direction.DOWN;
        } else if (Utils.sameBlockType(block, Block.REDSTONE_WALL_TORCH)) {
            return Direction.valueOf(block.getProperty("facing").toUpperCase()).opposite();
        }
        throw new RuntimeException("invalid block type: " + block.namespace());
    }

    private boolean isLit() {
        return getLocation().getBlock().getProperty("lit").equals("true");
    }

    private void setLit(boolean isLit) {
        getLocation().setBlock(block -> block.withProperty("lit", String.valueOf(isLit)));
    }

    @Override
    public boolean update() {
        if ((RedstoneUnit.getRedstoneUnit
                (getLocation().relative(getRoot().vec())
                ).getPowerLevel(true) > 0)
                ==
                isLit()
        ) {
            delayedUpdateAndNotify(1, () -> {
                setLit((RedstoneUnit.getRedstoneUnit(getLocation().relative(getRoot().vec())).getPowerLevel(true) == 0));
            });
        }
        return false;
    }

    @Override
    public Pair<Byte, Boolean> getPowerDelivery(Location target) {
        return Pair.of((byte) ((isLit() &&
                (getConnectionPoints().contains(Utils.getDirectionAbsolute(getLocation().point(), target.point())))
                && (!target.getBlock().isSolid() || (target.point().sub(getLocation().point())).samePoint(0, 1, 0))
        ) ? 15 : 0), true);
    }

    @Override
    public List<Direction> getConnectionPoints() {
        Direction root = getRoot();
        return Arrays.stream(Direction.values()).filter(direction -> direction != root).toList();
    }
}
