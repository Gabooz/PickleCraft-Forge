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

## Eight-step expressive review

The earlier construction and reference tables record the neutral baseline.
This pass remade the art from those drafts. Each row keeps the aesthetic note
separate from the treatment applied after construction and describes the cue
checked at inventory size next to the earlier vanilla comparison.

| Item | Step 3. Aesthetic intent | Step 7. Expressive remake | Step 8. Vanilla fit |
| --- | --- | --- | --- |
| Anvil | Dense and heavy | The existing side and waist already have the same solid silhouette as the placed anvil. | The vanilla twin is visually identical. |
| Open barrel | Inviting, with room inside | The overhead view already puts the open cavity at the center of the icon. | The vanilla oak rim and bands still anchor the open shape. |
| Workbench | Rough base, dressed work surface | The flat light top and bark sides already signal the contrast on the placed model. | The profile still reads as an oak log with a worked top. |
| Carpenter’s table | Solid and spacious | A higher viewing angle and larger scale emphasize the length of the top and its supports. | The work surface reads beside the vanilla crafting table and fits the slot. |
| Iron furnace | Tall, heavy, and hot inside | A larger, lower-angle front view emphasizes the chamber and chimney. | The chimney and dark opening distinguish it from the vanilla furnace. |
| Plumaria | Airy and open | The view looks farther down onto the spreading leaves and blooms. | It reads as a plant beside the azure bluet. |
| Hot iron ingot | Strong radiant heat | Deepened the orange cast on the exact iron facets. | The iron silhouette is identical to the cool bar. |
| White-hot iron ingot | Incandescent | Moved the tint toward pale cream. | The iron silhouette matches and the hot tint is lighter. |
| Hot gold ingot | Radiant and golden | Pushed the hue toward a bright amber-yellow. | The facets read as the vanilla gold bar. |
| Heating iron ore | Dense mineral warming up | Kept the live heat ramp over the raw iron facets. | The underlying raw iron outline matches. |
| Heating gold ore | Warm mineral with recognizable facets | Lightened the warm tint over the raw gold. | The lumpy outline distinguishes it from the gold bar. |
| Hot coal | Brittle crust over intense embers | Put two hot accents inside the crust and subdued surrounding reds. | The body still reads as a lump beside charcoal. |
| Firebox flame | Full and intensely hot at its base | Strengthened the pale roots and orange sides across all four frames. | The orange-to-pale color ordering reads as fire. Frame timing is shared with the baseline. |
| Iron saw | Straight, sharp, and workmanlike | Brightened a few patches along the steel rail and darkened the lower edge. | The steel reads next to shears. The straight plate and grip join survive at inventory size. |
| Saw blade | Sharp and rigid | Carried the stronger metal tones onto the separate blade. | The blade has the pale steel finish of an ingot and the saw’s narrow profile. |
| Saw handle | Sturdy and easy to grip | Added a stronger warm outer bevel and a dark underside. | The material reads as wood beside a stick. The opening stays readable. |
| Wooden hammer | Sturdy oak workmanship | Added a warmer face and a shaded edge. | Wood and handle tones sit in the same range as the wooden axe. |
| Stone hammer | Hard and well-built | Kept one broad cool face and made the near edge darker. | Gray head and wooden grip separate at the same scale as the stone axe. |
| Iron hammer | Firm and neatly finished | Sharpened the contrast between the steel face and lower edge. | Bright steel and wood read beside the iron axe. |
| Iron tongs | Precise and firm at the grip | Put a short glint on the jaws and emphasized the hinge. | Their steel reads beside shears and the two rails remain distinct. |
| Tongs holding iron | A secure grip on radiant iron | Used the same frame with a pale orange top and deeper red-orange side on the load. | The color and facets echo the heated iron bar at the scale of the jaws. |
| Tongs holding gold | A secure grip on hot gold | Used the same frame and load silhouette with a yellower top and amber underside. | The hue matches the gold family. |
| Oar | Long, smooth wood with a useful paddle | Added a broad warm highlight across the paddle and deepened its far edge. | The color reads as wood beside the oak boat. |
| Fire starter | Rough wood and a concentrated spark | Brightened the small area where the spindle meets the wood. | The spark occupies a small area beside the high-contrast flint and steel icon. |
| Stone | Smooth and hard | Broadened the face tone, sharpened one glint, and strengthened the bottom contour. | It reads as a small gray material beside the sharper vanilla flint. |
| Glue | Soft, rounded, and elastic | Rebuilt the first expressive draft as a low dome with a compact upper-left reflection. | The dome and shine read as a soft material beside slimeball. See the return below. |
| Plumaria blossoms | Delicate but colorful | Strengthened the pink and blue clusters and shaded the green stem. | Individual clusters still read next to vanilla flower items. |
| Creative Zone Map | Worn but readable parchment | Warmed the lit paper edge and added weight to its lower rim. | The paper remains the dominant color beside the vanilla map. |
| Lit torch | Dry wood, with a bright usable flame | Increased the separation between the pale wick and the wooden shaft. | The shaft and light remain vertical like the vanilla torch. |
| Unlit torch | Cold and charred | Used the lit torch’s exact shaft treatment with a small, dark cap. | The silhouette reads as a cold torch beside the vanilla lit reference. |
| Cow spawn egg | Mottled cream and dark coat marks | Brightened the cream face and anchored the largest dark patch. | The cow markings sit inside the shared egg profile. |
| Bull spawn egg | Warm tan and distinct coat marks | Pushed the face toward tan and deepened the brown marks. | The shared egg profile reads as an egg and the warmer body distinguishes the bull. |

