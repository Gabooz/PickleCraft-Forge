# Iron saw reconstruction

This is a reinforced hand saw. The user pushes and pulls a wooden bow to
move a rigid steel plate through wood. A raised spine braces the plate.
The teeth sit along the edge opposite the spine. The near side of the handle
and the near face of the steel are visible. The projected view looks at the
wide face of the blade with its nose angled up and to the right.

## Build the visible structure

1. Start with a transparent 16 × 16 canvas. A coordinate specifies one
   whole pixel. Count x to the right and y down from (0, 0) at the upper
   left. Use inclusive endpoints for every run below. Leave everything
   outside the specified runs transparent.
2. Build the wooden bow from these horizontal runs. They include the
   visible material contour. Wood terminates at x = 6 on its right rail.
   The hole at x = 2–4 and y = 10–12 belongs to the bow and stays empty in
   every subsequent step.

   | Row y | Wood columns x |
   | --- | --- |
   | 7 | 3–5 |
   | 8 | 2–5 |
   | 9 | 1–5 |
   | 10–12 | 1 and 5–6 |
   | 13 | 1–6 |
   | 14 | 2–5 |

3. Measure the spine axis at all ten steel columns. For each integer x
   from 6 through 15, use `s = 6 - floor((x - 6) / 2)`. This axis rises
   one row for every two columns. At x = 6, fill from y = s to s + 3,
   giving the heel a short seated end above the wooden rail. At every
   other column, fill from y = s to s + 5. This makes the plate and spine
   one rigid silhouette and sets the parallel cutting edge. The upright
   nose is all six pixels at x = 15, from y = 2 through y = 7.
4. Subdivide the visible steel without changing its footprint. Use
   y = s and s + 1 for the continuous reinforcing spine. Put the plate below
   that, with its cutting-edge bevel at y = s + 5 for x = 7–15. The short
   heel ends before reaching this bevel. The wood and steel masks share
   no pixels. Their visible join is the neighboring wood at x = 5 and steel
   at x = 6 on rows 7–9, with the steel at (6, 9) seated over the wood at
   (6, 10). The face of the bow is warm brown and the spine, plate, and
   cutting edge use distinct value bands of neutral or cool iron. Put a
   lighter lip on the lower row of the spine and a darker strip on the
   first row of the plate directly under it. Keep this separation along
   the working span from x = 7 through x = 14 so the spine reads as a
   brace at inventory size.
5. Add one steel tooth pixel at y = s + 6 in each of columns 7, 9, 11,
   and 13. The teeth fall at (7, 12), (9, 11), (11, 10), and (13, 9).
   Leave the intervening four positions and the space below the nose
   transparent. Each tooth is one pixel beyond its local bevel. No other
   steel extends below the cutting edge.
6. Check the result. The wood contains one enclosed 3 × 3 aperture even
   with the steel removed. The wood and steel are each a single
   edge-connected part. The steel has a two-row, ten-column reinforcing
   spine with no breaks and a constant plate depth through the working
   span. At 1×, follow the spine and the long cutting edge before looking
   for the four small teeth. If the steel reads as a row of flexible
   points or the heel looks unsupported, return the recipe. The final art
   can vary the lighting and exact shade boundaries while preserving all
   structural runs, the join, and the silhouette of both materials.

## Replay and return record

The first instruction set asked for intermittent teeth and a rigid back.
A replay could start the teeth in column 7 or column 8, and could give
the reinforcing spine one row or two. Both satisfied the wording. The
one-row reconstruction gave the steel the same thin, flexible reading
as the prior release. I returned to the empty canvas, fixed the phase
and depth, and defined every wood run, the seated heel, and the blunt cap.

The first complete expressive draft still let the spine merge into the
face at inventory size. I amended step 4 to require a relief edge and a
shadow below it, then replayed the full recipe before shading all surfaces
on a fresh canvas again.

For the final replay, rebuild the steel one column at a time and the wood
one row at a time. Check this result against a separate row-by-row reading
of the recipe before applying lighting. Perform the same replay after
future changes to the recipe.
