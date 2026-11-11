# BhopUI

Fabric client mod for Minecraft 26.3: spring-based menu animations. Mixins only, no Fabric API.
Build with JDK 25 (`JAVA_HOME=~/.jdks/jdk-25.0.4.1+1`), `./gradlew build`; test with `./gradlew runClient`.
26.3 is unobfuscated (Mojang names); GUI drawing is `GuiGraphicsExtractor` / `extractRenderState`, check names with
`javap` on `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-clientonly-deobf/26.3/*.jar`.

Installed in Lunar profiles `dlss-test` and `just-optimized` (`~/.lunarclient/profiles/<name>/mods`); replace the jar there on release.

## Release

Bump `version` in `gradle.properties`, build, commit, push, then Modrinth (token `MODRINTH_TOKEN` in `../siris/.env`,
header `Authorization: <token>`): project `bhopui` (id A9Dg5EJy). New version: POST /v2/version multipart (`data` JSON +
`file`; version_type release, game_versions 26.3, loaders fabric, featured, no deps), then PATCH /v3/version/<id>
`{"environment":"client_only"}`.

- 1.0.1 (2026-11-11): new mod icon (bhop jumper on a speed slider, drawn by `icon/make_icon.py`; 512 px version is the Modrinth project icon).
- 1.0.0 (2026-11-11): first release, Modrinth version nJgFlh1M. Project submitted for review the same day.
