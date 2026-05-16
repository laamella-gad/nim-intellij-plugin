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
com.intellij.fileType                        → NimFileType   (.nim, .nims, .nimble)
com.intellij.lang.parserDefinition           → NimParserDefinition
com.intellij.lang.syntaxHighlighterFactory   → NimSyntaxHighlighterFactory
com.intellij.lang.commenter                  → NimCommenter
com.intellij.lang.quoteHandler               → NimQuoteHandler
com.intellij.lang.braceMatcher               → NimBraceMatcher
com.intellij.postStartupActivity             → NimProjectConfigurator
com.intellij.externalFormatProcessor         → NimFormatProcessor
projectListeners (BulkFileListener)          → NimNimbleFileListener
com.redhat.devtools.lsp4ij:
  server                                     → NimLanguageServerFactory (id="nim")
  languageMapping language=Nim               → server id="nim"
  fileTypeMapping  fileType=Nim              → server id="nim"
```

### Key classes

| Class | Role |
|---|---|
| `NimLanguage` | Singleton `Language("Nim")` — identity used by LSP4IJ `languageMapping` |
| `NimFileType` | Maps `.nim/.nims/.nimble` to `NimLanguage`; provides icon |
| `NimFile` | `PsiFileBase` subclass — required so PSI files carry `NimLanguage` (enables commenter lookup) |
| `NimParserDefinition` | Minimal `ParserDefinition` — flat token tree, no real parser; provides lexer, comment/string token sets, and `NimFile` factory |
| `NimTokenTypes` | `IElementType` constants for all lexer token kinds including bracket tokens |
| `NimLexer` | Hand-written `LexerBase` — keywords, strings, chars, numbers, nested block comments, doc comments, bracket tokens |
| `NimSyntaxHighlighter` | Maps token types to `DefaultLanguageHighlighterColors` keys |
| `NimSyntaxHighlighterFactory` | Creates `NimSyntaxHighlighter`; registered for `language="Nim"` |
| `NimCommenter` | Line comment `#`, block comment `#[`/`]#` — enables Ctrl+/ |
| `NimQuoteHandler` | Auto-closes `"` and `'` |
| `NimBraceMatcher` | Highlights matching `()`, `[]`, `{}` pairs |
| `NimProjectConfigurator` | `ProjectActivity` — reads `.nimble` on project open; creates module if absent, creates `srcDir`/`binDir` if missing, marks them as source root / excluded |
| `NimNimbleFileListener` | `BulkFileListener` registered via `projectListeners` — re-runs `configureNimProject` when the `.nimble` file changes or is created |
| `configureNimProject` | Top-level function shared by `NimProjectConfigurator` and `NimNimbleFileListener`; performs all `.nimble`-driven project configuration |
| `NimFormatProcessor` | `ExternalFormatProcessor` — runs `nimpretty` on Reformat Code; shows warning balloon if not on PATH |
| `NimLanguageServerFactory` | LSP4IJ entry point; creates connection provider and client features (`isUseIntAsJsonRpcId=true`) |
| `NimLanguageServerConnectionProvider` | Extends `ProcessStreamConnectionProvider`; launches `nimlangserver` |
| `NimSettings` | Application-level `PersistentStateComponent` storing `serverPath` |
| `NimSettingsConfigurable` | Settings UI at **Settings → Languages & Frameworks → Nim** |

### Known workarounds

**Integer JSON-RPC IDs** — nimlangserver only includes `id` in responses when the request id is a JSON number (lsp4j defaults to strings). Fixed by overriding `isUseIntAsJsonRpcId()` in `NimLanguageServerFactory.createClientFeatures()`. LSP4IJ 0.19.3 uses `DefaultLauncherBuilder.RemoteEndpointWithIdAsInt` when this returns `true`. A bug has been filed upstream against nimlangserver.

### LSP4IJ version

Pinned in `build.gradle.kts`. Check current version at: https://plugins.jetbrains.com/plugin/23257-lsp4ij/versions — update the `plugin("com.redhat.devtools.lsp4ij:X.Y.Z")` line if bumping.

### Platform target

Controlled entirely by `gradle.properties` (`platformType`, `platformVersion`, `pluginSinceBuild`, `pluginUntilBuild`). No code changes needed to retarget.

## License

EPL-2.0 (Eclipse Public License 2.0) — see `LICENSE`.

Third-party assets are documented in `NOTICE`. The plugin icon (`META-INF/pluginIcon.svg`) is the official Nim logo from https://commons.wikimedia.org/wiki/File:Nim_logo.svg, MIT-licensed by the Nim project; the embedded PNG reference layer has been stripped leaving only vector paths.
