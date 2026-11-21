# LightningClient - Decompiled & Cracked

A shitty client with the code stolen from NightClient and Homovore, nice obf, security and brain cell ass.

Imagine pasting code from other clients, slapping a garbage authentication gate on top, and praying nobody reverses your clown-tier security in five minutes.

## "Uncrackable" Security in Action

Buddy thought his client was slick

![Fucked by FPG](menu.jpg)

## Credits & Slander

- **Original source dump:** Doom33
- **Reverse engineering & technical cleanup:** Fixed decompiled garbage variables, de-obfuscated CFR ternary messes, completely bypassed auth/heartbeat gates, resolved synthetic lambda format errors, and made this mess actually compile from clean source by 2c6r with da AI.
- **Public release:** Shoutout to FPG for making this client public!

## What Was Done

- Ripped out all remote authentication checks, license gates, and remote heartbeats. Client runs fully offline without checking with anyone's server.
- Renamed decompiled variables across crystal placement, combat aura, mining modules, and rotation managers so the code is actually readable instead of pure single-letter garbage.
- Fixed decompiled synthetic lambda method names that triggered JVM ClassFormatErrors on launch.
- Repaired broken CFR bytecode artifacts and invalid exception handling across classloaders.
- Added working standalone Gradle wrapper and desktop launcher script.

## Building & Running

Requirements: Java 25

```bash
# Build the mod JAR
./gradlew assemble

# Run directly
./gradlew runClient
```

Pre-compiled release JARs are available on the GitHub Releases page.
