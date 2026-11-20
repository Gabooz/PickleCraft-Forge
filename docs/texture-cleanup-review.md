# Final texture cleanup review

The scope is every PNG under the mod's two texture directories. The contact
sheets show PickleCraft on the left and the closest vanilla reference on the
right, at 3× on dark slots and 1× on light slots. Five opaque tiles were also
checked in 2 × 2 repeats. All four fire frames were checked in playback order
and across the wrap. The in-game item-art sheet checks baked models, overlays,
and heat states that use vanilla layers directly.

## Saw from the expressive draft

The post-redesign cleanup started from the saved expressive PNG. The approved
spine, tooth positions, wood and steel masks, and 3 × 3 grip hole are the
targets from the [reconstruction recipe](saw-structure.md).

| Pass | Pixel | Before / after | Reason |
| --- | --- | --- | --- |
| Expressive cleanup | (5, 11) | `#94683c` / `#68482d` | Let the lower inside of the wooden grip fall into shadow. |
| Expressive cleanup | (6, 9) | `#c6cbcb` / `#a6adb0` | Bring the heel highlight down to the adjacent steel tone so the heel recedes beside the wood. |
| Expressive cleanup | (15, 5) | `#c6cbcb` / `#a6adb0` | Give the upright nose a continuous midtone along its lower face. |
| Final comparison | (7, 12) | `#9ca7a9` / `#757f82` | Carry the cutting-edge shade onto the tooth next to the grip so it reads as steel at the shaded heel. |

Coordinates are zero-based from the upper left. The in-game vanilla review
was repeated after the final adjustment. The same 31 wood and 62 steel
pixels still satisfy the reconstruction. The derived handle and blade were
re-extracted from the approved sprite, with no hand edits.

## Item PNGs

| Texture | Vanilla comparison | Final check |
| --- | --- | --- |
| `bull_spawn_egg` | Cow spawn egg | Swept horns and coat patches maintain a clear edge and full cattle silhouette. |
| `fire_starter` | Flint and steel | The single bright ignition point sits at the spindle and board junction. |
| `firebox_flame` | Fire and fire charge | Four distinct flame tips sit above a connected bright core and stable foot. |
| `glue` | Slimeball | Its glint sits on the dome and the lower rim follows the silhouette. |
| `hot_coal` | Charcoal | Softened the abrupt step between the two amber parts of the ember seam at (8, 7), from `#574434` to `#80664c`. |
| `iron_hammer` | Iron axe | The steel planes and narrow wood grip read clearly at inventory size. |
| `iron_saw` | Shears | The final shaded tooth follows the cutting edge; the spine, assembly and hole retain their approved masks. |
| `iron_tongs` | Shears | The two jaws and hinge remain continuous and the gap is clear. |
| `iron_tongs_loaded` | Iron ingot and shears | The orange load stays within the tips. The frame exactly matches the empty tongs. |
| `iron_tongs_loaded_gold` | Gold ingot and shears | The golden load uses the same outline as the iron load and the same tongs frame. |
| `loose_stone` | Flint | Broad face and lower fracture shade read as one small material. |
| `oar` | Oak boat and stick | Blade broadens from a straight shaft. The oak ramp stays within the wood family. |
| `plumaria_blossoms` | Azure bluet and peony | The three small bloom clusters remain distinct at 1×. |
| `saw_blade` | Shears and assembled saw | Re-extracted after parent approval. Translating it back reproduces all 62 visible steel pixels. |
| `saw_handle` | Wooden axe grip and assembled saw | Re-extracted after parent approval. Translating it back reproduces all 31 wood pixels and the grip aperture. |
| `stone_hammer` | Stone axe | The gray head separates clearly from the shared wood grip. |
| `wooden_hammer` | Wooden axe | The warm head, shared grip, and dark edge remain readable. |
| `zone_map` | Empty map | The custom ink stays within the actual vanilla parchment layer. |

## Block PNGs

| Texture | Vanilla comparison | Final check |
| --- | --- | --- |
| `furnace_band` | Block of iron and exposed copper | The worn metal flecks remain inside the band and the strip reads continuously. |
| `furnace_lining` | Deepslate tiles | Dark brick and mortar stay readable together across tile boundaries. |
| `furnace_lining_hot` | Magma block and deepslate tiles | Mortar and brick registration keep the hot lining identifiable as the same chamber surface. |
| `furnace_masonry` | Furnace side and stone bricks | Cleared a rust fleck that interrupted the mortar at (3, 4), from `#9b694e` to `#37383b`. Rust on the stone faces remains. |
| `plumaria_blue` | Blue orchid | Blue petal shapes and light tips retain clear edges. |
| `plumaria_leaves` | Large fern | Layered leaf tips retain clear taper and turn. |
| `plumaria_pink` | Peony | Warm petal clusters stay connected and distinct from the dark center. |
| `plumaria_stems` | Fern | Upright stems stay connected to the leaf fan with clean cutout edges. |
| `plumaria_white` | Oxeye daisy | The pale petal tips and warm center remain legible as a single bloom. |
| `stockpile_floor` | Dirt path top | Soil grain repeats without a standout seam and stays in the path palette. |
| `unlit_torch` | Torch | All 16 shaft pixels match vanilla; the four-pixel cold cap is distinct. |
| `withered_wheat` | Mature wheat and dead bush | Dry stems and seed heads stay connected. Their muted brown ramp separates them from live wheat. |

All other source pixels passed without edits. The flame animation metadata
and registered styles stayed intact. Texture sheets and mapped uses reviewed
in this pass included the furnace and Plumaria inventory models, the torch's
shared block/item material, and the wheat crop and stockpile surface model
assignments.
