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
reason for the choice for each item, including component and state items.

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

## Layer 4. What the item must communicate

Before drawing, write what it is, what its key parts do, and how those parts
physically meet. Identify the two or three cues that must survive at inventory
size. For example, a hand saw needs a single straight toothed plate and a closed
grip seated at the heel. A mallet needs two equal striking faces and a wooden
handle seated flush in the center of the head. Record the desired orientation,
proportions, material boundaries, and meaningful negative spaces.

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

## Layer 7. Remake the item to emphasize its aesthetic intent

Save the neutral construction from layer 6. Open a blank 16 × 16 canvas or an
empty model scene and reopen the layer 3 notes. Design a second version from
the ground up. Plan the shape, proportions, joint or depth, material regions,
and lighting together around the aesthetic intent. Keep the structural
requirements and protected vanilla parts available as guides. For an exact
vanilla state, rebuild the state-specific art and reuse the protected base.

Write down the redesigned structural and lighting choices before drawing.
For an original sprite, show where the alpha mask or the boundaries between
parts have changed. Palette edits, selective highlights, or tracing the
neutral outline with different colors do not complete this layer. For 3D art,
recompose the model where permitted. When the inventory model represents an
actual placed object, redesign how its volume is presented while keeping the
placed geometry. Exact counterparts and protected state silhouettes make the
vanilla geometry the starting constraint.

## Layer 8. Require a strict vanilla match

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

Send those notes back to layer 4, the structural requirements step that was
formerly layer 3. Revise those requirements in light of the critique. Then
repeat reference inspection, structural construction, expressive remaking,
and the vanilla comparison. Repeat layers 4–8 as many times as needed.
Retain the aesthetic intent unless the user changes it. Record the draft,
the critique, the revised requirement, and the rerendered result. A packaging
or model check cannot substitute for the visual pass.

## Review items individually

Give each texture or model its own record covering all eight layers. Do not
substitute one record for a family. If exact matching is required, share the
source model or compare the finished masks directly, then review each linked
item in its own context. Review an animation frame by frame and as a loop.
