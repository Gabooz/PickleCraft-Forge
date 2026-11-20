# Individual item art review

Each row covers the five steps in order. The 3D choices use the corresponding
placed model where possible. State sprites use vanilla layers by resource ID,
so there are no duplicate ingot or ore silhouettes to keep in sync.

| Item | 1. Medium | 2. Exact match or closest family | 3. Required cues | 4. Reference inspected | 5. Construction and review check |
| --- | --- | --- | --- | --- | --- |
| Anvil | 3D, shape carries depth | Same placed anvil | Horn, waist, stepped foot | Vanilla anvil and PickleCraft blockstate | Point to the exact placed model. Check the waist and foot in the GUI. |
| Open barrel | 3D, opening is essential | Same placed open barrel | Visible rim, cavity, and bottom | Vanilla barrel materials and PickleCraft barrel | Use the exact hollow barrel model. Check that the inventory angle exposes the opening. |
| Workbench | 3D, a work surface above a log | Same placed workbench | Top and log body | PickleCraft workbench and vanilla log end grain | Use the exact block model. The GUI should show the surface and at least one side. |
| Carpenter’s table | 3D, two blocks of furniture | Both placed halves | Long top, legs, side implements | Both PickleCraft table models and vanilla oak/spruce texture treatment | Compose both halves into a centered 32-unit model. Scale GUI and hand transforms to fit. |
| Iron furnace | 3D, two openings define the station | Existing iron furnace miniature | Narrow chute, broader body, dark mouth | PickleCraft furnace and vanilla furnace | Restore the original volume model. Check that its front opening reads at inventory size. |
| Plumaria | 3D, shows a placeable living plant | Blooming placed Plumaria | Blooms above radiating leaves | Placed model and vanilla plant inventory conventions | Use the blooming model with its cutout geometry. Keep the harvested icon distinct. |
| Plumaria blossoms | 2D, harvested ingredient | Flowers of the placed Plumaria | Pink, pale, and blue heads, brief stem | PickleCraft bloom surfaces and vanilla flower items | Group the three heads into separate clusters. Keep the green base shorter than the plant’s. |
| Hot iron ingot | 2D, material item and heat state | Exact vanilla iron ingot | Angled side and flat upper facet, orange heat | Vanilla iron ingot, normal and heated views | Reuse vanilla layer. Apply orange tint only, with the exact original alpha mask and facets. |
| White-hot iron ingot | 2D, peak heat state | Exact vanilla iron ingot and hot iron | Identical facets, paler heat | Vanilla iron ingot and preceding hot state | Same layer and a paler tint. Check alongside hot and cool iron. |
| Hot gold ingot | 2D, heat state | Exact vanilla gold ingot | Gold bar shape stays distinct from ore | Vanilla gold ingot | Same layer with a warm tint, retaining its gold material and outline. |
| Heating iron ore | 2D, animated material state | Exact vanilla raw iron | Irregular facets before the bar forms | Vanilla raw iron and the furnace states | Reuse raw iron layer and retain the dynamic custom-model-data tint slot. |
| Heating gold ore | 2D, material state | Exact vanilla raw gold | Lumpy gold facets, warm color | Vanilla raw gold | Reuse raw gold layer and keep the heating tint. |
| Hot coal | 2D, renderer effect | Charcoal structure with ember cracks | Black edge and short incandescent seam | Vanilla charcoal | Keep a stone-like outer mask and put warm flecks in one central cluster. |
| Firebox flame | 2D, crossed animated render planes | Minecraft fire color order | Tall orange tips, nearly white base | Vanilla fire and PickleCraft furnace effect | Keep a connected base across four 16×16 frames. Retain sprite-sheet dimensions and timing. |
| Iron saw | 2D, a thin hand tool | PickleCraft blade and grip components | Straight pale plate, stepped edge, wooden grip | Forge Multipart iron saw and vanilla shears | Retain checked rails and handle-width rule. Use a darker underside contour without changing the join. |
| Saw blade | 2D, flat component | Metal part of the finished saw | Same edge and plate profile with no grip | Assembled saw and Forge Multipart | Derive the mask from the assembled steel. Shade the lower silhouette without thickening it. |
| Saw handle | 2D, flat component | Grip of the finished saw | Same closed opening while enlarged enough to read | Assembled saw and wooden tool grips | Preserve the part perimeter and opening. Darken inside the wood contour only. |
| Wooden hammer | 2D, handheld Minecraft convention | Shared mallet silhouette, wooden head | Two matching faces, tan wood head, centered joint | Tinkers’ Construct handle and vanilla wooden axe | Keep the checked mallet mask and diagonal joint. Set head in warm oak tones. |
| Stone hammer | 2D, handheld Minecraft convention | Shared mallet silhouette, stone head | Two matching faces, cooler textured head | Tinkers’ Construct handle and vanilla stone tool materials | Preserve the mallet mask. Use a subdued gray head and keep the wood/stone distinction. |
| Iron hammer | 2D, handheld Minecraft convention | Shared mallet silhouette, iron head | Two matching faces, brighter steel facets | Tinkers’ Construct handle and vanilla iron tools | Preserve the mallet mask. Separate light upper steel from the darker underside. |
| Iron tongs | 2D, hinged tool silhouette | Common tongs frame for all states | Two rails, pivot, distinct tips | Vanilla shears | Keep the gap and pivot. Use a narrow steel highlight instead of whitening the whole tool. |
| Tongs holding iron | 2D, inventory-only load | Empty tongs and hot iron angling | Original rails and a small orange faceted ingot | Vanilla iron ingot and empty PickleCraft tongs | Preserve frame pixels. Shape a small bar between the tips with a pale top and orange sides. |
| Tongs holding gold | 2D, inventory-only load | Empty tongs and hot gold angling | Original rails and a slightly yellower bar | Vanilla gold ingot and empty PickleCraft tongs | Use the same occupied bar coordinates as the iron load and a distinct gold ramp. |
| Oar | 2D, broad silhouette like other handheld tools | Wood palette of the boat it builds | Clearly wider paddle at the end of a straight shaft | Vanilla oak boat and wooden tools | Keep a broad flat blade and darken the far edge without bending the shaft. |
| Fire starter | 2D, small hand tool | Wooden spindle plus fire board | Two distinct wooden parts and an ember where they meet | Vanilla flint and steel for warm accent, PickleCraft starter design | Put the bright ember at the spindle contact. Keep enough contrasting board beneath it. |
| Stone | 2D, small ground material | Vanilla flint as a neighboring visual category | Irregular broad face and angular underside | Vanilla flint | Make a distinct gray silhouette with one light plane and a darker lower facet. |
| Glue | 2D, soft material like slimeball | Slimeball form with pale adhesive color | Rounded mass and a soft specular cluster | Vanilla slimeball | Use a rounded, contiguous outline. Keep the highlights in one cluster instead of sharp stone facets. |
| Creative Zone Map | 2D, vanilla map convention | Vanilla paper map with PickleCraft terrain colors | Parchment first, subtle inked land and water | Vanilla map and PickleCraft zone palette | Make the parchment frame wider than the color patches. Limit map ink to the interior. |
| Lit torch | 2D item, 3D placed counterpart | Vanilla upright torch and shared PickleCraft shaft | Slim upright stem and bright flame | Vanilla torch | Use the upright pose and apply a warm flame to the top without moving the shaft. |
| Unlit torch | 2D item, 3D placed counterpart | Same lit torch shaft and alignment | Charred head, same height and width of wood | Vanilla torch and PickleCraft lit variant | Reuse the shaft mask, remove fire, and use a dark charred cap. |
| Cow spawn egg | 2D, vanilla spawn egg convention | Shared PickleCraft cattle egg profile | Warm cream base with irregular dark marks | Vanilla spawn egg family and cow coat | Keep the egg boundary while preserving visible cow markings at inventory scale. |
| Bull spawn egg | 2D, vanilla spawn egg convention | Same egg profile as the cow | Warmer coat tones and separate markings | Vanilla spawn egg family and PickleCraft bull coat | Reuse the egg profile. Keep a browner mark and a warm highlight distinguishable from the cow. |

