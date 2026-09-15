# Changelog

All notable changes to Tiltfile for GoLand are documented here.

## [0.1.1] - 2026-09-15

Released.

### Fixed

- Removed both usages of `PathEnvironmentVariableUtil.findInPath(String)`, which
  JetBrains has scheduled for removal.
- Replaced executable lookup with Java NIO while preserving compatibility with
  GoLand 2026.2, the IDE's shell environment, PATH search order, and existing
  fallback locations.

### Tests

- Added six executable lookup regression tests covering PATH order, missing files,
  directories, execute permissions, spaces, invalid entries, relative paths, and
  unsuccessful lookups.

## [0.1.0] - 2026-09-15

Released. Initial release.

### Added

- Bundled TextMate syntax highlighting for `Tiltfile` and `*.tiltfile` files,
  including case-insensitive file name matching.
- Automatic startup of Tilt's language server when a Tiltfile is opened, providing
  code completion, documentation, parameter hints, and server diagnostics.
- Automatic Tilt detection through PATH and common Homebrew and user installation
  locations, plus a configurable executable under
  `Settings > Languages & Frameworks > Tiltfile`.
- Executable validation, support for paths containing spaces, persisted settings,
  and language server restart in open projects when the executable setting changes.
- A missing-executable notification with a shortcut to the plugin settings.
- Syntax highlighting without a local Tilt installation; language server features
  work without running `tilt up`.
- GoLand 2026.2 support with GoLand 2026.2.2.1 as the default build and verification
  target.
- Gradle Wrapper and Task commands for building, testing, verification, running a
  sandbox IDE, and cleaning build outputs.
- Integration tests for plugin registration, highlighting, file recognition,
  executable handling, and settings persistence, plus a smoke test using a real
  Tilt language server.
- English installation and development documentation, an example Tiltfile for
  local highlighting checks, and bundled grammar license notices.
