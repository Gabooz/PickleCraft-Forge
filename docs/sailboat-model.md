# Sailboat item and rig

Use a 3D inventory model because the cloth is transverse to the hull and
the mast rises from inside it. Use the existing oak boat as the counterpart.
The placed boat uses the actual vanilla oak hull model, including oars.
The item's wooden hull, mast, and cloth use the original vanilla oak planks,
stripped oak log, white wool, and light gray wool resources.

The desired impression is of a light, practical watercraft. Put the front at
the low-x end. Use model units with y up and z across the boat. Enclose the
interior with a flat sole at y = 1–3, long side walls at y = 3–5, and two
short sloping-step ends. Center a mast at x = 6 and z = 8. Put the yard
above the cabin area across z. The drop from yard to foot is a continuous
white panel, with a narrower bottom and a single pale gray hem. Set the
inventory angle to show wood on two faces and expose the broad sail face.
The shared rig resource is also rendered over the vanilla placed hull.

| Part | Min xyz | Max xyz | Material |
| --- | --- | --- | --- |
| Sole | 1,1,4 | 15,3,12 | Oak planks |
| Near wall | 2,3,3 | 14,5,4 | Oak planks |
| Far wall | 2,3,12 | 14,5,13 | Oak planks |
| Bow | 1,3,4 | 3,5,12 | Oak planks |
| Stern | 13,3,4 | 15,5,12 | Oak planks |
| Mast | 5,3,7 | 7,16,9 | Stripped oak |
| Yard | 5,14,2 | 7,15,14 | Stripped oak |
| Cloth | 7,9,3 | 8,14,13 | White wool |
| Foot | 7,8,4 | 8,9,12 | White wool |
| Hem | 7,7,4 | 8,8,12 | Light gray wool |

The mast and spar sit immediately behind the cloth at x = 7. The hem
fills the lowest row adjoining the foot. The independent replay exposed
coincident mast and cloth faces in the first construction. The finished
cloth and mast share a boundary and have disjoint volumes.

The clean form uses two cloth widths and one hem. Smaller triangular
steps and extra trim would make the wool read as multiple objects in the
inventory slot. Check that hull, dark mast and bright cloth remain
separate at inventory size, the mast penetrates the deck, and the
cloth touches its spar. The boat uses stock material pixels, including
their existing mipmaps, on all mapped faces.
