package com.berotech.voxellink.target;

import com.berotech.voxellink.cc.CCComputerLookup;

public record CCResolvedTarget(CCComputerLookup.ResolvedComputer computer) implements ResolvedTarget {}