## Step 6. Compare with vanilla at inventory size

The item-art playtest draws each PickleCraft model on the left and its vanilla
comparison on the right using the same inventory renderer and background.
These notes record what the first comparison showed and how it was resolved.

| Item | Vanilla comparison | Observation and decision |
| --- | --- | --- |
| Anvil | Anvil | Shape, framing, and light already agree. Retain the exact placed model. |
| Open barrel | Barrel | The open top is darker than the vanilla lid, which is necessary for the cavity to read. Retain the same volume model and barrel materials. |
| Workbench | Oak log | The top reads as a work surface and the body still reads as a log. Retain the original model and visible end-grain family. |
| Carpenter’s table | Crafting table | The two-block silhouette was a little small beside the crafting table. Enlarge it in the GUI while keeping both halves and the legs visible. |
| Iron furnace | Furnace | The narrow chute and front mouth read, but the model was much smaller than the vanilla furnace. Increase GUI scale and leave the placed geometry alone. |
| Plumaria | Azure bluet | Both read as flowers at inventory size. Retain the three bloom colors and the tilted plant model to keep it distinct from the harvested blossoms. |
| Hot iron ingot | Iron ingot | The exact silhouette matches. Raise the orange tint's green channel to make the highlight warmer and less copper-like. |
| White-hot iron ingot | Iron ingot | The shape matches, but the highlight needs to read as the lightest stage. Pale the tint without making it neutral gray. |
| Hot gold ingot | Gold ingot | Exact silhouette, with a bit too much orange cast. Lighten the tint toward vanilla gold's yellow highlights. |
| Heating iron ore | Raw iron | Shape and facet placement match. Its deeper orange is created by the live heat tint, needed to show progress. Retain the dynamic tint. |
| Heating gold ore | Raw gold | Its warm cast was more saturated than the raw gold comparison. Soften the tint while keeping ore distinct from the finished ingot. |
| Hot coal | Charcoal | The ember marks were too scattered for a tiny fuel icon. Darken the outside embers and retain the bright center. |
| Firebox flame | Fire charge | The orange ramp is comparable. Break up the flat lower stripe and concentrate the lightest pixels in the fire's core, keeping four connected frames. |
| Iron saw | Shears | The steel edge was softer. Darken the far contour and warm the grip accent while preserving the blade line and teeth. |
| Saw blade | Iron ingot | The silver material reads, but the lower edge needed more definition. Harden the underside without widening the plate. |
| Saw handle | Stick | Its inset shape is distinct but too dark in the item slot. Add a modest warm rim while leaving the opening intact. |
| Wooden hammer | Wooden axe | The head is correctly wood but its underside blends into the handle. Introduce a darker wood plane beneath the head. |
| Stone hammer | Stone axe | The stone face was paler than the axe. Add a muted underside and keep the two striking faces and centered shaft. |
| Iron hammer | Iron axe | The pale face reads as iron. Cool the lower edge shadow so the head is distinct from the shaft. |
| Iron tongs | Shears | Both show a hinged tool, but the tongs were too evenly bright. Add contrast at the pivot while keeping two open rails. |
| Tongs holding iron | Iron ingot | The loaded tool should still read as tongs first. Apply the matching pivot treatment and deepen the underside of the orange load. |
| Tongs holding gold | Gold ingot | The gold load is recognizable. Deepen its amber underside and preserve the matching grip position and frame. |
| Oar | Oak boat | The brown ramp is compatible, but the paddle edge is soft. Darken the far edge and add a narrow face highlight. |
| Fire starter | Flint and steel | It should read as wood with a small spark. Darken the fireboard bottom and concentrate the ember at the spindle contact. |
| Stone | Flint | Similar scale and material category, but the loose stone was too evenly gray. Strengthen the lower rim and one angular upper face. |
| Glue | Slimeball | Its soft silhouette works. Increase the cream highlight and deepen the underside to separate it from the gray stone. |
| Plumaria blossoms | Peony | Pink is familiar, but the green competed with the blooms. Give the stems a darker base and keep three separate colored heads. |
| Creative Zone Map | Empty map | The terrain ink is a required difference. Frame it with a brighter parchment top and a darker lower fold. |
| Lit torch | Torch | Same upright pose, with a broader flame. Match the ordinary wood colors more closely and put the lightest pixel near the flame tip. |
| Unlit torch | Torch | The dark cap must read as extinguished. Use the same adjusted shaft as the lit version and deepen the charred cap. |
| Cow spawn egg | Cow spawn egg | The mod egg read like a pale pebble beside the vanilla egg. Enlarge the dark coat mark and deepen the perimeter while keeping the egg mask. |
| Bull spawn egg | Cow spawn egg | It also needed a stronger species cue. Use broader warm brown markings and a warm upper highlight, distinct from the cow egg. |

