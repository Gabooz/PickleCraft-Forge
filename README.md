# PickleCraft

PickleCraft is a standalone Minecraft Forge mod project for Minecraft Java Edition 26.3. It takes inspiration from Botania in FTB Infinity Evolved, Stardew Valley, and Terraria. The current build is the development foundation for a rework of vanilla survival.

## Design rules

1. Make interactions happen in the world by default. Keep ordinary Minecraft screens such as the inventory, and justify any new screen by what it lets the player do.
2. Give each tool a reason to stay useful. Use situational limits, costs, and tradeoffs so one tool does not obsolete all the others.
3. Replace repetitive work with interesting decisions. When a loop gets boring, change the loop and test it in play.

## Play the development build

Use JDK 25. Set `JAVA_HOME` to the JDK directory, or create a local `local-jdk.cmd` containing `@set "JAVA_HOME=C:\path\to\jdk-25"`. Double-click **Play PickleCraft.cmd**. It builds from the current saved source and launches the Forge development client. The first run downloads Gradle, Minecraft, and dependencies. Test worlds and logs live under `run/`.

To build a distributable jar, run `.\gradlew.bat build` from a terminal using JDK 25. The jar is `build/libs/PickleCraft-0.1.0.jar`. Use it with Minecraft Forge 66.1.0 for Minecraft 26.3.

## Open in Eclipse

1. Use Eclipse IDE for Java Developers 2025-12 or newer with Buildship. Add JDK 25 in **Window â†’ Preferences â†’ Java â†’ Installed JREs**.
2. Choose **File â†’ Import â†’ Gradle â†’ Existing Gradle Project** and select this folder. In the import options, use the Gradle wrapper and select JDK 25 as the Gradle Java home.
3. Edit and save Java files in `src/main/java`, then use the Windows launcher to play your changes. If you edit build settings, right-click the project and select **Gradle â†’ Refresh Gradle Project**.

Start in `PickleCraft.java` for initialization. Put gameplay behavior in its own classes as it grows. Game data and assets go in `src/main/resources`.

## Git sync

After the GitHub remote and branch tracking are configured, double-click **Sync PickleCraft.cmd** to pull and push in one step. Save and commit the changes you want to share first. If both sides have new commits, the fast-forward pull stops so you can resolve the divergence deliberately.

## World generation prototype

Create a new PickleCraft world to test the prototype. It uses the hex-zone generation described below, including mine zones for iron and gold. Vanilla foliage, mob spawning, structures, and snow and ice generation are disabled. Sparse oaks appear in grasslands, while oak forests have more trees. Previously generated blocks remain in existing saves. The Overworld rejects mobs unless a registered biome challenge explicitly allows them.

Vanilla grid-crafting recipes are disabled in both inventories and crafting tables. The survival inventory has no crafting grid or recipe-book button. Furnace and other non-grid recipes are still available.

The development client uses the wide Steve model. With an empty main hand, logs take two minutes, dirt takes 30 seconds, stone and deepslate take five minutes, and sand takes 15 seconds. Other breakable blocks with positive hardness take about a minute. Leaves and instant-break blocks keep their normal behavior.

To run the automated playtest, use JDK 25 and run `.\gradlew.bat runClient -PpicklecraftSmokeTest`. The test creates a fresh Survival world under `build/playtest`, checks the inventory, Steve skin, mining rates, recipes, and Overworld settings, then exits on success. See `build/playtest/logs/latest.log`.

## Ground materials and Plumaria

Right-click the top of a solid block with a stick, string, or loose stone to place it. Add up to sixteen pieces. Right-click an exact stack of one of each with an empty hand to assemble a stone pickaxe. Right-click the tool to cycle through pickaxe, sword, shovel, and axe. Shift-right-click takes the top piece or the finished tool. Hitting a stack drops its contents.

In new Overworld chunks, look on grass for loose stones, sticks, and occasional Plumaria plants or pairs. Sand has only widely scattered stones and sticks. Right-click the flowering plant with an empty hand to collect blossoms. The leaves stay and the plant flowers again after seven Overworld days. Place blossoms on solid ground and Shift-right-click them for one string, or shear the blossoms on a Workbench for two string.

The Overworld clock runs at quarter speed, giving an 80-minute cycle at 20 ticks per second. Night light varies with the moon. Full-moon sky and ambient light are 30 percent brighter than at new moon during the darkest part of the night.

The PickleCraft creative tab contains the Workbench, loose stone, placeable Plumaria, blossoms, stick, string, and all four stone tools. For natural spawns, explore newly generated chunks or create a fresh PickleCraft world.

The Workbench accepts one input item at a time. Multi-item outputs stack on its surface. Take the output before adding another input.

