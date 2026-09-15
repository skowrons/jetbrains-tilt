# Tiltfile für GoLand

TextMate-Highlighting, Autovervollständigung, Dokumentation und Parameterhilfe in
einem Plugin. Der Tilt-Language-Server startet automatisch beim Öffnen einer Tiltfile.

## Installation

Voraussetzungen: GoLand 2026.2 und [Tilt](https://docs.tilt.dev/install.html).

1. Plugin mit `task build` bauen oder die fertige ZIP verwenden.
2. In GoLand unter `Settings > Plugins > Install Plugin from Disk` die Datei
   `build/distributions/jetbrains-tiltfile-0.1.0.zip` auswählen.
3. Falls angefordert neu starten, dann eine `Tiltfile` öffnen.

Tilt wird automatisch gefunden. Einen abweichenden Pfad unter
`Settings > Languages & Frameworks > Tiltfile` eintragen.
Syntax-Highlighting funktioniert auch ohne Tilt; `tilt up` ist für die Editorhilfe nicht nötig.

## Entwicklung

Voraussetzungen: JDK 25 und [Task](https://taskfile.dev).
Der Gradle Wrapper ist enthalten. Standardziel ist GoLand 2026.2.2.1.

```sh
task          # Aufgaben anzeigen
task build    # Plugin-ZIP bauen
task test     # Tests ausführen
task verify   # Plugin-Konfiguration, Struktur und Kompatibilität prüfen
task check    # Tests, Build und alle Prüfungen
task run      # GoLand mit Plugin in einer Sandbox starten
task clean    # Build-Ergebnisse entfernen
```

Zusätzliche Gradle-Argumente nach `--` übergeben, beispielsweise für eine lokale IDE:

```sh
task check -- -PplatformLocalPath="/path/to/GoLand.app"
```

Der echte LSP-Test läuft bei installiertem Tilt. Diagnosen hängen vom Tilt-Server ab;
nicht jeder Syntaxfehler wird erkannt.

## Lizenz

[MIT](LICENSE). Die mitgelieferte Grammatik hat eigene
[Lizenzhinweise](textmate/README.md).
