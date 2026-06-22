package net.hardat.chunkdeletecannon.item;

import net.hardat.chunkdeletecannon.world.ChunkDeletionScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ChunkDeleteRodItem extends FishingRodItem {
    private static final double TARGET_RANGE = 128.0D;

    public ChunkDeleteRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        BlockHitResult hit = raycast(level, user);
        if (hit.getType() != HitResult.Type.BLOCK) {
            serverPlayer.displayClientMessage(Component.literal("Aim at a block within " + (int) TARGET_RANGE + " blocks."), true);
            return InteractionResult.FAIL;
        }

        ChunkPos chunkPos = new ChunkPos(hit.getBlockPos());
        boolean queued = ChunkDeletionScheduler.enqueue(serverLevel, chunkPos, serverPlayer);
        if (!queued) {
            serverPlayer.displayClientMessage(Component.literal("That chunk is already being deleted."), true);
            return InteractionResult.FAIL;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }

        serverPlayer.displayClientMessage(Component.literal("Queued chunk deletion at chunk " + chunkPos.x + ", " + chunkPos.z + "."), true);
        return InteractionResult.SUCCESS;
    }

    private static BlockHitResult raycast(Level level, Player user) {
        Vec3 start = user.getEyePosition();
        Vec3 end = start.add(user.getViewVector(1.0F).scale(TARGET_RANGE));
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
    }
}
