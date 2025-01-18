package com.github.lordtgm.rri;

import com.github.lordtgm.rri.units.RedstoneTorchUnit;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedstoneUnit {

    static final HashMap<Class<? extends RedstoneUnit>, Pair<Function<Block, Boolean>, Function<Location, RedstoneUnit>>> registry = new HashMap<>();
    static final HashMap<Location, RedstoneUnit> locations = new HashMap<>();
    final Location location;

    public static void register(Class<? extends RedstoneUnit> type, Pair<Function<Block, Boolean>, Function<Location, RedstoneUnit>> function) {
        registry.put(type, function);
    }

    public RedstoneUnit(Location location) {
        this.location = location;
        if (this.getClass() != RedstoneUnit.class) {
            locations.put(location, this);
        } else {
            locations.remove(location);
        }
    }


    public Location getLocation() {
        return location;
    }

    public static RedstoneUnit updateRedstoneUnit(Location location) {
        if (!locations.containsKey(location)
                ||
                !Optional.ofNullable(
                        registry.getOrDefault(locations.get(location).getClass(), null)
                ).map(
                        functionFunctionPair -> functionFunctionPair.left().apply(location.getBlock())
                ).orElse(false)
        ) {

            return registry.values().stream()
                    .filter(pair -> pair.left().apply(location.getBlock()))
                    .findFirst()
                    .map(Pair::right)
                    .orElse(RedstoneUnit::new)
                    .apply(location);
        }
        return locations.get(location);
    }

    public static RedstoneUnit getRedstoneUnit(Location location) {
        if (!locations.containsKey(location)) {
            return updateRedstoneUnit(location);
        }
        return locations.get(location);
    }

    public static void removeRedstoneUnit(Location location) {
        locations.remove(location);
    }

    public byte getStrongPowerLevel() {
        return isSolid() ? location.getBlock().getTag(RedstoneTags.StrongPowerLevel) : 0;
    }

    public byte getWeakPowerLevel() {
        return isSolid() ? location.getBlock().getTag(RedstoneTags.WeakPowerLevel) : 0;
    }

    public void setStrongPowerLevel(byte strongPowerLevel) {
        location.setBlock(block -> block.withTag(RedstoneTags.StrongPowerLevel, strongPowerLevel));
    }

    public void setWeakPowerLevel(byte weakPowerLevel) {
        location.setBlock(block -> block.withTag(RedstoneTags.WeakPowerLevel, weakPowerLevel));
    }

    public boolean isSolid() {
        return location.getBlock().isSolid();
    }

    /**
     * get the power level for this block.
     * always zero for non-solid blocks.
     *
     * @param allowWeak if weak power should be used as well. only false for redstone wire.
     * @return the power level of this block.
     */
    public byte getPowerLevel(boolean allowWeak) {
        return
                allowWeak ?
                        (byte) Math.max(getStrongPowerLevel(), getWeakPowerLevel()) :
                        getStrongPowerLevel();
    }

    /**
     * get the power delivered by this block.
     * always zero for non-component blocks.
     * target block might be any block, not just adjacent or neighbour blocks.
     *
     * @param target the block which this block is delivering power to.
     * @return the amount and strength of delivered power.
     */
    public Pair<Byte, Boolean> getPowerDelivery(Location target) {
        return Pair.of((byte) 0, true);
    }

    /**
     * collect the power delivered for nearby blocks.
     * this is meant to be used only for solid blocks.
     *
     * @return a pair of strong / weak power level.
     */
    public @NotNull Pair<Byte, Byte> collectPower() {
        return forEachNeighbour(
                neighbour -> RedstoneUnit.getRedstoneUnit(neighbour).getPowerDelivery(location)
        )
                .stream()
                .reduce
                        (
                                Pair.of((byte) 0, (byte) 0),
                                (byteBytePair, byteBooleanPair) ->
                                        (
                                                byteBooleanPair.right() ?
                                                        Pair.of((byte) Math.max(byteBytePair.left(), byteBooleanPair.left()), byteBytePair.right()) :
                                                        Pair.of(byteBytePair.left(), (byte) Math.max(byteBytePair.right(), byteBooleanPair.left()))
                                        ),
                                (byteBytePair1, byteBytePair2) -> Pair.of(
                                        (byte) (byteBytePair1.left() + byteBytePair2.left()),
                                        (byte) (byteBytePair1.right() + byteBytePair2.right())
                                )
                        );
    }

    public boolean update() {
        return isSolid() && updateSolid();
    }

    public boolean updateSolid() {

        Pair<Byte, Byte> powerLevels = collectPower();

        if (getStrongPowerLevel() != powerLevels.left()) {
            setStrongPowerLevel(powerLevels.left());
        } else if (getWeakPowerLevel() != powerLevels.right()) {
            setWeakPowerLevel(powerLevels.right());
        } else {
            return false;
        }
        return true;
    }

    public void postUpdate() {

    }

    public Point[] getNeighbours() {
        return new Point[]{
                new Vec(-1, 0, 0),
                new Vec(1, 0, 0),
                new Vec(0, -1, 0),
                new Vec(0, 1, 0),
                new Vec(0, 0, -1),
                new Vec(0, 0, 1),
        };
    }

    public List<Direction> getConnectionPoints() {
        return List.of(new Direction[0]);
    }

    public <T> List<T> forEachNeighbour(Function<Location, T> function) {
        return Arrays.stream(getNeighbours()).map(point -> function.apply(location.relative(point))).toList();
    }

    public void updateAndNotify() {
        if (update()) {
            postUpdate();
            notifyNeighbours();
        }
    }

    public void notifyNeighbours() {
        forEachNeighbour(neighbour -> {
            getRedstoneUnit(neighbour).updateAndNotify();
            return null;
        });

    }

    public void delayedUpdateAndNotify(int delay, Runnable function) {
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (RedstoneUnit.getRedstoneUnit(getLocation()) != this) return;
            function.run();
            notifyNeighbours();
        }, TaskSchedule.tick(delay * 2), TaskSchedule.stop());
    }

    public void tick() {

    }

    public boolean isTickable() {
        return false;
    }

}
