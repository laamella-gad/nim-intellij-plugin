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

This is an IntelliJ Platform plugin that adds Nim language support by wiring **LSP4IJ** (plugin dep `com.redhat.devtools.lsp4ij`) to **nimlangserver** (`nimble install nimlangserver`). LSP4IJ owns all protocol handling; this plugin configures the server and provides native IDE integration.

### Extension point flow (plugin.xml)

```
com.intellij.fileType                  → NimFileType   (.nim, .nims, .nimble)
com.intellij.lang.syntaxHighlighter    → NimSyntaxHighlighterFactory
com.intellij.directoryProjectConfigurator → NimProjectConfigurator
com.intellij.nonProjectFileWritingAccessExtension → NimWritingAccessExtension
com.redhat.devtools.lsp4ij:
  server                               → NimLanguageServerFactory (id="nim")
  languageMapping language=Nim         → server id="nim"
  fileTypeMapping  fileType=Nim        → server id="nim"
```

### Key classes

| Class | Role |
|---|---|
| `NimLanguage` | Singleton `Language("Nim")` — identity used by LSP4IJ `languageMapping` |
| `NimFileType` | Maps `.nim/.nims/.nimble` to `NimLanguage`; provides icon |
| `NimTokenTypes` | `IElementType` constants for all lexer token kinds |
| `NimLexer` | Hand-written `LexerBase` — keywords, strings, chars, numbers, nested block comments, doc comments |
| `NimSyntaxHighlighter` | Maps token types to `DefaultLanguageHighlighterColors` keys |
| `NimSyntaxHighlighterFactory` | Creates `NimSyntaxHighlighter`; registered for `language="Nim"` |
| `NimProjectConfigurator` | `DirectoryProjectConfigurator` — finds `.nimble` on project open, marks `srcDir` as source root and `binDir` as excluded |
| `NimWritingAccessExtension` | Allows editing Nim files outside content roots (fallback for projects without `.nimble`) |
| `NimLanguageServerFactory` | LSP4IJ entry point; creates the connection provider |
| `NimLanguageServerConnectionProvider` | Extends `ProcessStreamConnectionProvider`; launches `nimlangserver --stdio`; wraps output stream with `LspIdOutputStream` |
| `LspIdOutputStream` | Rewrites lsp4j string request IDs (`"id":"1"`) to JSON numbers (`"id":1`) — nimlangserver only echoes the id when it is a number |
| `NimSettings` | Application-level `PersistentStateComponent` storing `serverPath` |
| `NimSettingsConfigurable` | Settings UI at **Settings → Languages & Frameworks → Nim** |

### Known workarounds

**`LspIdOutputStream`** — lsp4j (used internally by LSP4IJ) generates request IDs as JSON strings; nimlangserver only includes `id` in responses when the request id is a JSON number. Without this fix the server stays in "starting" state permanently. A bug has been filed upstream against nimlangserver. Once fixed, `LspIdOutputStream` can be removed and `getOutputStream()` override deleted.

### LSP4IJ version

Pinned in `build.gradle.kts`. Check current version at: https://plugins.jetbrains.com/plugin/23257-lsp4ij/versions — update the `plugin("com.redhat.devtools.lsp4ij:X.Y.Z")` line if bumping.

### Platform target

Controlled entirely by `gradle.properties` (`platformType`, `platformVersion`, `pluginSinceBuild`, `pluginUntilBuild`). No code changes needed to retarget.

## License

EPL-2.0 (Eclipse Public License 2.0) — see `LICENSE`.

Third-party assets are documented in `NOTICE`. The plugin icon (`META-INF/pluginIcon.svg`) is the official Nim logo from https://commons.wikimedia.org/wiki/File:Nim_logo.svg, MIT-licensed by the Nim project; the embedded PNG reference layer has been stripped leaving only vector paths.
