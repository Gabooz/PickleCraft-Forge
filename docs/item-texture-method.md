# PickleCraft item texture method

Draw item sprites directly on the target pixel grid. Use 16 × 16 transparent
PNG for ordinary Minecraft items unless the asset calls for a different size.
Enlarge previews with nearest-neighbor scaling. Never make a large illustration
and shrink it to create the sprite.

## Layer 1. Choose 2D or 3D for this item

Start with the way this item is used and what players need to recognize in the
inventory, hand, and world. Compare its Minecraft equivalent. Prefer the actual
block model for a placeable object whose volume, open top, work surface, or
other depth cue is part of its identity. Use a sprite when its silhouette is
the principal cue or when the corresponding vanilla item uses one. An item can
use a sprite while its placed block has a separate model. Keep an explicit
reason for the choice for each item, including component and state items. As
the final check in this step, ask whether the item is flagged as a direct
clone of all or part of a parent texture. Only the saw handle and saw blade
are flagged. Send those two through the derived texture process below.
Make and approve their parent, the assembled iron saw, through all eleven
layers first.

## Layer 2. Match the object that players already recognize

Decide whether this item is a state of, a component of, a material variant of,
or a direct counterpart to an existing item. Name the exact visual match and
which properties must carry over. For state changes, start with the same model
or part mask and change only the state cues. For example, a hot iron ingot
should use the iron ingot's silhouette and facet layout, including its angled
ends, and change its color. An open barrel should retain its actual hollow
model in the inventory. A component must still read as the piece seen in the
assembled item. When there is no exact match, record the nearest visual
convention and the parts that are deliberately original. For a direct vanilla
counterpart, use its resource ID when possible. When a state requires custom
art, register it over that counterpart on the native grid and protect all
unaffected pixels. A regular torch must look like a regular torch. A dormant
torch changes at the wick and keeps the shaft's width, position, and shading.

## Layer 3. Record the aesthetic intent and set it aside

Decide what the object should feel like beyond being recognizable. Write one
or two physical or emotional qualities, such as rounded elasticity for glue,
solid workmanship for a mallet, or smooth hardness for a stone. Name what a
player should sense at inventory size. Keep this note separate and set it aside
until layer 7. It does not drive the structural requirements, reference search,
or first construction.

## Layer 4. Write and replay a complete reconstruction recipe

Start from an empty canvas or scene. Assume the reader has never seen the
object and has no draft, model, or shared intuition to consult. Give the
reader a complete, ordered set of instructions that produces the intended
object from primitives. Explain what the object does and which physical cues
make it recognizable. Then define the whole construction, including the
parts a knowledgeable artist might otherwise supply from memory.

1. Define the canvas or model coordinate system, units, viewing direction,
   origin, and axes. Identify every part and the order in which it is built.
2. Specify each part's complete shape, dimensions, placement, orientation,
   material, inward and outward faces, and every joint. Define the empty
   spaces and their boundaries as deliberately as the solid ones. For
   repeated features, specify the starting phase, spacing, count, and end.
   For a 3D model, define depths, hidden faces, overlaps, and which faces
   the intended views must reveal. For a sprite, specify the visible
   projection and how the parts remain distinct at native size.
3. Describe the intended cues as construction decisions. For example, a
   rigid saw needs an uninterrupted reinforcing spine, a continuous plate
   of controlled depth beneath it, and small tooth tips along a parallel
   edge. Translate words such as *straight*, *stiff*, and *centered* into
   explicit geometry and checks a reader can reproduce.
4. Close the recipe with expected results for each part and the assembled
   object. Include measurable values and a recognition check at normal
   size. Record what may vary during the expressive redraw and what must
   survive it.

### Replay the recipe before continuing

Put the draft and reference views aside. Reconstruct the object, in order,
from the recipe alone on a new canvas or in a new scene. After each
instruction, check that the required inputs already exist, the next
operation is unambiguous, and the result is usable by the next instruction.
Check every part and their completed assembly against the intended object.
A recipe fails if a step requires guessing, two different constructions
satisfy the same instruction in a meaningful way, a part is missing, or the
reconstructed object gives the wrong impression at its intended size.

