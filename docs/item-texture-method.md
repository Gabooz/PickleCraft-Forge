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
convention and the parts that are deliberately original.

## Layer 3. What the item must communicate

Before drawing, write what it is, what its key parts do, and how those parts
physically meet. Identify the two or three cues that must survive at inventory
size. For example, a hand saw needs a single straight toothed plate and a closed
grip seated at the heel. A mallet needs two equal striking faces and a wooden
handle seated flush in the center of the head. Record the desired orientation,
proportions, material boundaries, and meaningful negative spaces.

## Layer 4. Find references for the listed parts

For each visual requirement from layer 3, look for an equivalent item or part
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
helmet's rim. Draw original pixels and use references for proportion, pose,
material separation, and readability.

## Layer 5. How the pixels must achieve it

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

## Layer 6. Compare the finished item beside common vanilla items

Render the finished item beside familiar vanilla items at the same in-game
size, lighting, background, and display angle. Use a counterpart where one
exists and a common item from the same category for new designs. Compare
scale and pose, outline weight, light direction, contrast, palette, and the
size of color clusters. This is a review of the finished render, after the
earlier reference search and construction.

Give each item a pass or return decision. If the style differs from vanilla
in a way that weakens the item's intended look, record the mismatch, the vanilla
comparison, and the Step 3 cues that must survive. Send those notes back to
Step 4. Find a more useful reference or inspect the original one more closely,
then rebuild the affected geometry, outline, or shading in Step 5. Render it
again in Step 6. Repeat Steps 4–6 until the item passes. A packaging or model
check cannot substitute for the visual pass.

Keep a short record of each return and what changed. A visible difference can
pass when it is needed for the item's identity or a state cue from Step 3 and
the rest of the rendering still fits. Recheck shared traits on each related
item, including material variants, components, and animations.

## Review items individually

Give each texture or model its own record covering all six layers. Do not
substitute one record for a family. If exact matching is required, share the
source model or compare the finished masks directly, then review each linked
item in its own context. Review an animation frame by frame and as a loop.
