# PickleCraft item texture method

Draw item sprites directly on the target pixel grid. Use 16 × 16 transparent
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

## Layer 2. How the pixels must achieve it

Translate each requirement to dimensions, axes, masks, and checks specific to
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

Render the flat construction and outlined version side by side, with 1×
previews on dark and light backgrounds. Inspect the result at 1× and at an
integer enlargement. A pixel check proves its stated constraint only. If the
sprite reads incorrectly, identify the missing requirement, add the
corresponding check, and redraw before export. For experiments, keep the draft
files separate from the mod assets until a version is selected.