Record the first failing instruction, what the replay actually produced,
and the missing definition. Rewrite the recipe and replay it again from
the empty starting state. Repeat from the beginning after **every**
revision, including a single correction. Continue only when the complete
recipe independently reconstructs the intended item. If a later layer
exposes a structural weakness, return here, revise the recipe, and rerun
the full replay before moving forward. Keep the prose recipe and the
successful reconstruction with the review record.

## Layer 5. Find references for the listed parts

For each visual requirement from layer 4, look for an equivalent item or part
in the installed Minecraft assets and in the historical FTB Infinity Evolved
1.7 pack. Confirm mod membership from the pack listing and verify the reference
against that era's source or assets before attributing it to the pack. Inspect
the sprite at native size. A modern version of a mod is not a substitute for
the historical version.

Record the source, the specific part it informs, and the guideline it gives
the new sprite. For example, Minecraft's compass shows how a strong bezel
frames a readable needle, and Tinkers' Construct's wooden hammer handle shows
how few tones can define a diagonal shaft. If there is no exact equivalent,
say so and use the closest relevant part, such as a shell's rib shading or a
helmet's rim. Use references for proportions, pose,
material separation, and readability. Keep vanilla parts linked to their game
resources when available. Draw original parts at native size.

## Layer 6. Build the structural version

Translate the visual match, requirements, and reference observations into checks
for the finished model or sprite. For 3D, check openings, depth cues and the
inventory, hand, and ground transforms. For 2D, define dimensions, axes,
and masks specific to the item. Construct both sides of a joint in the same coordinate system. Check
the final visible join after overlap. Measure width perpendicular to a diagonal
axis, test symmetry by reflecting the actual mask, and test gaps against outside
air when their function depends on being open. Check bounds, meaningful
connectivity, proportions, and placement. Use nearly the entire grid in the
directions the object naturally spans. Leave deliberate margins when they
protect the geometry rather than forcing every item to touch all four sides.

Plan a full contour with the silhouette. Use an outside outline where it gives
the part its intended width and an inside outline where extra pixels would
close a hole, swallow teeth, or distort a joint. After outlining, measure the
finished contour and the visible material cores again. Preserve agreed parts
when changing other parts. Shade coherent faces and small pixel clusters with
a consistent light direction. Keep parts distinct even where their shadows
meet.

Render the flat construction and outlined version side by side, with 1×
previews on dark and light backgrounds. Inspect the result at 1× and at an
integer enlargement. A pixel check proves its stated constraint only. If the
sprite reads incorrectly, identify the missing requirement, add the
corresponding check, and redraw before export. For experiments, keep the draft
files separate from the mod assets until a version is selected.

## Layer 7. Explore the expressive redesign

Save the neutral construction from layer 6. Open a blank 16 × 16 canvas or an
empty model scene and reopen the layer 3 notes. Design a second version from
the ground up. Push the qualities the item should communicate. Try visibly
different silhouettes, proportions, gestures, material boundaries, and light
patterns, even when a draft overshoots. Sketches may temporarily depart from
the replayed construction. Keep exact vanilla counterparts and protected
state features as fixed sources, and respect geometry shared with a placed
block or another item.

Save the exploratory draft before judging its finish. Record which choices
express the intent and which experiments are worth carrying forward. The
exploration and cleanup together are the redraw.

## Layer 8. Resolve the expressive redesign

Use the exploratory draft as a sketch and the reconstruction recipe as the
construction target. Make a decisive second design pass. Redraw any part
that needs it. Adjust silhouette, proportions, joins, negative space,
material boundaries, depth cues, and shading so the expressive idea becomes
a clean and usable item. Combine strong ideas from the draft, simplify
repeated shapes, and remove effects that obscure the form. Work at native
size first. Check the result at integer zoom and on light and dark
backgrounds. For 3D art, fit the design to the mapped faces and intended
projection, and check its seams and overlaps.

