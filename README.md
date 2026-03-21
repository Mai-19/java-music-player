# COMP-2800-Project
An all-java local music player!

## Installation

> ### Note
> No precompiled JARS are available yet.

### Prerequisites
- [OpenJDK 25](https://jdk.java.net/25/) <br> (recommended — other versions down to Java 8 may work but are untested)

### Steps
1. **Clone the repository**
```bash
git clone https://github.com/Mai-19/comp-2800-project.git
cd comp-2800-project
```
2. **Compile the source**
All dependencies are included in `lib/`. Run the following from the project root:
```bash
javac -cp "lib/*" -sourcepath src -d out src/app/App.java
```
3. **Package into JAR**
```bash
jar cfe MusicPlayer.jar app.App -C out . -C res .
```
4. **Run**
```bash
java -jar MusicPlayer.jar
```

## Usage

Add a directory in the settings and press refresh!

![Demo](.github/assets/demo.gif)

## License
[LGPL-3.0](./LICENSE)