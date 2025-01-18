package com.github.lordtgm.rri.test;

import com.github.lordtgm.rri.RedstoneReimplementation;
import com.github.lordtgm.rri.RedstoneUnitBlockHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;

public class Main {
    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        RedstoneReimplementation.init(MinecraftServer.getGlobalEventHandler());
        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        DynamicRegistry.Key<DimensionType> dimensionType = MinecraftServer.getDimensionTypeRegistry()
                .register("test:overworld", DimensionType.builder().ambientLight(2.0F).build());
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(dimensionType);

        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        });

        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            instanceContainer.loadChunk(0, 0);
            instanceContainer.loadChunk(-1, 0);
            instanceContainer.loadChunk(-1, -1);
            instanceContainer.loadChunk(0, -1);

            instanceContainer.setBlock(0, 40, 0, Block.REDSTONE_BLOCK.withHandler(
                    RedstoneUnitBlockHandler.blockHandlers.get(Block.REDSTONE_BLOCK))
            );
            instanceContainer.setBlock(1, 40, 0, Block.REDSTONE_WIRE.withHandler(
                    RedstoneUnitBlockHandler.blockHandlers.get(Block.REDSTONE_WIRE))
            );
            instanceContainer.setBlock(2, 40, 0, Block.REDSTONE_WIRE.withHandler(
                    RedstoneUnitBlockHandler.blockHandlers.get(Block.REDSTONE_WIRE))
            );
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}