package com.github.lordtgm.rri;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

import java.util.Arrays;

public class Utils {
    public static Direction getDirectionRelative(Point point) {
        return Arrays.stream(Direction.values()).filter(direction -> direction.vec().samePoint(point)).findFirst().orElse(null);
    }
    public static Direction getDirectionAbsolute(Point source, Point target) {
        return getDirectionRelative(target.sub(source));
    }
    public static boolean sameBlockType(Block block1, Block block2) {
        return block1.namespace().equals(block2.namespace());
    }
}
