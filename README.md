# Tiltfile for GoLand

TextMate highlighting, completion, documentation and parameter hints in one plugin.
The Tilt language server starts automatically when you open a Tiltfile.

## Installation

Requirements: GoLand 2026.2 and [Tilt](https://docs.tilt.dev/install.html).

1. Build the plugin with `task build` or use the existing ZIP.
2. In GoLand, open `Settings > Plugins > Install Plugin from Disk` and select
   `build/distributions/jetbrains-tiltfile-0.1.0.zip`.
3. Restart if prompted, then open a `Tiltfile`.

`task build` prints the ZIP path. Build outputs are ignored by Git and may be hidden
in the IDE project view; use the path above in the plugin installation dialog.

Tilt is detected automatically. Set a custom path under
`Settings > Languages & Frameworks > Tiltfile` if needed.
Syntax highlighting works without Tilt; editor features do not require `tilt up`.

## Development

Requirements: JDK 25 and [Task](https://taskfile.dev).
The Gradle Wrapper is included. The default target is GoLand 2026.2.2.1.

```sh
task          # List tasks
task build    # Build the plugin ZIP
task test     # Run tests
task verify   # Verify plugin configuration, structure and compatibility
task check    # Run tests, build and all checks
task run      # Start GoLand with the plugin in a sandbox
task clean    # Remove build outputs
```

Pass additional Gradle arguments after `--`, for example to use a local IDE:

```sh
task check -- -PplatformLocalPath="/path/to/GoLand.app"
```

The real LSP test runs when Tilt is installed. Diagnostics depend on the Tilt server;
not every syntax error is detected.

## License

[MIT](LICENSE). The bundled grammar has its own
[license notices](textmate/README.md).
