package net.hardat.chunkdeletecannon.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public final class ChunkDeletionScheduler {
    private static final int BLOCKS_PER_TICK = 4096;
    private static final Queue<DeletionTask> TASKS = new ArrayDeque<>();
    private static final Set<String> QUEUED_KEYS = new HashSet<>();
    private static boolean registered = false;

    private ChunkDeletionScheduler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(ChunkDeletionScheduler::tick);
    }

    public static boolean enqueue(ServerLevel level, ChunkPos chunkPos, ServerPlayer owner) {
        String key = key(level.dimension(), chunkPos);
        if (!QUEUED_KEYS.add(key)) {
            return false;
        }

        TASKS.add(new DeletionTask(level.dimension(), chunkPos, owner.getUUID(), key));
        return true;
    }

    private static void tick(MinecraftServer server) {
        int processed = 0;

        while (processed < BLOCKS_PER_TICK && !TASKS.isEmpty()) {
            DeletionTask task = TASKS.peek();
            ServerLevel level = server.getLevel(task.dimension());

            if (level == null) {
                finish(server, Objects.requireNonNull(TASKS.poll()), "Skipped chunk deletion because the dimension is no longer loaded.");
                continue;
            }

            int minY = level.getMinY();
            int height = level.getHeight();
            int totalBlocks = 16 * 16 * height;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            while (processed < BLOCKS_PER_TICK && task.nextIndex() < totalBlocks) {
                int index = task.nextIndex();
                task.advance();

                int localX = index & 15;
                int localZ = (index >> 4) & 15;
                int y = minY + (index >> 8);

                mutablePos.set(task.chunkPos().getMinBlockX() + localX, y, task.chunkPos().getMinBlockZ() + localZ);
                if (!level.isEmptyBlock(mutablePos)) {
                    level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 3);
                }

                processed++;
            }

            if (task.nextIndex() >= totalBlocks) {
                finish(server, Objects.requireNonNull(TASKS.poll()), "Chunk deletion complete.");
            }
        }
    }

    private static void finish(MinecraftServer server, DeletionTask task, String message) {
        QUEUED_KEYS.remove(task.queueKey());
        ServerPlayer owner = server.getPlayerList().getPlayer(task.ownerUuid());
        if (owner != null) {
            owner.displayClientMessage(Component.literal(message), true);
        }
    }

    private static String key(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        return dimension.identifier() + ":" + chunkPos.x + "," + chunkPos.z;
    }

    private static final class DeletionTask {
        private final ResourceKey<Level> dimension;
        private final ChunkPos chunkPos;
        private final UUID ownerUuid;
        private final String queueKey;
        private int nextIndex;

        private DeletionTask(ResourceKey<Level> dimension, ChunkPos chunkPos, UUID ownerUuid, String queueKey) {
            this.dimension = dimension;
            this.chunkPos = chunkPos;
            this.ownerUuid = ownerUuid;
            this.queueKey = queueKey;
            this.nextIndex = 0;
        }

        private ResourceKey<Level> dimension() {
            return dimension;
        }

        private ChunkPos chunkPos() {
            return chunkPos;
        }

        private UUID ownerUuid() {
            return ownerUuid;
        }

        private String queueKey() {
            return queueKey;
        }

        private int nextIndex() {
            return nextIndex;
        }

        private void advance() {
            nextIndex++;
        }
    }
}
