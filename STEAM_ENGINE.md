# Steam engine — new industrial design

The assembled machine is **2 wide × 2 high × 3 deep**. The controller is the lower-left block of the front.

### Ports (relative to the machine front)

| Function | Position from controller | Exterior face | Forge capability |
|---|---|---|---|
| Steam input | Up 1 | Front | `IFluidHandler`, fill only; `examplemod:steam` |
| Condensate output | Back 1 | Left | `IFluidHandler`, drain only; `examplemod:condensate` |
| Electrical output | Right 1 and up 1 | Top/front | FE `IEnergyStorage`, extract only |

The fluid ports are installed automatically as part of the multiblock and are not individual craftable machines. Pipe fluid IDs must match the port fluid. Right-click a fluid port to inspect its tank; compatible fluid containers can be inserted/extracted via right-click.

The steam engine also keeps its original controller-side fluid capability for backwards compatibility. The power port delegates its FE buffer to the real controller (previously it incorrectly checked directly below the power port).

After upgrading an existing world, the controller replaces only the original casing at the designated port positions on its first server tick. It does not overwrite arbitrary nearby blocks.

### Textures

The **`steam_engine_atlas.png`** texture is a 4×4 grid, 256×256 pixels total, with 64×64 pixels per face. All machine models point to this single texture using per-face UV coordinates.

| Slot | Face |
|---|---|
| 0 | Front flywheel and gauges |
| 1 | Rear access panel |
| 2 | Left copper piping |
| 3 | Right copper piping |
| 4 | Top boiler hatch |
| 5 | Bottom armored plate |
| 6 | Repeatable steel casing |
| 7 | Cyan steam inlet |
| 8 | Copper condensate outlet |
| 9 | Gold FE power outlet |

The look is original HBM-inspired dark steel/copper industrial pixel art, not a copy of HBM's assets. For the controller's front face, an existing TESR still renders the animated flywheel on top of the panel.

### Technical notes

- This project targets Forge 1.12.2 / Java 8.
- Steam and condensate are now saved under separate NBT compounds.
- The split fluid port forwards Forge capabilities to the controller's existing steam and condensate tanks. No duplicate tank data is stored in the ports.


## Fully volumetric 3D model (feature/steam-engine-obj-3d)

The assembled machine now renders **one coherent, original OBJ model** over the
entire 2 wide × 2 high × 3 deep multiblock, rather than twelve visible cubic
blocks with a decorative flywheel on the front.

- `src/main/resources/assets/examplemod/models/machines/steam_engine.obj`
  has five named parts: `Base`, `Flywheel`, `Shaft`, `Transmission`,
  `Piston`. It may be edited in Blender using these object names.
- `client/model/SteamEngineModel.java` loads those parts at runtime and
  renders them using the existing `steam_engine_materials.png` palette.
  The renderer uses only Forge 1.12.2 and Java 8 APIs, with no OBJLoader
  dependency.
- The 3D base includes a boiler cylinder with bands, exposed metal chassis,
  radiator grilles, flanged steam/energy/condensate ports, industrial copper
  piping, bearings, bolt details and a mechanical piston housing.
- `Flywheel` rotates; `Shaft` spins at double rotor speed;
  `Transmission` follows an exact fixed-length crank-slider linkage;
  `Piston` slides accordingly.
- The controller, energy port and fluid ports render empty static models.
  This is intentional: their visible shapes are part of the OBJ.
- `BlockSteamEngineCasing.ASSEMBLED` is a saved block state. A casing placed
  alone retains its normal full-cube model; a casing assembled into the
  steam engine uses an empty block model. The controller upgrades older
  assembled casing metadata as the world runs.
- Assembled parts are no longer visually opaque or light-blocking so the
  TESR is not unnaturally black. Server mechanics, block collision and
  existing fluid/FE capability locations are unchanged.

**Port positions (relative to the front):** steam inlet upper front-left,
FE output upper front-right and condensate outlet one block behind the
controller on the machine's left side.

The **item icon** still uses `steam_engine_item.json`; only the installed
multiblock is drawn using the new OBJ.

### Manual validation checklist

1. Build using Java 8 and Forge 1.12.2, then place a new steam engine.
2. Verify free-standing casings stay solid and the assembled machine is 3D.
3. Look at all four orientations and confirm port faces align with connections.
4. Supply steam; confirm spinning wheel, moving rod and piston, FE output,
   and condensate production.
5. Break any casing/port: verify full multiblock teardown and single drop.
6. Reload a saved world built with the older engine; check casing appearance
   updates and no floating cube faces remain.

The HBM model/renderer was consulted for the five-part animation
architecture, but its geometry and textures were not copied.
