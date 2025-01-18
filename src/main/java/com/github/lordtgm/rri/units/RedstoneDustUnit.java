package com.github.lordtgm.rri.units;

import com.github.lordtgm.rri.Location;
import com.github.lordtgm.rri.RedstoneUnit;
import com.github.lordtgm.rri.Utils;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class RedstoneDustUnit extends RedstoneUnit {

    public static Tag<Byte> Power = Tag.Byte("power").defaultValue((byte) 0);

    public RedstoneDustUnit(Location location) {
        super(location);
    }

    @Override
    public Point[] getNeighbours() {
        return new Point[]{
                new Vec(-1, -1, 0),
                new Vec(-1, 0, 0),
                new Vec(-1, 1, 0),
                new Vec(1, -1, 0),
                new Vec(1, 0, 0),
                new Vec(1, 1, 0),
                new Vec(0, -1, 0),
                new Vec(0, 1, 0),
                new Vec(0, -1, -1),
                new Vec(0, 0, -1),
                new Vec(0, 1, -1),
                new Vec(0, -1, 1),
                new Vec(0, 0, 1),
                new Vec(0, 1, 1),
        };
    }

    /**
     * If this redstone wire is connected to a target point
     *
     * @param neighbour The point to check the connection to
     * @return If this redstone wire is connected to the point.
     */
    private boolean isConnected(Location neighbour) {
        if (Math.abs(getLocation().point().x() - neighbour.point().x()) > 1
                ||
                Math.abs(getLocation().point().y() - neighbour.point().y()) > 1
                ||
                Math.abs(getLocation().point().z() - neighbour.point().z()) > 1
        ) return false;
        if (getLocation().point().x() - neighbour.point().x() == 0 && getLocation().point().z() - neighbour.point().z() == 0) {
            return true;
        }
        if (getLocation().point().y() - neighbour.point().y() == 1
                && neighbour.relative(new Pos(0, 1, 0)).getBlock().isSolid()
                && !neighbour.relative(new Pos(0, 1, 0)).point().samePoint(getLocation().point())
        ) return false;
        if (getLocation().point().y() - neighbour.point().y() != 0) {
            if (!Utils.sameBlockType(neighbour.getBlock(), Block.REDSTONE_WIRE)) return false;
        }
        return !Optional.ofNullable(Utils.getDirectionAbsolute(getLocation().point(), neighbour.point().withY(getLocation().point().y()))).
                map(direction -> getLocation().getBlock().getProperty(direction.name().toLowerCase(Locale.ROOT))).
                orElse("none").
                equals("none");
    }

    @Override
    public boolean update() {
        Map<Direction, String> connections = new HashMap<>();
        for (Direction direction : Direction.HORIZONTAL) {
            for (int y = -1; y <= 1; y++) {
                if (Utils.sameBlockType(
                        getLocation().relative(direction.vec().add(0, y, 0)).getBlock(),
                        Block.REDSTONE_WIRE
                )) {
                    if (y == 1 && !getLocation().relative(new Vec(0, 1, 0)).getBlock().isSolid()) {
                        connections.put(direction, "up");
                    } else if (y == 0 || !getLocation().relative(direction.vec()).getBlock().isSolid()) {
                        connections.put(direction, "side");
                    }
                }
            }
            if (RedstoneUnit.getRedstoneUnit(getLocation().relative(direction.vec())).getConnectionPoints()
                    .contains(direction.opposite())
            ) {
                connections.put(direction, "side");
            }
        }
        if (connections.size() == 1) {
            connections.put(connections.keySet().iterator().next().opposite(), "side");
        }
        getLocation().setBlock(block -> block.withProperties(
                Arrays.stream(Direction.HORIZONTAL).map(
                        direction -> Map.entry(
                                direction.name().toLowerCase(Locale.ROOT),
                                connections.getOrDefault(direction, "none")
                        )
                ).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ))
        ));

        byte powerLevel = forEachNeighbour(
                neighbour -> {
                    RedstoneUnit redstoneunit = RedstoneUnit.getRedstoneUnit(neighbour);
                    return redstoneunit.isSolid()
                            ? redstoneunit.getPowerLevel(false)
                            : redstoneunit.getPowerDelivery(getLocation()).left();
                }
        )
                .stream()
                .reduce(
                        (aByte1, aByte2) -> (byte) Math.max(aByte1, aByte2)
                )
                .orElse((byte) 0);
        if (!Objects.equals(powerLevel, getLocation().getBlock().getTag(Power))) {
            getLocation().setBlock(block -> block.withTag(Power, powerLevel).withProperty("power", String.valueOf(powerLevel)));
            return true;
        }
        return false;
    }

    @Override
    public Pair<Byte, Boolean> getPowerDelivery(Location target) {
        byte power = getLocation().getBlock().getTag(Power);
        if (Utils.sameBlockType(target.getBlock(), Block.REDSTONE_WIRE)) {
            power -= 1;
        }
        if (!isConnected(target)) {
            power = 0;
        }
        return Pair.of(power, false);
    }
}
