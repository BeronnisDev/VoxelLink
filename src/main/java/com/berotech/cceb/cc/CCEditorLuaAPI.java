package com.berotech.cceb.cc;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.IComputerSystem;

import net.minecraft.server.level.ServerLevel;

public final class CCEditorLuaAPI implements ILuaAPI {
    private final IComputerSystem computer;

    public CCEditorLuaAPI(IComputerSystem computer) {
        this.computer = computer;
    }

    @Override
    public String[] getNames() {
        return new String[] { "cceditor" };
    }

    @LuaFunction(mainThread = true)
    public final Object[] open(IArguments args) throws LuaException {
        if (args.count() < 1) {
            throw new LuaException("Expected a file path");
        }

        String path;
        try {
            path = CCPaths.normalize(args.getString(0));
        } catch (IllegalArgumentException exception) {
            return new Object[] { false, exception.getMessage() };
        }

        if (path.isEmpty()) {
            return new Object[] { false, "Cannot open root directory" };
        }

        ServerLevel level = computer.getLevel();
        String computerId = EditorOpenRequests.encodeComputerId(level, computer.getPosition());
        EditorOpenRequests.Result result = EditorOpenRequests.requestOpen(level, computer.getPosition(), computerId, path);
        if (result.success()) {
            return new Object[] { true };
        }
        return new Object[] { false, result.message() };
    }
}
