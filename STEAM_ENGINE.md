# Steam engine — enclosed copper industrial redesign

This document describes the working implementation on
`feature/steam-engine-obj-3d` for **Minecraft Forge 1.12.2 / Java 8**.

## Construction and appearance

The machine is a **2 × 2 × 3** multiblock, using the same controller and
structural block registry IDs as before. The controller is the bottom-left
block of the front when facing the flywheel.

- **Closed case:** the installed engine is rendered as an original, fully
  enclosed copper-lined/gunmetal OBJ machine. There are no open cage walls.
- **Animated front:** a larger, 24-segment copper flywheel, eight spokes,
  an offset crank, a copper connecting rod, a geared shaft and a moving
  copper piston. Named OBJ groups stay `Base`, `Flywheel`, `Shaft`,
  `Transmission`, and `Piston`.
- **Material atlas:** `textures/blocks/steam_engine_materials.png`
  contains gunmetal, copper, brass and dark gasket materials.
- **Inventory item:** `steam_engine_copper_item_front.png` is the static
  copper flywheel face; `steam_engine_copper_casing.png` is the solid
  matching block casing texture.
- The OBJ is loaded by
  `com.example.examplemod.client.model.SteamEngineModel` using the
  existing tiny Wavefront parser. No new model loader dependency is added.

The assembled block models remain visually empty, because the controller's
TESR renders the *entire* multiblock. Free-standing steam-engine casing
still renders as an ordinary full block.

## Rear-only physical connections

**All connections face the REAR**, opposite the flywheel/front direction.
Connect pipes/cables to the outside of the rear (third depth) layer.

| Function | Structure coordinates (width, height, depth) | Visual | Capability |
|---|---|---|---|
| Steam input | `(0,1,2)` | Upper black circular socket | `IFluidHandler` fill-only, steam |
| Condensate output | `(0,0,2)` | Lower black circular socket | `IFluidHandler` drain-only, condensate |
| FE output | `(1,1,2)` | Protruding copper cable/gland | `IEnergyStorage`, extraction only |

Positions are relative to the front lower-left controller; the `width`
axis is the controller facing's `rotateY()`. The rear external
`EnumFacing` is always `engine.getFacing().getOpposite()`.

The two fluid sockets have separate capability handlers and use the
existing controller tanks. The FE cable delegates to the same stored
energy buffer as before. No new inventory or duplicate fluid storage
is created.

The controller itself no longer exposes *sided* fluid access at the
front/left. Un-sided internal fluid access remains for compatibility.

## Save migration and dismantling

On the first server tick after load, the controller reconciles the
eleven existing structural part positions:

- Any old front steam/energy or left condensate port is converted into
  assembled casing.
- The three new rear slots receive their actual fluid/energy port
  blocks. No unrelated neighboring block is overwritten.
- The conversion runs under `SteamEngineStructure.reconfigure()`, which
  blocks the normal break-triggered teardown while positions are moved.
- Both fluid contents and saved FE remain on the controller; they are not
  moved into the port blocks.
- Later breaking a normal part still dismantles the **whole** multiblock
  as usual, with one engine item in survival and none in creative.

Old pipes connected to the previous faces must be reconnected to the
new rear panel.

## Manual verification

1. Pull branch `feature/steam-engine-obj-3d`, run `gradlew.bat clean runClient`.
2. Place a new engine facing each of the four horizontal directions.
3. Confirm that the copper body is closed and the flywheel, rod and
   piston animate without leaving the front casing.
4. Confirm that BOTH black pipe sockets and the copper cable are
   on the *same rear wall*, with neither an extra top nor side port.
5. Connect a fluid pipe to the upper black socket and supply steam.
6. Verify that condensate is available through the lower socket and
   power extracts only through the rear FE port.
7. Reload an existing world and inspect the automatic relocation of old
   ports. Reconnect existing pipes to the rear side.
8. Break controller, casing, steam inlet, condensate outlet and FE port
   separately in test worlds; each must dismantle the entire structure,
   without item duplication.
9. Ensure a stand-alone crafted casing retains its own block model.

## Design notes

The HBM CE steam engine renderer was inspected for the practice of
separately animated `Base`, `Flywheel`, `Shaft`, `Transmission` and
`Piston` groups, but this repo uses its own OBJ geometry, UV/materials,
and visual design rather than copying HBM art assets.