Preserve the object's function, exact shared parts, and the cues the draft
was meant to express. Compare the cleanup with the layer 4 reconstruction.
If a stronger finished design changes a structural requirement, rewrite
and independently replay the recipe, then repeat layers 4–8 before the
formal reviews. Exploratory shapes have room to change within this layer.
Save the sketch and the resolved redraw with notes on what survived,
what was changed, and the effect at normal size.

## Layer 9. Require a strict vanilla match

Use the finished item at inventory size, rendered beside the exact vanilla
counterpart on the same background and with the same lighting. For an original
item, use the closest vanilla items for each recognizable part and for its
overall class. For example, inspect a mallet alongside the axe for the wooden
grip and alongside the mace for the head's weight and readable planes. Compare
at a whole-number enlargement as well. Use overlays where items share a pose,
material, or component.

Evaluate each trait independently. Any unexplained difference returns the
item for a rebuild. A readable object can still fail because it looks unlike
a vanilla item of its class. The desire to emphasize a quality provides no
exception. At minimum, ask the following questions.

- **Direct counterpart.** Do inventory placement, span, pose, outline, visible
  parts, and lighting agree? An actual torch shares the vanilla item art. An
  unlit torch keeps the shaft registered to the lit counterpart and changes
  only the flame and wick.
- **New item in a familiar family.** Do common parts agree in axis, length,
  thickness, join, outline weight, and finish? A wooden-grip mallet should
  belong with the vanilla axe and pickaxe when viewed at inventory size. The
  striking head is a function-specific difference.
- **Materials and lighting.** Compare outlines and interior contrast
  separately. Check local color ramps, light direction, flat planes, highlight
  size, edge pixels, and visual noise against the reference of the same
  material. Reject differences introduced only for decoration.
- **State and scale.** Check held, inventory, and placed views when a model
  has all three. Confirm common pixels of matched states, hand and inventory
  scale, legibility, cropping, and the animation loop when applicable.

Mark a failed item as **Return**. Identify the specific offending pixels or
dimensions, name the reference and what it does, explain the wrong reading,
and set a replacement requirement with a check. For example, “The unlit
torch is a thick brown dowel. Its vanilla reference is two pixels wide and
centered at x = 7–8. Rebuild around the original shaft and confine the char
to the two wick rows.” If a trait belongs to the design, record the functional
reason and compare the rest at the same standard. A pass requires a reviewed
render and a decision for each trait.

A pass advances the candidate to layer 10. A return restarts the review cycle
at layer 4 using the notes below.

## Layer 10. Independently verify the finished structure

Review the same baked render that passed layer 9. Pull up both structural
records side by side: the replayed reconstruction recipe from layer 4 and the
exact construction rules from layer 6. Review them independently. A good
vanilla match provides no evidence that either structural record passes.

- For every layer 4 reconstruction requirement, point to the cue in the finished art and
  identify the part, material, function, joint, opening, or state that it
  communicates at normal size. Fail a cue that relies on the description
  or a zoomed view to be recognizable.
- For every layer 6 rule, check the delivered asset after outline, overlap,
  lighting, transforms, and scaling. Use the appropriate measurement:
  occupied pixel bounds and spans, centerlines, contour reflection, part
  masks and joins, connected openings, component spacing, or projection
  of a 3D feature. Look at the rendered result as well as the source.
- Compare linked components and alternate states together. A protected
  shaft, outline, or grip stays registered across variants. Inspect each
  animation frame and the loop for the promised continuity.
- Record expected and observed values, and the pass or return for each
  requirement. Qualitative requirements still need visible evidence.
  A sprite can meet a mask measurement and fail because the part reads
  incorrectly at normal size. A 3D item can fail when the angle hides
  a feature that exists in the geometry.