### Structural return for glue

The first expressive draft looked like a cone of paste. Its pointed crown and
diagonal shoulders dominated the silhouette. Beside the vanilla slimeball,
its highlights traced the slope instead of describing a soft, rounded surface.

Step 4 now requires a broad dome with shoulders that reach full width in
short steps, a slightly flattened bottom, and a compact glint away from the
edge. The slimeball stays the shape and lighting reference. The revised
draft uses a 12 × 11 pixel footprint inside the 16 × 16 tile. The warm hue
identifies adhesive, and the concentrated glint and wider base carry the sense
of elasticity in the expressive pass.

## Strict vanilla redesign review

The earlier tables are the record of the previous rounds. In this round the
structural draft served as a brief for a fresh drawing. Common vanilla parts
were registered on the native grid or linked to their game models. The
comparison sheet shows the baked inventory render of each candidate beside
its vanilla reference. The 2D contact sheet also shows the previous art and
the native pixel geometry at 2×.

| Item | Aesthetic intent | Prior draft verdict and requirement | New construction and final comparison |
| --- | --- | --- | --- |
| Anvil | Heavy, solid iron | Pass. It represents the same placed anvil. | Retained the exact placed volume and matching vanilla inventory pose. |
| Open barrel | A clear, useful cavity | Pass. The ordinary barrel walls and bands already match. | Retained the placed barrel and its open top. The cavity accounts for the change in top shading. |
| Workbench | Rough log with a functional top | Pass. The oak log material and scale read correctly. | Retained the work surface and log end grain. The top projection conveys the function. |
| Carpenter’s table | A long, sturdy work surface | Return. The top-down view shortened the front edge. | Reframed the placed volume with a 30° pitch to expose the legs and front while keeping both halves in frame. |
| Iron furnace | Tall masonry with a hot chamber | Return. The low angle reduced the chute opening to a thin rim. | Reset the inventory pose to show the mouth and chute together. Stone and masonry shading remain consistent with the placed model. |
| Plumaria | An upward, airy bloom | Return. The earlier near-overhead view compressed the stems. | Reframed the planted volume at a shallower angle so the leaves, stems and bloom heads can be distinguished beside the azure bluet. |
| Hot iron ingot | Radiant orange heat | Match the exact iron facets. | Shared vanilla model with a warm orange tint. |
| White-hot iron ingot | A pale, intense peak | Match the exact iron facets. | Shared vanilla model with a brighter cream tint to keep it distinct from the orange state. |
| Hot gold ingot | Golden radiant heat | Match the exact gold facets. | Shared vanilla model with an amber-yellow tint. |
| Heating iron ore | Raw metal warming up | Pass. The game supplies the live heat ramp on the correct vanilla raw iron layer. | Kept that layer and the live ramp. |
| Heating gold ore | Warm, irregular mineral | Match the exact raw gold facets. | Shared vanilla resource with a warm tint and distinct raw outline. |
| Hot coal | Brittle crust with heat inside | Return. Exposed orange fragments gave it the shape of loose chips beside charcoal. | Drew a compact dark lump and enclosed the smaller ember fissures inside the crust. |
| Firebox flame | A continuous bed of flame | Return. The old high tips separated into thin spikes. | Redrew four connected frames with changing tips and a bright common base. The warm palette and clustered light read beside fire charge. |
| Iron saw | Straight, sharp, secure in the hand | Return. The first redraw narrowed the metal into a rapier. | Added width to the flat plate, gave it repeating dark teeth, and seated the closed grip inside the heel. The steel uses compact light clusters akin to shears. |
| Saw blade | Rigid steel with a defined cutting edge | Return. The first redraw had too little face between spine and edge. | Derived the independent component from the broadened final saw plate, including the same tooth rhythm. |
| Saw handle | A hand-sized, sturdy grip | Return. The outline and center hole from the long handle filled too much of the icon. | Drew a short closed wooden bow and used the same profile at the plate’s heel. The handle has a dark wood outline like a stick. |
| Wooden hammer | A solid, well-built mallet | Return. The old orange grip was too light and bulky beside the wooden axe. | Redrew the grip on the vanilla 45° tool axis in dark wood and centered a mirrored, double-faced wood head across it. |
| Stone hammer | Weight and hard stone | Return. The grip taper and head highlights looked like a separate stylized tool family. | Used the shared rebuilt grip and matched the stone axe’s gray plane and dark underside. Symmetric head ends mark the mallet. |
| Iron hammer | Solid workmanship and clean steel | Return. The old head and shaft widths disagreed with the iron axe. | Used the rebuilt grip, with a two-sided hammer head in the vanilla iron tool value range. |
| Iron tongs | Firm, precise grip | Return twice. The old tool read as a clip, and the first redraw’s narrow jaws almost closed in the slot. | Redrew the second jaw out from the first with a continuous visible interior gap. The silvery steel planes use shears as the reference. |
| Tongs holding iron | A secure grip on hot iron | Return. The load shrank to a pinpoint in the first redraw. | Spanned the open tips with a small faceted orange bar and reused the empty tool’s frame. |
| Tongs holding gold | A secure grip on hot gold | Return. The first redraw could barely be distinguished from the iron load. | Reused the same load silhouette and grip, with a yellower highlight and deeper amber side. |
| Oar | Long, planed wood and a useful blade | Return. The old shaft elbow and bright paddle patch broke the long tool pose. | Drew a straight diagonal shaft with a wider flat paddle, shaded in oak browns. |
| Fire starter | Rough wood and a concentrated spark | Return. The first redraw made the board too short to read separately. | Anchored the spindle to a near-horizontal board. Kept the bright contact area small, as in flint and steel’s localized contrast. |
| Stone | Smooth and hard | Return. The first redraw was scarcely half the size of flint. | Enlarged a single smooth gray face to a near-flint span and kept a flat dark underside. |
| Glue | Rounded elasticity | Return. The old profile was flattened and the glint read as a separate cream-colored patch. | Drew a slimeball-like dome with a compact glint integrated into a warm body and dark lower edge. |
| Plumaria blossoms | Airy, distinct petals | Return. The first redraw lost the heads at inventory scale. | Enlarged the individual color clusters while keeping the stem sparse. Flower size and cluster spacing read beside vanilla flowers. |
| Creative Zone Map | Familiar parchment with readable terrain | Return twice. First too triangular, then still too small beside the vanilla map. | Used the actual vanilla parchment layer and drew only the small land and water marks inside it. |
| Lit torch | A conventional working torch | Return. It was too thick and the shaft was too orange. | References the actual Minecraft torch item model. The lit placed block already uses the matching Minecraft block model. |
| Unlit torch | The same torch, cold at the tip | Return. The old shaft and cap had their own widths and colors. | Uses the same two-column silhouette at x = 7–8, with the unchanged vanilla shaft at y = 8–15 and a new charred cap at y = 6–7. The item and placed block share this texture. |
| Cow spawn egg | Immediately recognizable cattle markings | Return twice. Generic egg and pinched egg shapes both failed beside the cattle icon. | References the actual vanilla cow spawn egg model. |
| Bull spawn egg | A fuller tan head with pointed horns | Return twice. A generic icon then a short oval missed vanilla’s animal-specific shape. | Redrew a full-height cattle silhouette with a separate pair of horns, warm coat and light face planes. The warm hues and horns identify the bull beside the vanilla cow egg. |

