# Zita

## Getting Started

To quickly set up and run Zita:

1. **Download the latest jar:**
   ```sh
   curl -L -o Zita.jar https://github.com/Addzyyy/Zita/releases/latest/download/Zita.jar
   ```

2. **Download the rules file:**
   ```sh
   curl -L -o rules.xml https://raw.githubusercontent.com/Addzyyy/Zita/main/src/main/resources/rulesets/rules.xml
   ```

3. **Run the tool:**
   ```sh
   java -jar Zita.jar --project /path/to/project --rules rules.xml --renderer
   ```

Replace `/path/to/project` with the path to your Processing project folder containing `.pde` files.

## Renderer Option

The `--renderer` option controls the output format of Zita's results. You can specify one of the following types:

- `zita` (default): Outputs human-readable feedback in a style similar to Atelier comments.
- `html`: Outputs results in HTML format for viewing in a browser.
- `json`: Outputs results in JSON format for further processing or integration.

Example usage:
```sh
java -jar Zita.jar --project /path/to/project --rules rules.xml --renderer html
```
If you omit `--renderer`, Zita will use its default text output.