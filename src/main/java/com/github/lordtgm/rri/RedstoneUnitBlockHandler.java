package com.github.lordtgm.rri;

import com.github.lordtgm.rri.units.RedstoneBlockUnit;
import com.github.lordtgm.rri.units.RedstoneDustUnit;
import com.github.lordtgm.rri.units.RedstoneTorchUnit;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RedstoneUnitBlockHandler implements BlockHandler {

    public static HashMap<Block, RedstoneUnitBlockHandler> blockHandlers = new HashMap<>();

    static {
        RedstoneUnit.register(RedstoneBlockUnit.class, Pair.of(block -> Utils.sameBlockType(block, Block.REDSTONE_BLOCK), RedstoneBlockUnit::new));
        RedstoneUnit.register(RedstoneDustUnit.class, Pair.of(block -> Utils.sameBlockType(block, Block.REDSTONE_WIRE), RedstoneDustUnit::new));
        RedstoneUnit.register(RedstoneTorchUnit.class, Pair.of(
                block -> Utils.sameBlockType(block, Block.REDSTONE_TORCH) || Utils.sameBlockType(block, Block.REDSTONE_WALL_TORCH)
                , RedstoneTorchUnit::new));
        for (Block block : new Block[]{
                Block.REDSTONE_WIRE,
                Block.REDSTONE_BLOCK,
                Block.REDSTONE_TORCH,
                Block.REDSTONE_WALL_TORCH
        }) {
            blockHandlers.put(block, new RedstoneUnitBlockHandler());
        }
    }

    public static void register() {
        blockHandlers.values().forEach(
                blockHandler -> MinecraftServer.getBlockManager().registerHandler(
                        blockHandler.getNamespaceId(), () -> blockHandler
                )
        );
    }

    @Override
    public void onPlace(@NotNull BlockHandler.Placement placement) {
        Location location = new Location(placement.getInstance(), placement.getBlockPosition());
        RedstoneUnit newUnit = RedstoneUnit.updateRedstoneUnit(location);
        newUnit.update();
        newUnit.notifyNeighbours();
    }

    @Override
    public void onDestroy(@NotNull BlockHandler.Destroy destroy) {
        RedstoneUnit oldUnit = RedstoneUnit.getRedstoneUnit(new Location(destroy.getInstance(), destroy.getBlockPosition()));
        RedstoneUnit.updateRedstoneUnit(new Location(destroy.getInstance(), destroy.getBlockPosition()));
        oldUnit.notifyNeighbours();
    }

    @Override
    public void tick(@NotNull BlockHandler.Tick tick) {
        RedstoneUnit.getRedstoneUnit(new Location(tick.getInstance(), tick.getBlockPosition())).tick();
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("minecraft:redstone_unit");
    }
}
