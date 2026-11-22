# Auto Melee

A small client-side Forge mod for Minecraft 1.12.2. Hold the attack
button with a melee weapon and it keeps attacking whatever living thing
you're aiming at, swinging each time the attack cooldown is full.

Melee weapons are swords and axes, including modded ones built on them,
plus BetterSurvival's dagger, hammer and battle axe.

Both hands work. The main hand follows the attack button. With RLCombat
installed, the off-hand follows the use button instead, wherever
RLCombat allows that item in the off-hand.

Timing matches a perfectly timed click, so damage per second doesn't
change. When RLCombat is installed, attacks go through its own main-hand
and off-hand attacks; otherwise the vanilla attack is used. Targeting
follows ReachFix when it's present, and Ice and Fire dragons can be hit
through their parts.

There is no config, keybind or HUD. Drop the jar in `mods/` and it
works. Servers don't need it, and none of the mods above are required.

## Building

The build compiles against RLCombat and ReachFix and tests against
BetterSurvival, all taken from a Minecraft instance's `mods` folder. It
looks upward from the project for a `minecraft/mods` folder, or uses
`mods_dir` in `gradle.properties` when that is set.

- `./gradlew build` builds the jar into `build/libs/` and runs the unit
  tests.
- `./gradlew runGameTests` runs the in-game scenarios in a dev client.
  It needs a display.
