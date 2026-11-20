# PickleCraft item texture method

Draw item sprites directly on the target pixel grid. Use 16 Ã— 16 transparent
PNG for ordinary Minecraft items unless the asset calls for a different size.
Enlarge previews with nearest-neighbor scaling. Never make a large illustration
and shrink it to create the sprite.

## Layer 1. What the item must communicate

Before drawing, write what it is, what its key parts do, and how those parts
physically meet. Identify the two or three cues that must survive at inventory
size. For example, a hand saw needs a single straight toothed plate and a closed
grip seated at the heel. A mallet needs two equal striking faces and a wooden
handle seated flush in the center of the head. Record the desired orientation,
proportions, material boundaries, and meaningful negative spaces.

## Layer 2. Find references for the listed parts

For each visual requirement from layer 1, look for an equivalent item or part
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

## Layer 3. How the pixels must achieve it

Translate the requirements and reference observations to dimensions, axes, masks, and checks specific to
the item. Construct both sides of a joint in the same coordinate system. Check
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

Render the flat construction and outlined version side by side, with 1Ã—
previews on dark and light backgrounds. Inspect the result at 1Ã— and at an
integer enlargement. A pixel check proves its stated constraint only. If the
sprite reads incorrectly, identify the missing requirement, add the
corresponding check, and redraw before export. For experiments, keep the draft
files separate from the mod assets until a version is selected.
