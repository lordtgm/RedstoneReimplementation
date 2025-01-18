package com.github.lordtgm.rri.units;

import com.github.lordtgm.rri.Location;
import com.github.lordtgm.rri.RedstoneUnit;
import net.minestom.server.utils.Direction;

import java.util.List;

public class RedstoneBlockUnit extends RedstoneUnit {
    public RedstoneBlockUnit(Location location) {
        super(location);
        setStrongPowerLevel((byte) 15);
    }

    @Override
    public List<Direction> getConnectionPoints() {
        return List.of(Direction.HORIZONTAL);
    }

    @Override
    public boolean updateSolid() {
        return false;
    }
}
