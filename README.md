# Huffman Coding

A JavaFX desktop application that compresses and decompresses files with Huffman coding.

## Features

- Select and compress any non-`.huf` file.
- Decompress `.huf` files created by the application.
- Display generated Huffman codes and byte frequencies.
- Display compression sizes, savings, and output paths.
- Preserve an existing original file by choosing a non-conflicting decompression filename.

## Technologies & Tools

- **Java 25** - application language and required JDK.
- **JavaFX 25** - desktop controls, windows, file choosers, and styling.
- **Maven** - dependency management, compilation, testing, and application launch.
- **JUnit 5** - codec round-trip and invalid-file tests.

## Data Structures

- **Frequency array (`int[256]`)** - counts every possible byte value.
- **Array-backed min-heap** - repeatedly selects the least-frequent Huffman nodes.
- **Binary Huffman tree** - represents byte codes as left/right bit paths.
- **Code table (`String[256]`)** - maps each byte value to its generated bit sequence.

## Prerequisites

- JDK 25 available through `JAVA_HOME` or the system `PATH`.
- Internet access on the first build so Maven Wrapper can download Maven and the declared dependencies.

No separate Maven or JavaFX SDK installation is required.

## Getting Started

```powershell
git clone <repository-url>
cd Huffman
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

On macOS or Linux, set `JAVA_HOME` and use `./mvnw` instead of `.\mvnw.cmd`.

Compressed files are written beside the selected input with `.huf` appended. Decompression restores the original filename when it is available; otherwise it uses a `-decompressed` suffix without overwriting the existing file.

## Project Structure

- `src/main/java/application` - the existing Java classes and `application` package.
- `src/main/resources/application` - the JavaFX stylesheet.
- `pom.xml` - JDK, JavaFX, test, and run configuration.
- `.mvn/wrapper`, `mvnw`, `mvnw.cmd` - portable Maven Wrapper files.
