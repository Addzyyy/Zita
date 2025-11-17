# Zita

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.70-blue.svg)](https://kotlinlang.org/)

A production-grade static code analysis tool for Processing projects, designed for automated assessment and feedback in computer science education.

## Overview

Zita analyzes Processing sketches by converting them to Java and applying custom PMD rules specific to Processing. Currently deployed in production at **Macquarie University** for COMP1000 (Introduction to Computer Programming), where it provides automated feedback to students, assists teaching staff with code assessment, and is used by honours students as part of their thesis research on automated code assessment and programming education.

### Key Features

- **Processing-to-Java Conversion**: Seamlessly converts Processing sketches (.pde files) to Java for analysis
- **Custom Rule Engine**: 50+ educational rules covering programming fundamentals, OOP concepts, and Processing-specific patterns
- **Multiple Output Formats**: Specialized renderers for different audiences (students, teaching assistants, examiners)
- **Educational Assessment**: Category-based rules aligned with learning outcomes and grading rubrics
- **Production Ready**: Has Processed over 5000+ student submissions at Macquarie University 

## Research Background

This project is based on research-validated approaches to automated code assessment for Processing:

- **Zita Framework**: Built upon Tim Blok's Zita project, which converts Processing code to Java for PMD analysis
  - 📄 [Blok, T. (2019). "Zita: A Tool for the Automated Assessment of Processing Programs"](https://purl.utwente.nl/essays/77948)
  - 💻 [Original Zita Repository](https://github.com/swordiemen/zita/)

- **PMD Rules for Processing**: Incorporates rules developed by Remco de Man for Processing-specific code analysis
  - 📄 [de Man, R. et al. (2018). "PMD For Processing: Automated Feedback For Processing"](https://www.scitepress.org/Link.aspx?doi=10.5220/0006701704420431)
  - 💻 [ProcessingPMD Repository](https://github.com/ZITA4PDE/ProcessingPMD)

- **Atelier Integration**: Derived from the Atelier-PMD system for integration with learning management systems
  - 💻 [Atelier-PMD Repository](https://github.com/creativeprogrammingatelier/atelier-PMD)

### This Implementation

This repository represents continued development and production deployment of the standalone Zita tool, including:
- Ongoing maintenance and bug fixes
- Development of additional custom rules for educational assessment
- New output renderers (student feedback, viva/oral exam handovers)
- Production deployment and operational management at Macquarie University

## Getting Started

### Quick Start

1. **Download the latest JAR:**
   ```sh
   curl -L -o Zita.jar https://github.com/Addzyyy/Zita/releases/latest/download/Zita.jar
   ```

2. **Download the rules configuration:**
   ```sh
   curl -L -o rules.xml https://raw.githubusercontent.com/Addzyyy/Zita/main/src/main/resources/rulesets/rules.xml
   ```

3. **Run analysis on a Processing project:**
   ```sh
   java -jar Zita.jar --project /path/to/processing/project --rules rules.xml --renderer student
   ```

### Usage

```
java -jar Zita.jar --project <project_path> --rules <rules_path> [--renderer <type>]
```

**Required Arguments:**
- `--project <path>`: Path to Processing project directory containing `.pde` files
- `--rules <path>`: Path to PMD rules XML configuration file

**Optional Arguments:**
- `--renderer <type>`: Output format (default: `zita`)

### Renderer Options

The `--renderer` option controls the output format of Zita's analysis results:

| Renderer | Description | Use Case |
|----------|-------------|----------|
| `zita` (default) | Human-readable text output styled like Atelier comments | Quick feedback review |
| `student` | Categorized feedback by difficulty level with educational context | Student-facing automated feedback |
| `handover` | Structured assessment data organized by rule categories | Viva/oral exam preparation for teaching staff |
| `html` | Web-viewable formatted output | Browser-based review and archiving |
| `json` | Structured JSON data | Integration with LMS or analytics tools |
| `csv` | Comma-separated values | Spreadsheet analysis and reporting |

**Examples:**
```sh
# Default text output
java -jar Zita.jar --project ./student_submission --rules rules.xml

# Student-facing feedback with categorized rules
java -jar Zita.jar --project ./student_submission --rules rules.xml --renderer student

# Generate viva handover document
java -jar Zita.jar --project ./student_submission --rules rules.xml --renderer handover

# Export to JSON for further processing
java -jar Zita.jar --project ./student_submission --rules rules.xml --renderer json
```

## Technical Architecture

### Technology Stack

- **Languages**: Java 11, Kotlin 1.3.70
- **Build Tool**: Maven 3
- **Static Analysis Engine**: PMD 6.35.0
- **Packaging**: Executable JAR via Maven Shade Plugin

### How It Works

1. **Processing Sketch Discovery**: Recursively finds all `.pde` files in the project directory
2. **Conversion**: Converts Processing sketches to Java compilation units
3. **PMD Analysis**: Runs custom PMD rules against the converted Java code
4. **Rendering**: Formats violations according to the selected renderer
5. **Output**: Produces feedback in the specified format

### Project Structure

```
src/main/
├── java/nl/utwente/
│   ├── Runner.java                          # CLI entry point
│   ├── processing/                          # Processing file models
│   │   ├── ProcessingProject.java
│   │   └── ProcessingFile.java
│   └── renderers/                           # Output formatters
│       ├── AtelierStyleTextRenderer.java    # Default text output
│       ├── StudentFeedbackRenderer.java     # Categorized feedback
│       └── VivaHandoverRenderer.java        # Assessment handover
└── kotlin/nl/utwente/processing/pmd/
    ├── rules/                               # Custom analysis rules (50+)
    ├── symbols/                             # Processing language symbols
    └── utils/                               # Helper utilities
```
## Building from Source

### Prerequisites

- Java JDK 11 or higher
- Maven 3.x

### Build Steps

```sh
# Clone the repository
git clone https://github.com/Addzyyy/Zita.git
cd Zita

# Compile and package
mvn clean package

# Run the built JAR
java -jar target/Zita.jar --project <path> --rules <rules_path>
```

## Production Deployment

Zita is actively used in production at Macquarie University for COMP1000, processing student Processing projects and generating automated feedback aligned with course learning outcomes.

### Production Features

- **Automated Build Verification**: Validates sketches compile before analysis
- **Category-based Assessment**: Rules mapped to grading rubrics
- **Multi-format Output**: Different renderers for students, TAs, and examiners
- **Educational Alignment**: Rules designed around specific learning milestones

## Use Cases

1. **Automated Feedback**: Integrate into assignment pipelines for immediate student feedback
2. **Self-Assessment**: Students run Zita locally before submission
3. **Teaching Assistant Support**: Streamline code review with automated initial assessment
4. **Viva Preparation**: Generate structured handover documents for oral examinations
5. **Learning Analytics**: Export data for analyzing common programming mistakes

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Adding Custom Rules

To add a new rule:
1. Create a Kotlin class extending `AbstractProcessingRule` in `src/main/kotlin/nl/utwente/processing/pmd/rules/`
2. Implement the PMD visitor pattern for AST analysis
3. Add the rule to `src/main/resources/rulesets/rules.xml` with appropriate category
4. Rebuild the project

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project builds upon the foundational research and development work of:
- **Tim Blok** - Original Zita framework for Processing-to-Java conversion
- **Remco de Man & Ansgar Fehnker** - PMD rules for Processing code analysis
- **Arthur Rump** - Main contributor to Atelier-PMD
- The **Creative Programming Atelier** team at University of Twente

Special thanks to **Ansgar Fehnker** for supervising the continued development and production deployment of this tool.

---

**Maintained by**: Adam Fulton
**Deployed at**: Macquarie University, COMP1000
