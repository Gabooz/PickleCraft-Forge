# PickleCraft item sprite rework

All 32 PickleCraft item definitions have dedicated native-resolution sprites.
The firebox uses four 16 × 16 frames. Placed workstations retain their block
models, and the lit and unlit torch block textures are separate from their
inventory portraits.

| Family | What it must communicate | Reference cues used | Pixel construction and checks |
| --- | --- | --- | --- |
| Saw, plate, and grip | A straight toothed plate seated in a closed wooden grip | Forge Multipart iron saw and Minecraft shears for the metal plane and material separation | Keep the reviewed blade and grip geometry. Test the saw axis, grip width, and tooth gaps. Derive the two components from the assembled parts. |
| Wood, stone, and iron hammers | The same symmetric head and centered diagonal joint in three materials | Legacy Tinkers’ Construct hammer handle and Minecraft wooden axe for the shaft | Reuse the same checked outline in all three versions. Change the head ramp while preserving the handle and joint. |
| Tongs and both loads | Two steel jaws on a common hinge. The load sits within the jaw tips | Minecraft shears for separated rails and the ingots for hot material cues | Preserve the grip silhouette across the three states. Put the load only near the tips and distinguish iron orange from gold yellow. |
| Oar and fire starter | A long shaft ending in a broad paddle. A spindle seated in a fireboard | Minecraft oak boat and flint and steel for wood and ignition cues | Use a diagonal paddle with a visibly broader tip. Seat the spindle against a stable board and put the ember at the contact point. |
| Glue and loose stone | Pale adhesive with a soft lobed silhouette. An irregular solid stone | Minecraft slimeball and flint | A grouped cream highlight indicates glue. A more angular outline and cool stone ramp distinguish the pebble. |
| Plumaria and blossoms | Distinct blue, pink, and pale blooms. The plant retains more green mass | Minecraft flower pot and flower assets for compact color clusters | Separate each bloom and keep it connected to the green body where possible. Give the plant and harvested material different silhouettes. |
| Hot iron, gold, ores, coal, and flame | Flat bars, rough chunks, ember flecks, and rising flames | Minecraft raw metals, ingots, charcoal, and fire | Share the bar mask across ingot phases. Preserve the neutral iron ore texture for the dynamic heat tint. Keep four flame frames at the existing timing. |
| Anvil, workbench, carpenter’s table, barrel, furnace | The silhouettes and openings that distinguish the placed stations | Minecraft anvil and furnace cues plus PickleCraft’s own placed models | Give the anvil a waist, the workbench a log face, the carpenter’s table visible legs, the barrel an open rim, and the furnace a dark chamber. |
| Zone map and torches | Colored regions on a parchment sheet. A diagonal torch with lit or charred tip | Minecraft map, compass, and torch | Use linked color regions inside the parchment. Keep the same shaft for both torch states and vary only the tip and flame. |
| Cattle spawn eggs | A matching pair whose patterns distinguish cow and bull | Minecraft spawn egg proportions and PickleCraft’s own herd appearance | Use a shared egg profile with separate spotted and warm brown markings. |

Historical sources inspected include the [Forge Multipart saw](https://github.com/Chicken-Bones/ForgeMultipart/blob/master/resources/assets/microblock/textures/items/sawIron.png) and the [Tinkers’ Construct wooden hammer handle](https://github.com/benjamin-pilgrim/tconstruct/blob/master/resources/assets/tinker/textures/items/hammer/wood_hammer_handle.png). Vanilla references were inspected in the installed client archive. The exported pixels are original PickleCraft sprites.
