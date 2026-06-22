# Chunk Delete Cannon

A Fabric 1.21.11 mod that adds a one-use **Chunk Delete Rod**.

## What it does

- Adds `chunkdeletecannon:chunk_delete_rod`, a fishing-rod-style item with max durability 1.
- Any player can use the rod if they have it.
- Right-click while aiming at a block within 128 blocks to queue that block's chunk for deletion.
- The deletion is server-side and processed in batches of 4,096 blocks per server tick to reduce lag spikes.
- The rod is consumed after one successful survival-mode use.
- The mod sets chunk blocks to air; it does **not** delete region files, corrupt worlds, crash users, or do a chunk-ban exploit.

Back up your world before using it. This is intentionally destructive.

Modrith mod: https://modrinth.com/mod/orbital-chunk-delete-cannon

## Admin command

```mcfunction
/chunkrod give
/chunkrod give <targets>
```

The command requires permission level 2, so normal players cannot create rods through commands unless they are opped or have equivalent permission.

## Survival recipe

Shaped crafting recipe:

<img width="871" height="297" alt="image" src="https://github.com/user-attachments/assets/8f8f46ce-c576-40cc-ae48-78354a1a1673" />

- `E` = Echo Shard
- `T` = TNT
- `F` = Fishing Rod
- `N` = Nether Star

This makes the item survival-obtainable, but expensive enough that one chunk delete is hard to do.

## Compatibility target

- Minecraft: `1.21.11`
- Fabric Loader: `>=0.19.3`
- Fabric API: `0.141.4+1.21.11`
- Java: `21`
- Mappings: official Mojang mappings through Fabric Loom remap plugin

## Build

```bash
./gradlew build
```

The built jar should appear in `build/libs/`.

If your Gradle setup cannot resolve `net.fabricmc.fabric-loom-remap`, replace the plugin id in `build.gradle` with the legacy Fabric plugin id:

```gradle
id 'fabric-loom' version "${loom_version}"
```

## Install

For a dedicated server, put the built jar and Fabric API in the server's `mods/` folder. Players should also install the jar client-side so the item name/model show correctly.

For singleplayer, put the built jar and Fabric API in the client's `mods/` folder.

## Test checklist

I could not run a real Fabric 1.21.11 dedicated server or production client inside this sandbox, so do these before submitting:

1. Run `./gradlew build` outside the development environment.
2. Start a clean Fabric 1.21.11 dedicated server with only Fabric API and this mod.
3. Join with a Fabric 1.21.11 client that has Fabric API and this mod.
4. As an opped/admin user, run `/chunkrod give <your_name>`.
5. As a non-opped user, confirm `/chunkrod give` fails.
6. Give the rod to a non-opped user and confirm they can use it.
7. Confirm one successful survival use consumes the rod.
8. Confirm the recipe crafts the rod in survival.
9. Confirm the targeted chunk is cleared and nearby chunks are not cleared.
10. Repeat in a copied world, not your main save.

## Project hygiene

This project intentionally contains no mixins and no leftover sample classes or unrelated sample items/files.
