# Workshop request tracking

The daily Obsidian Workshop check records the exact text of each highlighted
request here before implementation starts. Once work starts, its highlight
markers are removed from the note. In-progress entries remain here until their
implementation and verification are complete, so unhighlighting cannot lose work.

## Ocean zones

Status: Completed. The integrated Minecraft playtest passed, including water at sea level, a submerged ocean floor, and the creative map.

> Add ocean zones to the mod, obviously these ocean zones generate and cluster together to make large oceans. Beaches or cliffs form next to the ocean. In the ocean are also island zones that have 1-2 islands. There are also archipelago zones that have a bunch of clustered islands.


## Carpentry and boats

Status: Completed. The integrated Minecraft playtest verified hot iron and handle shaping, saw assembly, slab cutting, assembling the table, crafting an oar and a boat, and linked table removal.

> Add a carpenter’s table to the mod. The carpenters table is two blocks wide and is made by shift right clicking two places adjacent planks with a new saw item. Saws are only iron right now. Make the carpenters table look like a woodshop table with tools on the side and four legs. Make it have pieces of wood on it and make it look heavy duty and cool.

> Now the boat is made by putting glue (it looks like slime just a tan/white color) or a slimeball on the table with two plank slabs and two oars.

> Plank slabs are made by right clicking a saw on a wood plank on the workbench (the one made from a log) and it returns two slabs. An oar is made by a stick and a slab on the carpenters table and right clicking with the saw.

> A saw is made from a saw blade and a saw handle. The saw handle is made by using a hammer on a plank on the anvil. The saw blade is made by taking a piece of hot iron from the iron furnace and while it’s still hot putting it on the anvil and right clicking. The two parts can be combine with a hammer on the anvil.

## Coastlines, seasons, ground crafting, and campfires

Status: Completed. The integrated playtest passed, including seasonal foliage opacity, ground crafting, five-minute campfire cooking and retrieval, torch placement, burnout, relighting, water extinguishing, and the creative tab and map.

> Leaves are not rendering properly, I'm guessing because of the seasons. The beaches/land that is generated next to the ocean zones should be for the most part ordinary land that has its characteristics linked to the zones next to it. Beaches should be relatively thin and on the very edge of the ocean.

> Torches are made by right clicking sticks on a campfire, the stick is consumed and a torch pops into the world. The campfire also cooks food, it takes 5 minutes generally. The campfire holds up to four items, and they are placed on the four corners of the campfire. Once done they don't overcook but you can rightclick the campfire to take them off. Torches burn out after 40 in game minecraft days (4 seasons/one year) they become unlit torches, and you can relight them with a flint and steel. Water also delights torches (instead of destroying them)

> Once you switch seasons in the map, everything unrenders. Also make the wood block in the stack to make the wooden hoe smaller and look like it's stacked on top of the other items (or under them)

> You have to shift slick to place planks or any future block on the ground for crafting

## Beach grade, hammer upgrade, and ignition crafting

Status: Completed. Build and integrated playtest passed, including campfire assembly and ignition, fire starter durability, stone hammer conversion, and the flint and steel recipe.

> Right now the beach goes below sea level turns into a biome and creates a wall up to sea level. It should remain beach all the way to the sea, and stop at sea level.

> Stone Hammers should be able to be upgraded to iron with an ingot on the anvil

> A campfire is made by shift clicking 4 logs onto the ground. They are placed one by one as they are in the campfire model. Once all four are placed a flint and steel is used to light the fire.

> Make a new fire starter tool that's made by placing two sticks and right clicking on the stack on the ground. It does everything a flint and steel does just it only has 30 uses

> Speaking of flint and steel that's made by using a hammer on flint and steel on the anvil

## Sailboats

Status: Completed. The integrated playtest verified carpenter's table assembly, entity spawning, recovery as a sailboat, fifteen percent more forward thrust on water, the creative tab and the Creative Zone Map. The item model was reviewed beside the oak boat.

> Sailboats. Make a boat with an added sail to it, and make it go a little faster than an ordinary boat.


## Three-wide railway and steam locomotive

Status: In progress. The milestone passed the Forge build and integrated playtest. It covers the three-block railway, locomotive and tender, controls, three-coal startup, five-minute moving fuel intervals, support collapse, coal generation, the creative tab and the Creative Zone Map. Routes are straight and level, and the build uses the existing workbench and carpenter’s table. The remaining fuel alternatives need the design of a villager hiring and mining system and a magic heat source. Neither system exists in the mod yet. The documentary line is a personal research note.

> The Train

> 3 blocks tall and wide, and 10 blocks long. The Coal cart is 4 blocks long. The train has 4 sets of big wheels and 1 set of small wheels at the front. It has a bell and a smock stack. It has a large locomotive lamp on the top of the front. The carriage has 2 levers, a throttle lever, and a brake lever. Inside the carriage is also the furnace and a chute from the coal cart. To get the locomotive to full steam, it takes 3 coal, then 1 coal per 5 minutes of the train going. Now the thing is that it's not super fun to have to use coal every time you use a train tbh, but there are easy ways to get coal. If you have access to a coal mine, it's everywhere, you can hire villagers to mine for you, and there are other ways to make it work out, for instance you can use magic to heat up the water instead actually.

> The Tracks

> The tracks are three blocks wide. The player must assemble them by placing down rails and sleepers and hammering in the ties (not actually laborious you just gotta have the hammer in one hand and the sleepers in the other). The rails are placed as blocks, and you can change their orientation with the hammer. Placed rails will copy the orientation of the rail they're placed next to. The same with the sleepers. A rail or sleeper placed on sand or other unsteady ground will break the block and drop like an anvil if a train passes over it.

> The Assembly

> Gee I'm gonna have to watch a train documentary for this.

> So... actually I slept on the idea and I realized I needed to scale it down a bit. And this is important for the next lesson, which is, that since we are not perfect, and we are relying on a concept of perfection that exists outside of ourselves, we should never rely on our own work as the standard, instead we should allow change in the pursuit of this perfection.

> Now what I realized was that I hadn't implemented the idea of trains with the rest of the game in mind.

> In fact it goes far deeper than that, I totally forgot to think what trains are supposed to even do!! That's how mixed up you can get.

> So what is the train supposed to do in the world?

> It's a fast and easy way to get from one set place to another.

> So if it's a fast and easy way, then I can't make the construction of the tracks and train too cumbersome.

> I think I should make the train and tracks 3 blocks wide instead of 5. The coal use still makes sense, in my head, long distance travel should be a big consideration in this mod. But I have to ensure that using the train has advantages over other forms of transport, because the train is still expensive, takes preparation and thought, costs something to use, and is set to wherever the tracks go. So that must be a large consideration.
