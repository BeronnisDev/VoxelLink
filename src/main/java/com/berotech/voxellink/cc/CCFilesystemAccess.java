package com.berotech.voxellink.cc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.WritableMount;

import net.minecraft.server.MinecraftServer;

public final class CCFilesystemAccess {
    private static final Set<OpenOption> WRITE_OPTIONS = Set.of(
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
    );

    private CCFilesystemAccess() {}

    public static List<String> listFiles(MinecraftServer server, CCComputerLookup.ResolvedComputer computer, String path) throws IOException {
        WritableMount mount = openMount(server, computer);
        String normalizedPath = CCPaths.normalize(path);
        if (!normalizedPath.isEmpty() && !mount.exists(normalizedPath)) {
            throw new IOException("No such directory: " + path);
        }
        if (!normalizedPath.isEmpty() && !mount.isDirectory(normalizedPath)) {
            throw new IOException("Not a directory: " + path);
        }

        List<String> names = new ArrayList<>();
        mount.list(normalizedPath, names);

        List<String> files = new ArrayList<>(names.size());
        for (String name : names) {
            files.add(CCPaths.child(normalizedPath, name));
        }
        return files;
    }

    public static String readFile(MinecraftServer server, CCComputerLookup.ResolvedComputer computer, String path) throws IOException {
        WritableMount mount = openMount(server, computer);
        String normalizedPath = CCPaths.normalize(path);
        if (normalizedPath.isEmpty()) {
            throw new IOException("Cannot read root directory");
        }
        if (!mount.exists(normalizedPath)) {
            throw new IOException("No such file: " + path);
        }
        if (mount.isDirectory(normalizedPath)) {
            throw new IOException("Is a directory: " + path);
        }

        try (SeekableByteChannel channel = mount.openForRead(normalizedPath)) {
            long size = mount.getSize(normalizedPath);
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File too large: " + path);
            }

            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) < 0) {
                    break;
                }
            }
            return StandardCharsets.UTF_8.decode(buffer.flip()).toString();
        }
    }

    public static void writeFile(MinecraftServer server, CCComputerLookup.ResolvedComputer computer, String path, String content) throws IOException {
        WritableMount mount = openMount(server, computer);
        String normalizedPath = CCPaths.normalize(path);
        if (normalizedPath.isEmpty()) {
            throw new IOException("Cannot write root directory");
        }
        if (mount.exists(normalizedPath)) {
            if (mount.isDirectory(normalizedPath)) {
                throw new IOException("Is a directory: " + path);
            }
            if (mount.isReadOnly(normalizedPath)) {
                throw new IOException("File is read-only: " + path);
            }
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (SeekableByteChannel channel = mount.openFile(normalizedPath, WRITE_OPTIONS)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }
    }

    public static void deleteFile(MinecraftServer server, CCComputerLookup.ResolvedComputer computer, String path) throws IOException {
        WritableMount mount = openMount(server, computer);
        String normalizedPath = CCPaths.normalize(path);
        if (normalizedPath.isEmpty()) {
            throw new IOException("Cannot delete root directory");
        }
        if (!mount.exists(normalizedPath)) {
            throw new IOException("No such file: " + path);
        }
        if (mount.isReadOnly(normalizedPath)) {
            throw new IOException("File is read-only: " + path);
        }

        mount.delete(normalizedPath);
    }

    private static WritableMount openMount(MinecraftServer server, CCComputerLookup.ResolvedComputer computer) {
        return ComputerCraftAPI.createSaveDirMount(server, computer.saveSubPath(), computer.storageCapacity());
    }
}
