package com.github.lordtgm.rri;

import com.github.lordtgm.rri.placement.RedstoneTorchPlacementRule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RedstoneReimplementation {
    public static void init(@NotNull EventNode<? super PlayerEvent> eventNode) {
        RedstoneUnitBlockHandler.register();
        eventNode.addListener(PlayerBlockPlaceEvent.class, playerBlockPlaceEvent -> {
            if (RedstoneUnitBlockHandler.blockHandlers.containsKey(playerBlockPlaceEvent.getBlock())) {
                playerBlockPlaceEvent.setBlock(playerBlockPlaceEvent.getBlock().withHandler(
                        RedstoneUnitBlockHandler.blockHandlers.get(playerBlockPlaceEvent.getBlock())
                ));
            } else {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    RedstoneUnit unit = RedstoneUnit.getRedstoneUnit(new Location(playerBlockPlaceEvent.getInstance(), playerBlockPlaceEvent.getBlockPosition()));
                    unit.update();
                    unit.notifyNeighbours();
                });
            }
        });
        eventNode.addListener(PlayerBlockBreakEvent.class,playerBlockBreakEvent -> {
            RedstoneUnit unit = RedstoneUnit.getRedstoneUnit(new Location(playerBlockBreakEvent.getInstance(), playerBlockBreakEvent.getBlockPosition()));
            MinecraftServer.getSchedulerManager().scheduleNextTick(unit::notifyNeighbours);
        });
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new RedstoneTorchPlacementRule());
    }
}
