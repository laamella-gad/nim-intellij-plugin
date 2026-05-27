# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew runIde          # launch sandbox IntelliJ with plugin loaded (primary dev loop)
./gradlew buildPlugin     # produce distributable ZIP in build/distributions/
./gradlew verifyPlugin    # check compatibility against target platform
./gradlew check           # run tests and inspections
./gradlew generateLexer   # regenerate _NimLexer.java from src/main/flex/_NimLexer.flex (auto-runs before compile)
```

> First run: `./gradlew wrapper` to generate the wrapper JAR if it is missing.

## Debugging

Start `runIde` in **Run** mode (not Debug) — it opens port 5005. Then attach the **Debug Plugin** Remote JVM Debug run config. Breakpoints in plugin code will trigger in the sandbox IntelliJ instance.

## Tests

Tests live in `src/test/kotlin/com/laamella/nim/`. Two styles in use:

- **`BasePlatformTestCase`** — JUnit 3-style (method names start with `test`, no `@Test`). Provides a headless IntelliJ project with `myFixture`. The `<caret>` marker in `configureByText` sets the caret. Used for tests that need a real `Project`, `Editor`, or module roots (`NimLineIndentProviderTest`, `NimProjectConfiguratorTest`).
- **Plain JUnit** — `@Test` annotations, no platform. Used for tests that need none of the platform (e.g. `NimNewProjectWizardTest` tests `createNimProjectStructure` with a temp dir).

Nimsuggest error output during test runs is harmless — LSP4IJ tries to start the server and fails gracefully in the sandbox.

## Architecture

This is an IntelliJ Platform plugin that adds Nim language support by wiring **LSP4IJ** (plugin dep `com.redhat.devtools.lsp4ij`) to **nimlangserver** (`nimble install nimlangserver`). LSP4IJ owns all protocol handling; this plugin configures the server and provides native IDE integration.

### Extension point flow (plugin.xml)

```
com.intellij.fileType                        → NimFileType   (.nim, .nims, .nimble, .nimscript)
com.intellij.lang.parserDefinition           → NimParserDefinition
com.intellij.lang.syntaxHighlighterFactory   → NimSyntaxHighlighterFactory
com.intellij.lang.commenter                  → NimCommenter
com.intellij.lang.quoteHandler               → NimQuoteHandler
com.intellij.lang.braceMatcher               → NimBraceMatcher
com.intellij.postStartupActivity             → NimProjectConfigurator
com.intellij.externalFormatProcessor         → NimFormatProcessor
com.intellij.lineIndentProvider              → NimLineIndentProvider
com.intellij.langCodeStyleSettingsProvider   → NimLanguageCodeStyleSettingsProvider
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
| `NimTokenTypes` | `IElementType` constants for all lexer token kinds including bracket tokens; all fields annotated `@JvmField` for Java access from generated lexer |
| `_NimLexer.flex` | JFlex grammar in `src/main/flex/`; generated to `build/generated/sources/grammarkit-lexer/` by `./gradlew generateLexer` (wired into `compileKotlin`); handles keywords, strings (normal/raw/triple/generalized), chars, numbers (all bases + suffixes), nested block/doc-block comments, backtick identifiers |
| `NimLexer` | Thin `FlexAdapter(_NimLexer(null))` wrapper; incremental re-lex state managed by JFlex |
| `NimSyntaxHighlighter` | Maps token types to `DefaultLanguageHighlighterColors` keys |
| `NimSyntaxHighlighterFactory` | Creates `NimSyntaxHighlighter`; registered for `language="Nim"` |
| `NimCommenter` | Line comment `#`, block comment `#[`/`]#` — enables Ctrl+/ |
| `NimQuoteHandler` | Auto-closes `"` and `'` |
| `NimBraceMatcher` | Highlights matching `()`, `[]`, `{}` pairs |
| `NimProjectConfigurator` | `ProjectActivity` — reads `.nimble` on project open; creates module if absent, creates `srcDir`/`binDir` if missing, marks them as source root / excluded; marks `tests/` as test source root if it exists. In `projectconfig/` |
| `NimNimbleFileListener` | `BulkFileListener` registered via `projectListeners` — re-runs `configureNimProject` when the `.nimble` file changes or is created. In `projectconfig/` |
| `configureNimProject` | Top-level function shared by `NimProjectConfigurator` and `NimNimbleFileListener`; performs all `.nimble`-driven project configuration. In `projectconfig/` |
| `configureNimLibraries` | Runs `nimble deps --format:json` in a pooled thread, resolves installed packages from `~/.nimble/pkgs2/`, and adds them as project libraries linked to the module; stale libs (no longer in deps) are removed. In `projectconfig/` |
| `configureNimStdlib` | Runs `nim --version` to find the version, locates `pkgs2/nim-VERSION-*/lib/`, and registers it as a project library named `"Nim"`. In `projectconfig/` |
| `NimFormatProcessor` | `ExternalFormatProcessor` — runs `nimpretty` on Reformat Code; shows warning balloon if not on PATH |
| `NimLineIndentProvider` | `LineIndentProvider` — computes Enter-key indentation by inspecting the previous line's last non-comment character; delegates shared helpers `nimOpensBlock`/`stripTrailingComment` |
| `NimLanguageCodeStyleSettingsProvider` | Sets default indent/tab size to 2 spaces for Nim files |
| `NimPackageType` | Enum: `BINARY`, `LIBRARY`, `HYBRID` — controls .nimble fields and generated source files to match `nimble init` output |
| `NimNewProjectWizard` | `LanguageGeneratorNewProjectWizard` — File → New Project → Nim; exposes package type, version, author, description, and license (SPDX combo) fields; detects installed Nim version via `nim --version` for the `requires` constraint; delegates file creation to `createNimProjectStructure` |
| `createNimProjectStructure` | Package-level function in `newproject/`; generates `*.nimble`, `src/name.nim`, and (for Library/Hybrid) `src/name/submodule.nim`; `bin/` only for Binary and Hybrid; `DEFAULT_NIM_VERSION = "2.0.0"` used as fallback `requires` version |
| `NimLanguageServerFactory` | LSP4IJ entry point; creates `OSProcessStreamConnectionProvider` launching `nimlangserver`; prepends `nimbleBinPath` to PATH for the subprocess; client features (`isUseIntAsJsonRpcId=true`); returns `NimLanguageServerInstaller` from `createServerInstaller()` |
| `NimLanguageServerInstaller` | `LanguageServerInstallerBase` — `checkServerInstalled()` tests exe via `File.canExecute()` (or PATH search for bare names); `install()` runs `nimble install --accept --useSystemNim nimlangserver`, prepending `nimbleBinPath` to PATH so `nim` is findable when IntelliJ was launched without the toolchain on PATH |
| `NimSettings` | Application-level `PersistentStateComponent`; stores `nimbleBinPath` (toolchain directory) and `nimlangserverExe`/`nimbleExe`/`nimprettyExe` (filenames, default to `tool.exe` on Windows / bare name on Unix); `exePath(exe)` combines them; helpers `nimlangserver()`/`nimble()`/`nimpretty()`/`nim()` for callers |
| `NimSettingsConfigurable` | Settings UI at **Settings → Languages & Frameworks → Nim** |