## Recursive vanilla and structure review

The prior approval of the bull icon is superseded. Each finish in this round
was evaluated against a vanilla reference and then against the structural
requirements and construction rules for that same revision. The approved
hammer, tongs, and torch art stayed in the comparison as benchmarks.

| Item | Layer 8. Vanilla fit | Layer 9. Required reading and finished construction |
| --- | --- | --- |
| Anvil | Same vanilla material and outline. | Horn, waist, and foot are visible in the shared placed model. |
| Open barrel | Oak barrel bands and wall scale agree. | Rim and cavity are visible together. The opening is the functional change from the ordinary barrel. |
| Workbench | Oak grain and log scale agree. | The flat work top and rounded log support are both visible with the placed model projected in the slot. |
| Carpenter’s table | The crafting table provides the reference for surface and wood values. | Worktop and legs read separately. The two placed halves project into one inventory slot. |
| Iron furnace | Masonry tones and chamber contrast read beside the furnace. | Both the top chute and front chamber are visible in the retained projection. |
| Plumaria | Custom petals and stems fit the vanilla flower material and scale. | Three bloom colors, stems, and spreading leaves remain visible on the 3D plant. |
| Hot iron ingot | Exact iron ingot resource. | Angled ends and both planes remain registered. Orange indicates heat. |
| White-hot iron ingot | Exact iron ingot resource. | Same ends and planes. The pale high-temperature state is distinguishable from orange. |
| Hot gold ingot | Exact gold ingot resource. | Same gold silhouette and facets. Warmth is confined to the tint. |
| Heating iron ore | Exact raw iron resource. | The raw outline remains registered while the live heat tint changes. |
| Heating gold ore | Exact raw gold resource. | The uneven facets remain readable during warming. Its shape separates it from the gold bar. |
| Hot coal | Dark charcoal-family lump with an ember seam. | The burn color occupies a bent interior seam inside a connected 11 × 9 lump. |
| Firebox flame | Shared warm ramp with a pale core. | The bright base spans six columns inside a ten-column foot. The four tip profiles change above the shared footprint. |
| Iron saw | Long steel plate reads in the same value range as shears. | The first redraw returned for a grip opening that shrank in the slot. The second returned because the tooth tips merged into the contour. The final plate and grip leave all nine pixels of a 3 × 3 opening unobstructed. Alternating steel tips stand away from the plate edge. |
| Saw blade | Steel planes match the complete saw and the metal reference. | The blade is the unchanged plate and tooth mask translated by two columns left and one row down. |
| Saw handle | Wood reads as an enclosed grip in the same ramp as the assembled saw. | First redraw returned with the saw. The final separate component encloses the same nine-pixel hole. Its translated shape matches the attached grip. |
| Wooden hammer | Approved diagonal tool grip and oak tones. | The grip centerline intersects the middle of the two striking faces. The finished mask is shared across all three materials. |
| Stone hammer | Approved diagonal pose and stone tool values. | Two opposite faces and the central wooden joint are visible. The grip and outline match the wooden and iron variants. |
| Iron hammer | Approved diagonal pose and iron tool values. | The stronger head contrast leaves both faces and the joint visible. Grip and outline match the other mallets. |
| Iron tongs | Approved steel finish beside shears. | The two jaws, open gap, and connected pivot remain readable. |
| Tongs holding iron | Approved frame and iron heat cue. | Frame pixels register exactly with the empty tongs. The orange load occupies the same shape as the gold load. |
| Tongs holding gold | Approved frame and amber heat cue. | Frame pixels register exactly with the empty tongs. The shared load silhouette carries the warmer gold cue. |
| Oar | Oak color and diagonal hand-tool alignment. | First redraw returned because the thin end read as a spoon. The finished blade stays five to six pixels wide for three successive rows and tapers through the neck into a three-pixel shaft. |
| Fire starter | Dark wood outline and concentrated warm accent. | The horizontal board is eleven pixels wide at its widest row. The ember sits where the diagonal spindle meets it. |
| Stone | Stone face and darker flint-family edge. | The rounded shoulder has an uninterrupted upper face and a dark lower plane. Occupied mask spans 12 × 9 pixels. |
| Glue | Rounded dome and integrated glint beside slimeball. | A single connected 11 × 10 body with a stepped shoulder and flattened base reads as a soft blob. |
| Plumaria blossoms | Small clusters and green stem read beside vanilla flower sprites. | Three differently colored heads remain readable, with their stems coming together below. |
| Creative Zone Map | Exact vanilla parchment. | The ink stays inside the inherited paper mask and contains distinct land and water clusters. |
| Lit torch | Approved actual vanilla item model. | The two-column silhouette and wick are exactly registered to the vanilla placed torch. |
| Unlit torch | Approved vanilla shaft and dark wick. | The 16 shaft pixels match vanilla exactly. Only the four wick pixels are replaced. |
| Cow spawn egg | Exact vanilla cow model. | The cattle silhouette and markings stay registered to the vanilla icon. |
| Bull spawn egg | Two earlier designs returned. The final shares the cow icon’s footprint, broad lower face and single-pixel outline, with warm coat markings and swept horn tips. | The previous release shifted the silhouette left and left the last two rows empty. The first redraw still had a 12-pixel lower face and a four-pixel chin. The final spans x = 1–14 and y = 0–15, with a 14-pixel lower face on rows 7–12 and an eight-pixel base. Distinct tips above the crown now sweep inward into the head. A further vanilla return required darker, larger coat patches, which now separate the lighter planes at inventory size. |

