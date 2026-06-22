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

## Admin command

```mcfunction
/chunkrod give
/chunkrod give <targets>
```

The command requires permission level 2, so normal players cannot create rods through commands unless they are opped or have equivalent permission.

## Survival recipe

Shaped crafting recipe:

<img width="871" height="297" alt="image" src="https://github.com/user-attachments/assets/50a2b605-b541-455a-bae3-0a460b5a4cc9" />

- `E` = Echo Shard
- `T` = TNT
- `F` = Fishing Rod
- `N` = Nether Star

This makes the item survival-obtainable, but expensive enough that one chunk delete is hard.

## Requirements

- Fabric 19.3+
- Only works on 1.21.11 (for now)

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

## Arch Linux build fix

If system Gradle fails with `repository 'LoomLocalRemappedMods' was added by plugin`, make sure `settings.gradle` does **not** contain:

```gradle
repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
```

Fabric Loom adds required repositories during setup, so this project leaves that strict repository mode disabled.

