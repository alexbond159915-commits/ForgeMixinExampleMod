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