### Return rules tested on the final assets

- The bull needs the cow family’s full footprint and a connected lower face.
  The two tips must stay distinct from the crown on the top row and widen
  inward at the root. Large coherent coat markings must remain visible.
- Both saw grips need the same completely enclosed 3 × 3 opening. Check
  that the blade is connected to the grip and that placing it leaves all
  nine background pixels untouched.
- The oar needs distinguishable blade, neck, and shaft. Check the enlarged
  blade spans and the transition on the finished sprite, then check that
  the unzoomed drawing reads as a broad paddle.

## Assembled saw and derived parts

The finished iron saw is the only source for the handle and blade. Its
expressive brief is a firm wooden grip and a straight, sharp steel plate.
The vanilla shears supplied the reference for narrow steel highlights,
dark contours, and a readable grip. The saw keeps its distinct enclosed
wooden bow and alternating cutting edge. At the heel, the grip's right rail
must be entirely wood and remain closed when it is separated from the metal.

The first extraction returned because a steel pixel split the visible
wooden outline and left a second one-pixel hole in the isolated handle.
The correction was made in the assembled saw. It was then reviewed for a
connected blade and grip, a straight blade with alternating teeth, and one
3 × 3 grip opening. Both component sprites were copied from that revision.

| Component | Copied pixels | Touch-up | Parent comparison |
| --- | --- | --- | --- |
| Saw handle | All 28 visible wood pixels | Whole-pixel centering only | Translating the component five columns left and four rows down restores the exact grip, including the 3 × 3 opening. |
| Saw blade | All 44 visible steel pixels | Whole-pixel centering only | Translating the component three columns right and one row up restores the exact tooth, plate, and heel pixels. |