## Step 6 return log

This review starts from the previous vanilla-fit gallery. A pass means the
item fits the comparison at inventory size while meeting its Step 3 cues.
A return records which reference was revisited before the shape or shading
was rebuilt. The final gallery uses the same paired inventory renderer.

| Item | Review and result |
| --- | --- |
| Anvil | Pass on entry. The exact vanilla-based 3D model agrees in pose and scale. |
| Open barrel | Pass on entry. The oak hues match barrel materials, and the open rim is an intended functional difference. |
| Workbench | Pass on entry. The oak palette agrees with the log and its top must read as a working surface. |
| Carpenter’s table | Pass on entry. Its warm wood and lit faces fit beside the crafting table. The longer silhouette shows both halves. |
| Iron furnace | Pass on entry. The stone shading agrees with the vanilla furnace. Keep the chimney and exposed chamber. |
| Plumaria | Pass on entry. Its pale clusters agree with vanilla flower treatment, and its blue and pink petals distinguish the species. |
| Hot iron ingot | Pass on entry. Identical iron facets, with its required orange heat cue. |
| White-hot iron ingot | Pass on entry. Identical iron facets, with a distinct paler heat cue. |
| Hot gold ingot | Pass on entry. Identical gold facets with its warm state tint. |
| Heating iron ore | Pass on entry. The raw iron facets match. The warmer tint must report heat. |
| Heating gold ore | Pass on entry. The raw gold facets and color family already fit. |
| Hot coal | Pass on entry. Its dark mass and clustered embers serve the same fuel cue as charcoal while showing its active state. |
| Firebox flame | Return to fire charge and vanilla fire. Reduce the flat bright lower stripe and use staggered pale and dark clusters. Pass after first rerender. |
| Iron saw | Pass on entry. The narrow grip and silver plate read at the same scale as shears. |
| Saw blade | Pass on entry. Its metal ramp aligns with iron, while the diagonal plate shape must match the assembled saw. |
| Saw handle | Return to stick and the wooden tool grips. Retain the approved opening and brighten the top wood cluster. Pass after first rerender. |
| Wooden hammer | Pass on entry. The warm handle and head separation read beside the wooden axe. |
| Stone hammer | Pass on entry. The gray head matches the stone tool value range and keeps the protected symmetric geometry. |
| Iron hammer | Pass on entry. The light iron and shaded side agree with iron tools. |
| Iron tongs | Return to shears for more contrast at the pivot. First rerender still read like a hairpin. Return to the distinct working ends of shears, give the tongs blunt inward tips, and retain the open mouth. |
| Tongs holding iron | Return with the empty tongs. Recheck the common frame and keep a small orange ingot tucked between its ends. |
| Tongs holding gold | Return with the empty tongs. Recheck the common frame and keep a yellower ingot on the same load coordinates. |
| Oar | Return to oak boat. Give the broad paddle a visible pale face and a darker side without altering the shaft. Pass after first rerender. |
| Fire starter | Pass on entry. The restrained ember and two wood parts preserve the original fireboard design. |
| Stone | Return to flint for stronger face separation. First rerender skewed purple. Return to the neutral upper facet and charcoal edge of flint. |
| Glue | Return to slimeball for cohesion and a localized glint. First rerender was still flat. Return to slimeball's rounded shadow and shape a dark crescent and a tight highlight. |
| Plumaria blossoms | Pass on entry. The separate small blossoms and muted stem fit the flower comparison. |
| Creative Zone Map | Pass on entry. Its pale rolled parchment reads beside the map, with the colored print identifying the zone map. |
| Lit torch | Pass on entry. Its upright wood and small warm flame fit beside torch. |
| Unlit torch | Pass on entry. It matches the lit item shaft, with a necessary charred tip. |
| Cow spawn egg | Return to the vanilla cow egg. Rebuild the spotted shell with substantial black markings and a pale background. Pass after first rerender. |
| Bull spawn egg | Return to the vanilla cow egg. Use that contrast principle with a warmer horn-shaped pattern that remains distinct from the cow egg. Pass after first rerender. |

Final rerender: all ten returned items passed this visual review. The second
pass made the tongs' working tips distinct from their enclosed butt, returned
stone to neutral gray, and gave glue a curved shadow. The loaded tongs share
exact frame pixels. Both Minecraft smoke runs passed. The other 22 items
passed on entry and retained their existing art.