### Known workarounds

**Enter-key indentation uses `LineIndentProvider`, not `ExternalFormatProcessor.indent()`** — `ExternalFormatProcessor.indent()` is called during explicit reformat operations only, not on Enter. Enter-key auto-indent routes through `LineIndentProvider` (`com.intellij.lineIndentProvider` EP). `NimLineIndentProvider` owns this; `NimFormatProcessor.indent()` is a no-op stub required by the interface.

**Integer JSON-RPC IDs** — nimlangserver only includes `id` in responses when the request id is a JSON number (lsp4j defaults to strings). Fixed by overriding `isUseIntAsJsonRpcId()` in `NimLanguageServerFactory.createClientFeatures()`. LSP4IJ 0.19.3 uses `DefaultLauncherBuilder.RemoteEndpointWithIdAsInt` when this returns `true`. A bug has been filed upstream against nimlangserver.

### LSP4IJ version

Pinned in `build.gradle.kts`. Check current version at: https://plugins.jetbrains.com/plugin/23257-lsp4ij/versions — update the `plugin("com.redhat.devtools.lsp4ij:X.Y.Z")` line if bumping.

### Platform target

Controlled entirely by `gradle.properties` (`platformType`, `platformVersion`, `pluginSinceBuild`, `pluginUntilBuild`). No code changes needed to retarget.

## Issue tracker

https://codeberg.org/laamella-gad/nim-intellij-plugin/issues

## License

EPL-2.0 (Eclipse Public License 2.0) — see `LICENSE`.

Third-party assets are documented in `NOTICE`. The plugin icon (`META-INF/pluginIcon.svg`) is the official Nim logo from https://commons.wikimedia.org/wiki/File:Nim_logo.svg, MIT-licensed by the Nim project; the embedded PNG reference layer has been stripped leaving only vector paths.