The two aligned masks are disjoint and reconstruct all 72 visible pixels
of the assembled saw. Each derived part is paired with the assembled saw
in the in-game review.

## Blank-canvas saw redesign

1. **Medium.** A flat 16 × 16 handheld sprite fits the thin plate and the
   main viewing angle. The finished sprite is the parent of both parts.
2. **Visual match.** Use the finish of a vanilla iron tool and the clear
   steel-to-grip separation of shears. The plate and wooden bow give this
   saw its own silhouette.
3. **Aesthetic intent.** Sharp and sturdy, like a well-built workshop tool.
   This note was reserved for the expressive drawing.
4. **Requirements.** A straight-backed, broad steel plate tapers toward a
   blunt nose. Four cutting teeth should read at inventory size. A closed
   wooden grip meets the plate at the heel and owns the entire aperture.
5. **References.** Inspected vanilla shears and the saved Forge Multipart
   iron saw sprite from the historical Infinity Evolved reference set.
   Shears show a tight light steel ramp and a dark junction. The legacy saw
   shows a broad backed plate and a distinct wood frame.
6. **Structural draft.** A separate neutral drawing established the closed
   grip and high-backed plate. Final construction requires a single connected
   component for each material, an exact 3 × 3 enclosed grip opening, a
   straight spine aligned within one pixel of the line from (6, 7) to
   (15, 1), and a continuous three-row point of contact between wood and steel.
