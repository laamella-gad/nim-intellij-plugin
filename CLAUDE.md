# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew runIde          # launch sandbox IntelliJ with plugin loaded (primary dev loop)
./gradlew buildPlugin     # produce distributable ZIP in build/distributions/
./gradlew verifyPlugin    # check compatibility against target platform
./gradlew check           # run tests and inspections
```

> First run: `./gradlew wrapper` to generate the wrapper JAR if it is missing.

## Architecture

This is an IntelliJ Platform plugin that adds Nim language support by wiring **LSP4IJ** (plugin dep `com.redhat.devtools.lsp4ij`) to **nimlangserver** (`nimble install nimlangserver`). LSP4IJ owns all protocol handling; this plugin only configures and launches the server.

### Extension point flow (plugin.xml)

```
com.intellij.fileType          → NimFileType   (.nim, .nims, .nimble)
com.redhat.devtools.lsp4ij:
  server                       → NimLanguageServerFactory (id="nim")
  languageMapping language=Nim → server id="nim"
  fileTypeMapping  fileType=Nim→ server id="nim"
```

### Key classes

| Class | Role |
|---|---|
| `NimLanguage` | Singleton `Language("Nim")` — identity used by LSP4IJ `languageMapping` |
| `NimFileType` | Maps `.nim/.nims/.nimble` to `NimLanguage`; provides icon |
| `NimLanguageServerFactory` | LSP4IJ entry point; creates the connection provider |
| `NimLanguageServerConnectionProvider` | Extends `ProcessStreamConnectionProvider`; resolves `nimlangserver` path from `NimSettings` (falls back to PATH) |
| `NimSettings` | Application-level `PersistentStateComponent` storing `serverPath` |
| `NimSettingsConfigurable` | Settings UI at **Settings → Languages & Frameworks → Nim** |

### LSP4IJ version

Pinned in `build.gradle.kts`. Check current version at: https://plugins.jetbrains.com/plugin/23257-lsp4ij/versions — update the `plugin("com.redhat.devtools.lsp4ij:X.Y.Z")` line if bumping.

### Platform target

Controlled entirely by `gradle.properties` (`platformType`, `platformVersion`, `pluginSinceBuild`, `pluginUntilBuild`). No code changes needed to retarget.
