# Bundled Tiltfile grammar

Vendored from [tilt-dev/tiltfile.tmbundle](https://github.com/tilt-dev/tiltfile.tmbundle)
at commit `5846e38f058f4a3d9638da7999725dfb4293f85f`.

The upstream Apache 2.0 license and the grammar's original MIT license are included
in the bundle. The grammar derives from MagicStack's Python grammar and the Bazel
team's Starlark adaptation. See `tiltfile.tmbundle/Syntaxes/Tiltfile.tmLanguage.license`.

The bundle is shipped as a directory inside the plugin ZIP. `TextMateBundleProvider`
registers it automatically for the lifetime of the plugin. No user bundle settings
are modified.

