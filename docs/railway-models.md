# Railway model reconstruction

Railway items use 3D forms and stock vanilla materials. The rail cross
section is a narrow metal foot, narrow stem, and wider running head over
a low wooden base. Along the track axis the bar runs through the entire
block. The sleeper is a single two-unit timber laid across the axis, with
a pair of iron ties after hammering. Fit each piece to the full block
grid and leave the running surface above both wooden parts.

The engine is a three-by-three-by-ten-block volume. The group is oriented
with the boiler at the positive-Z end. Item model coordinates are the
engine's frame projected onto a 16-unit cube. The world renderer expands
the axes by 3, 3, and 10. The tender uses the same frame convention but
4 units of world length and begins immediately behind the engine. The
working aesthetic is a heavy black boiler, dark riveted frame, and clear
warm details for the lamp, bell, levers and fire door. Vanilla minecarts,
furnaces, iron blocks, exposed copper and spruce provide the material
references.

1. Build an iron frame across x = 1–15 at y = 3–5. Its z bounds are 0–16.
2. At z = 2, 5, 8 and 11 put paired driving wheels against both sides of
   the frame. Use black stepped octagons and an iron hub. They span y = 0
   through 6. Put the smaller paired front wheels at z = 14, y = 0–4.
3. Put the boiler at z = 5–14. Stack a broad middle section between
   narrower upper and lower slabs. Close the front with an iron door.
4. At the back, put a floor, two side posts, upright rear panel and cab
   roof. Leave separate open windows, and install a furnace door facing
   the driver. Place the throttle and brake on opposite sides. Leave a
   coal chute toward the tender.
5. Put a gray chimney and wide black cap toward the front of the boiler.
   Place an exposed-copper bell along the spine. Set a gold lamp with a
   brighter yellow center at the high front face.
6. Give the tender a frame and two sets of smaller wheels. Its raised
   sides frame one continuous bed of visible coal.

The resolved version makes the bell, chimney and lamp three independent
clusters. Rail items are rendered beside an iron rail and wooden plank.
The train is compared with the stock furnace minecart. Check the exact
frame, wheel count, tender offset, and clearance separately from color.

## Building and running a line

- Hammer one iron ingot on a workbench for 16 rails. Hammer a plank on a
  workbench for eight sleepers.
- Lay rails in two parallel rows with a one-block gap. Hold sleepers in the
  other hand and hammer either rail to tie that section. With a sleeper
  already placed in the gap, the same action fastens it. Sneak-hammer a
  piece to rotate it.
- Hammer three iron ingots and two planks on a carpenter's table to make the
  engine and coupled tender. Face along a level, straight route and place it
  on a tied sleeper with enough track for both parts.
- Right-click the tender with coal or charcoal. Sneak-right-click to load
  multiple pieces. Right-click the cab to board. Forward and back set the
  throttle, jump toggles the brake, and sneak dismounts. From outside the
  cab, sneak-right-click its left or right side for the throttle or brake.
  Right-click the front to ring the bell.

The core line is level and straight on either horizontal axis. A train
travels in both directions and stops before missing, obstructed, or
unloaded track. A fully tied section carries the load only if all three
positions have sound support. The weight of the train removes loose support
and makes those track pieces fall.

The settled inventory comparison uses the vanilla furnace minecart, rail,
and spruce planks. The new rail is one running bar and the sleeper one
timber. Their full assembly appears across three world blocks. The model
audit checked four large wheel centers and one small front pair, the
lit front lens, the separate tender and its coal load, and both axes of
each block model. The final models use vanilla textures directly.
