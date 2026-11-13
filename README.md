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

1. Use Eclipse IDE for Java Developers 2025-12 or newer with Buildship. Add JDK 25 in **Window → Preferences → Java → Installed JREs**.
2. Choose **File → Import → Gradle → Existing Gradle Project** and select this folder. In the import options, use the Gradle wrapper and select JDK 25 as the Gradle Java home.
3. Edit and save Java files in `src/main/java`, then use the Windows launcher to play your changes. If you edit build settings, right-click the project and select **Gradle → Refresh Gradle Project**.

Start in `PickleCraft.java` for initialization. Put gameplay behavior in its own classes as it grows. Game data and assets go in `src/main/resources`.

## Git sync

After the GitHub remote and branch tracking are configured, double-click **Sync PickleCraft.cmd** to pull and push in one step. Save and commit the changes you want to share first. If both sides have new commits, the fast-forward pull stops so you can resolve the divergence deliberately.

## World generation prototype

Create a new Default world to test the prototype. It uses vanilla Overworld terrain and biome distribution, with vanilla ore deposits and large veins, foliage, mob spawning, and structure generation disabled. Previously generated blocks remain in existing saves. The Overworld also rejects mobs that try to enter it, including mobs loaded from older saves.

Vanilla grid-crafting recipes are disabled in both inventories and crafting tables. The survival inventory has no crafting grid or recipe-book button. Furnace and other non-grid recipes are still available.
