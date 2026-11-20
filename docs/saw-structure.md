# Iron saw reconstruction

This is a reinforced hand saw. The user pushes and pulls a wooden bow to
move a steel plate through wood. A raised spine braces the plate. Teeth
sit along the edge opposite the spine. The projection shows the near
side of the grip and the wide face of the blade, nose up and right.

## Build the visible structure

1. Start with a transparent 16 × 16 canvas. Each coordinate specifies one
   whole pixel. Count x to the right and y down from (0, 0) at the upper
   left. All ranges include both endpoints. Unlisted pixels stay clear.
2. Build the wooden bow from the horizontal runs below. The wider lower
   rim forms a hand stop, tapering to the three-pixel base. The 3 × 3 hole
   at x = 2–4 and y = 10–12 stays empty throughout assembly.

   | Row y | Wood columns x |
   | --- | --- |
   | 7 | 3–5 |
   | 8 | 2–5 |
   | 9 | 1–5 |
   | 10 | 1 and 5–6 |
   | 11–12 | 0–1 and 5–6 |
   | 13 | 0–6 |
   | 14 | 1–5 |
   | 15 | 2–4 |

3. Build the steel column by column. For x from 6 to 15 use
   `s = 6 - floor((x - 6) / 2)` as the upper spine axis. Fill these
   offsets below s. The heel steps out of the wood. The plate keeps its
   full depth through the working span, then tapers by two pixels at the
   blunt nose.

   | Column x | Steel offsets from s |
   | --- | --- |
   | 6 | 0–3 |
   | 7 | 0–5 |
   | 8–14 | 0–6 |
   | 15 | 1–5 |

4. Give the spine a dark outer row at s and a lighter lip at s + 1
   across x = 6–14. The darker shadow at s + 2 separates that lip from
   the broad steel face. The plate light belongs in a few coherent
   clusters. The cutting bevel is the last row of each working column.
   The nose is a short cap with a dark top and bottom. The warm wood
   rail at x = 5 neighbors steel at x = 6 on rows 7–9, and steel at (6, 9)
   rests above wood at (6, 10). Each visible pixel is owned by one material.
5. Add a single steel pixel below the bevel at (8, 12), (10, 11),
   (12, 10), and (14, 9). The alternate positions remain clear. Keep
   the tooth tips in the bevel's value range so they read as part of one
   cutting edge rather than four bright marks.
6. Verify a connected 38-pixel wood mask and connected 68-pixel steel
   mask. The bow encloses exactly the specified 3 × 3 hole when isolated.
   The warm grip, continuous bracing spine and broad steel face must
   be distinguishable at 1×. Tone clusters can change while preserving
   the accepted material runs and join.

## Replay and redraw record

The earlier saw recipe also specified a straight axis, relief edge, four
shallow teeth, and a closed grip. This revision deepens the working plate
and extends the lower palm stop. The rough expressive sketch temporarily
took the spine above the grid's usual alignment and experimented with
oversized teeth, a bright nose, and scattered blue light.

The cleanup redrew the plate as a steady slab, reduced each tooth to one
pixel, enclosed the handle with a thicker lower rim, and kept the spine
highlight while grouping the rest of the steel into a few planes. The
revised recipe was replayed from an empty grid, then independently
checked by horizontal rows. Both constructions agreed on the masks.
