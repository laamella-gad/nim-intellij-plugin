# Nim IntelliJ Plugin

Nim language support for IntelliJ IDEA and other JetBrains IDEs, powered by [nimlangserver](https://github.com/nim-lang/langserver) via [LSP4IJ](https://github.com/redhat-developer/lsp4ij).

This is not official in any way.
It is just a slopped together plugin that works well enough.

## Features

Provided by the plugin:

- Syntax highlighting
- File type recognition (`.nim`, `.nims`, `.nimble`)
- Project setup from `.nimble` file (srcDir, binDir, tests/)
- Code formatting via `nimpretty` (Reformat Code / Ctrl+Alt+L)
- Comment/uncomment (Ctrl+/)
- Auto-close quotes and brackets
- Runner/test runner. Right-click the nimble file.
- New Project wizard (File → New Project → Nim)

Provided by nimlangserver over LSP:

- Code completion
- Hover documentation
- Go to definition
- Find usages
- Highlight all occurrences of symbol under cursor
- Diagnostics
- Inlay hints
- Folding
- Structure view
- Signature help

## Requirements

- IntelliJ IDEA 2026.1 (Community or Ultimate)
- [Nim and Nimble](https://nim-lang.org/install.html) installed
- [nimlangserver](https://github.com/nim-lang/langserver) — auto-installed by the plugin if missing; or install manually: `nimble install nimlangserver`
- [nimpretty](https://nim-lang.org/docs/nimpretty.html) for formatting: `nimble install nimpretty` (optional)

## Installation

Install from the JetBrains Marketplace (search "Nim"), or build from source:

```bash
./gradlew buildPlugin
```

Then install the ZIP from `build/distributions/` via **Settings → Plugins → Install Plugin from Disk**.

## Configuration

**Settings → Languages & Frameworks → Nim**:
- **Nim toolchain path** — directory containing the toolchain binaries (default: `/home/<username>/.nimble/bin`)
- **nimlangserver / nimble / nimpretty executable name** — filename of each tool within that directory (defaults: `nimlangserver`, `nimble`, `nimpretty`)

## Contributing

Contributions are welcome. Bug reports, pull requests, and feature suggestions are all appreciated.

## License

EPL-2.0 — see [LICENSE](LICENSE). Third-party asset attributions in [NOTICE](NOTICE).