7. **Expressive redraw.** Drew a second sprite on a blank canvas with a
   steel spine highlight, a tapered metal face, a separate tooth edge, and
   a warm wooden bow. The revision after the vanilla return widened the
   blade and replaced the needle point with a blunt five-pixel nose.
8. **Vanilla review.** Returned the first expressive draft. Its shine
   converged on a sharp point, making the narrow steel read like a sword.
   The redesign presents a broader continuous face with a short cap and
   four visible teeth while keeping the inventory scale, diagonal pose,
   and material separation of the shears reference.
9. **Structure review.** Verified a connected 27-pixel wood mask and a
   connected 57-pixel steel mask, with the 3 × 3 opening at x = 2–4,
   y = 9–11. The spine follows the specified line, four tooth positions
   project at (8, 12), (10, 11), (12, 9), and (14, 8), and the two
   materials meet across three consecutive rows at the heel.

The new handle and blade were derived from the accepted assembled PNG and
centered by whole pixels. Neither needed a retouch. Together the two aligned
masks account for all 84 visible pixels, with identical colors and no overlap.

## Rigid saw reconstruction review

The iron saw follows the complete [reconstruction recipe](saw-structure.md).
That record supersedes the structural instructions for the previous saw.
The vanilla shears and historical Forge Multipart iron saw remained the
references for steel planes, compact shading, and the grip-to-plate junction.

- The first reconstruction exposed an unspecified tooth phase and spine
  depth. The revised recipe fixes the tooth coordinates and builds a
  continuous two-row spine along a single stepped line.
- The first expressive render still let the spine merge into the broad
  face. The recipe was revised and replayed with a lit lip, a shadow
  directly below it, and a parallel cutting edge with one-pixel teeth.
- The column-by-column and row-by-row replays produced the same 31 wood
  pixels and 62 steel pixels. The heel has the specified three-row join
  and the wooden bow retains its enclosed 3 × 3 opening. In inventory,
  the spine, face, and small teeth remain individually readable.
- The handle and blade were copied only after the assembled saw passed
  the vanilla comparison and structural review. Repositioning the two
  extracted sprites reproduces all 93 visible parent pixels. Neither
  component required any touch-up.

## Expressive exploration and resolved saw

This revision used layers 7 and 8 as the two halves of the redraw. The
expressive sketch pushed the mass of a forged spine and the warmth of a
wooden bow. It used an oversized nose, irregular cutting teeth, bright
blue streaks, and a bulky grip to test how far both materials could be
pushed at 16 × 16.

The second half rebuilt the contour and material layout. The thick spine
survived, with one light lip and a continuous shadow beneath it. The broad
plate became a few quiet steel planes. The irregular points became four
one-pixel teeth and the bright nose became a short, blunt cap. The wooden
bow kept a stout lower palm stop and its own 3 × 3 opening. An updated
[reconstruction recipe](saw-structure.md) fixed the new working depth,
contours, material ownership, and tooth positions before the final review.

The resolved saw uses the compact cool-steel highlights and grip separation
seen in vanilla shears. Its connected 38-pixel grip and 68-pixel steel plate
passed the independent row reconstruction. The final comparison checked
the nose, wood-to-steel join, transparent opening and one-pixel teeth at
native size and at integer zoom. No further pixel edit was needed. Both
component sprites were extracted from this approved parent and recentered.
Their translations reconstruct the same 106 pixels exactly.