Keep the block above a Workbench clear to use it.

## Iron Furnace

Stack two cobblestone blocks vertically. Shift-right-click the lower block with a pickaxe to carve the furnace. Use one coal or charcoal on the lower opening and drop one raw iron, iron ore, or deepslate iron ore through the top. Shift-right-click the lower half with an empty hand to reclaim fuel before smelting begins.

The ingot appears after 55 seconds. The fire goes out after one minute, and cooling takes another five minutes. Right-click the lower half with an empty hand to retrieve the ingot. Handling it while hot deals severe fire damage.


## PickleCraft world generation

New worlds default to the **PickleCraft** world type. Its pointy hex regions have
roughly 1,071-block sides, with roughly 100 blocks of broad boundary variation and finer
edge detail. Grasslands and oak forests cluster, mountain cells follow broad
ridges, and river biomes still follow the vanilla climate sampler. Use a new
PickleCraft world to try this generation.

The layout and terrain live in `world/HexRegions.java`, `HexBiomeSource.java`,
and `HexTerrain.java`. World-preset and biome data are under
`data/picklecraft/worldgen`.

For future challenges, register a `BiomeChallenges.Definition` during mod setup.
It specifies eligible region kinds, selection weight, and a generation callback.
The callback receives the selected hex and runs once per generated chunk, so
hazards, resources, and structures can follow a stable region-wide choice.
An optional mob predicate lets that challenge permit its creatures through the
mod's usual mob suppression. The zombie challenge is registered for a seeded 15% subset of grassland zones.


Mine zones use a separate deterministic roll per hex. One third contain ore,
with iron selected five times as often as gold. The selected ore replaces the
normal Overworld coal, copper, iron, gold, redstone, diamond, and lapis deposits,
plus emerald deposits in mountain zones. Their vanilla placement counts, height
ranges, sizes, and air-exposure rules are preserved. Large iron/copper veins
are also converted. Ore blocks are checked against the warped zone boundary.
Newly generated chunks in PickleCraft worlds use these rules.


## Zombie grasslands

About 15% of grassland hex zones have a persistent roster of 18–25 zombies.
Tank, runner, and blind variants are selected with equal probability for each
roster member. Tanks have 45 hearts, normal zombie speed, a 10% larger body,
and 4-heart attacks. Runners have 30 hearts, baby-zombie speed, and 6-heart
attacks. Blind zombies have 35 hearts, slightly slower movement, and 7-heart
attacks. Their navigation heading varies by up to 60 degrees to either side.
Individual detection ranges and initial headings are seeded per roster member.

Zombies spawn on dark, solid, loaded ground 28–75 blocks from nearby survival
players between clock ticks 13000 and 23000. At dawn they heal and retreat from
players. Chasing them keeps them loaded and killable. Retired or unloaded
incarnations cannot duplicate roster members. Death from any cause permanently
removes the individual. The roster is saved in the Overworld's saved data.

Uncleared zones have fifteen-block fog by day. At night visibility is seven blocks,
or twelve blocks near emitted light. Night Vision removes zone fog. They also have extra
night darkness. Killing the entire roster clears those effects. Sparse bones
decorate newly generated challenge zones. Zombies reject paths through emitted
block light and seek darker ground when caught in light.


## Creative zone map

Find the **Creative Zone Map** in the PickleCraft tab and right-click it in
Creative mode. Drag to pan, scroll to zoom around the cursor, press R to center
on the player, and press F to toggle following. The white arrow tracks the
player's position and facing. Colors show the actual warped biome zones, while
the grid shows their underlying hex cells. Fe and Au mark planned iron and gold
mines, and Z marks a zombie challenge. Hover for zone details. The map previews
generation without loading chunks.

Newly generated terrain has lowlands around sea level and lower mountain
ranges. Existing generated terrain keeps its saved shape.


## Cattle

Five percent of grassland zones receive one founding herd of two or three animals,
including exactly one bull. The orange/tan bull has larger horns and a slightly
larger body. Cows and bulls are the only peaceful animals that spawn naturally.

Breed a cow with a bull using wheat. Each calf inherits the rounded-up average of
its parents' seven stats. Each stat has a five-percent total chance to mutate by
plus or minus one, bounded to 0–50. All founder stats are 25. Stats are stored but
have no gameplay effects yet.

Calves mature after fifteen Overworld clock days and follow their own mother.
A mother can nurture one calf within twelve blocks. Additional calves, or calves
separated for a full day, take half a heart of damage per second until reunited
or dead. Mothers cannot breed while any of their calves are still immature.
Adult cows can be milked once per clock day. Adult cattle drop 25–35 raw beef and
7–12 leather. Cow and bull spawn eggs are available in the PickleCraft creative tab.
