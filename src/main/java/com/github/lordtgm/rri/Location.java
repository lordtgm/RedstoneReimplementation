package com.github.lordtgm.rri;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.function.Function;

public record Location(Instance instance, Point point) {

    public Block getBlock() {
        return instance().getBlock(point);
    }

    public void setBlock(Block block) {
        instance().setBlock(point, block);
    }

    public void setBlock(Function<Block, Block> function) {
        instance().setBlock(point, function.apply(instance().getBlock(point)));
    }

    public Location relative(Point point) {
        return new Location(instance(), point().add(point));
    }

    public Location withInstance(Instance instance) {
        return new Location(instance(), point());
    }

    public Location withPoint(Point point) {
        return new Location(instance(), point);
    }

    public Location withPoint(Function<Point, Point> function) {
        return new Location(instance(), function.apply(point()));
    }
}