For a failure, write the rule it breaks, the evidence in the final render,
and the exact correction that the new construction must meet. A rule that
allowed a wrong reading must be tightened with an observable criterion.
For example, a bull egg should have visible horn tips and a full cattle
head in the same footprint and value range as the cow icon. A narrowed head
with decorative spikes fails even if the spikes look symmetrical.

## Layer 11. Make a final vanilla comparison and pixel pass

After the style and structure reviews pass, set aside the draft and compare
the finished asset once more beside the closest actual vanilla item or
material. Use the same scale, lighting, and background, then zoom to a
whole-number enlargement. Go over the pixels in order, checking the
silhouette, material junctions, transitions in the light, transparent
edges, and stray specks. Inspect repeating tiles over their seams and
animated textures over every frame and the full loop. Look at the baked
item or mapped surface too.

Make small, local fixes only where the comparison exposes an artifact.
Log the changed pixels and the reason. A texture with no artifact passes
as it is. Preserve exact vanilla parts, state registration, and approved
geometry. If a texture needs a redesign or structural change, return to
layer 4 with the failure and repeat. After any pixel fix, recheck the
vanilla match and structural rules on the edited version, then repeat
this final comparison. Finish when the same saved revision passes all
three reviews.

For an audit, inventory every shipped texture and its vanilla reference,
including texture sheets used in world models or effects. Record a decision
for each texture. For a flagged derivative, use the final comparison to
catch seams or odd pixels, then check the parent. Fix shared art in the
parent and re-extract both parts if needed.

## Repeat the full review until the final version passes

Either style or structure review can return the candidate to layer 4. Log
the failing revision and rule, what the render showed, and the replacement
requirement. Replay the reconstruction and repeat layers 4–11 in order.
A late edit invalidates the approval for the earlier revision. Continue
until the final version passes or report the specific unresolved failure.

Keep the aesthetic notes through the cycle. Preserve approved reference
parts while the feature and structural requirements support them. Never
relax a requirement solely to pass a draft. A successful build and
numerical check are evidence for the recorded checks. The visual reviews
still need the rendered item or surface.

## Review items individually

Give each original texture or model its own record covering all eleven layers.
Use the four-step record below for each flagged derivative. Do not
substitute one record for a family. If exact matching is required, share the
source model or compare the finished masks directly, then review each linked
item in its own context. Review an animation frame by frame and as a loop.


## Derived textures

Only these two textures carry the flag.

| Derived texture | Approved parent | Copy from the finished parent |
| --- | --- | --- |
| `saw_handle.png` | `iron_saw.png` | The visible wooden closed grip, including its outline and transparent opening |
| `saw_blade.png` | `iron_saw.png` | The visible steel plate, including its outline, teeth, and heel |

1. **Copy the part.** Extract its finished pixels directly from the approved
   parent and center them on the native canvas with a whole-pixel translation.
   The visible material boundary in the finished parent decides ownership.
2. **Touch it up only if necessary.** Limit any touch-up to a minor edge
   issue from isolating the part, and record the affected pixels and reason.
   Preserve its shape, orientation, shading, and recognizable features.
   If it needs a new shape or a more substantial repair, revise and review
   the assembled saw before deriving its parts again.
3. **Compare with the parent.** Render the extracted item beside the
   assembled saw at inventory size. Translate the part back over its
   original position and compare its mask, RGBA pixels, and edges.
   If it fails, record where and why, then return to step 1. If the fault
   is in the parent, return the assembled saw to layer 4 first.
4. **Done.** Save both approved outputs and their review. Rerun extraction
   and the parent comparison every time the assembled saw changes. The
   final pixel pass applies to the shipped derivatives as well. Route an
   issue with a copied pixel to the parent or repeat the extraction.

For the iron saw, run `java tools/DeriveSawTextures.java` from the project
root with the development JDK. It extracts the two visible materials from
the assembled PNG. Use `java tools/DeriveSawTextures.java --check` to check
the committed component sprites against that source without writing files.
Both components currently use exact copies, with translation as the only
change. The in-game item-art sheet pairs each component with the iron saw.
