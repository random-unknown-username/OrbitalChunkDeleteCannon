package net.hardat.chunkdeletecannon;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hardat.chunkdeletecannon.item.ChunkDeleteRodItem;
import net.hardat.chunkdeletecannon.world.ChunkDeletionScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Collection;
import java.util.List;

public final class ChunkDeleteCannonMod implements ModInitializer {
    public static final String MOD_ID = "chunkdeletecannon";
    public static final Identifier CHUNK_DELETE_ROD_ID = Identifier.fromNamespaceAndPath(MOD_ID, "chunk_delete_rod");
    public static final ResourceKey<Item> CHUNK_DELETE_ROD_KEY = ResourceKey.create(Registries.ITEM, CHUNK_DELETE_ROD_ID);

    public static final Item CHUNK_DELETE_ROD = Registry.register(
            BuiltInRegistries.ITEM,
            CHUNK_DELETE_ROD_KEY,
            new ChunkDeleteRodItem(new Item.Properties()
                    .setId(CHUNK_DELETE_ROD_KEY)
                    .durability(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant())
    );

    @Override
    public void onInitialize() {
        ChunkDeletionScheduler.register();
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("chunkrod")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("give")
                                .executes(context -> giveRod(context.getSource(), List.of(context.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> giveRod(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
        ));
    }

    private static int giveRod(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            ItemStack stack = new ItemStack(CHUNK_DELETE_ROD);
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        }

        String name = targets.size() == 1
                ? targets.iterator().next().getName().getString()
                : targets.size() + " players";
        source.sendSuccess(() -> Component.literal("Gave a Chunk Delete Rod to " + name + "."), true);
        return Command.SINGLE_SUCCESS;
    }
}
