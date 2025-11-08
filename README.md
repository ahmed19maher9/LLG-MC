# LLG Media Center

A comprehensive, multi-modal media center application built in Java,javascript and natural language programming (Models:SWE-1,grok) that combines video/audio playback, book reading, torrent streaming, and web integration (youtube/vimeo/gomovies) into a single, unified interface.

 [![GitHub version](https://img.shields.io/github/v/tag/ahmed19maher9/LLG-MC)](https://badge.fury.io/gh/ahmed19maher9%2LLG-MC) [![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0) [![Github All Releases](https://img.shields.io/github/downloads/ahmed19maher9/LLG-MC/total)]()  [![Gitter](https://img.shields.io/gitter/room/ahmed19maher9/LLG-MC)](https://gitter.im/LLG-MC/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![GitHub contributors](https://img.shields.io/github/contributors-anon/ahmed19maher9/LLG-MC)]() [![GitHub contributors](https://img.shields.io/github/issues-raw/ahmed19maher9/LLG-MC)]() [![GitHub contributors](https://img.shields.io/github/issues-closed-raw/ahmed19maher9/LLG-MC)]()
[![Donate](https://img.shields.io/badge/Donate-Donorbox-green)](https://4fund.com/tsu72s)

![LLG Media Center](https://i.ibb.co/Y4qCsgyh/llg-logo.png)

## Features

### 🎬 Media Player
- **VLC-powered playback** with full codec support
- **Drag-and-drop** file loading with automatic playlist creation
- **Subtitle support** (.srt, .ass, .ssa, .sub, .vtt) with sync controls
- **Translation features** for selected text with language detection
- **Saved selections** with timestamps for future reference and playback
- **Fullscreen mode** with custom controls
- **Playlist management** with repeat and shuffle options

### 📚 Book Reader
- **PDF to book conversion** with high-quality image rendering
- **OCR support** using Tesseract for text extraction and searchability
- **Interactive reading** with zoom, pan, and page navigation
- **Text selection** and copying capabilities
- **Bookshelf management** with automatic directory watching
- **Read-aloud functionality** using BookReader's TTS plugin

### 🌐 Web Content Integration
- **YouTube, Vimeo, and GoMovies** integration via JavaFX WebView
- **Automatic subtitle extraction** and display
- **Custom WebView logging** for debugging
- **Responsive design** with modern UI elements

### ⬇️ Torrent Streaming
- **Magnet link and .torrent file support**
- **Real-time streaming** while downloading
- **File prioritization** for sequential playback
- **Progress tracking** with detailed statistics
- **HTTP server integration** for streaming content
- **Peer discovery** with DHT, LSD, and UPnP support

## Requirements

### System Requirements
- **Java 21** or higher
- **VLC Media Player** (3.0 or higher) installed on the system
- **Windows 10/11** (primary platform, may work on others)
- **4GB RAM** minimum, 8GB recommended
- **2GB free disk space** for application and temporary files

### Dependencies
- **VLCJ** - Java bindings for VLC
- **JavaFX** - Modern UI framework
- **Apache PDFBox** - PDF processing
- **Tesseract OCR** - Optical character recognition
- **libtorrent** - Torrent functionality
- **OkHttp** - HTTP client for API calls

## Installation

### Prerequisites
1. Ensure **Java 21** is installed and `JAVA_HOME` is set
2. Install **Maven** for building the project

### Build Instructions

```bash
# Clone the repository
git clone <repository-url>
cd llg-mc

# Build with Maven
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="vlcj.llg_mc.App"

# Or build and run the JAR
mvn clean package
java -jar target/llg-mc-0.0.1-SNAPSHOT.jar
```

## Usage

### Starting the Application
Launch the application using the method above. The main interface provides a hub with cards for different media types.

### Media Player
1. **Open Media Player** from the main hub
2. **Drag and drop** video/audio files or use the playlist controls
3. **Load subtitles** by right-clicking in the playlist and selecting "Add Subtitle"
4. **Translate text** by selecting text and using the translation dialog
5. **Save selections** for future reference with timestamps and playbacks

### Book Reader
1. **Open Book Reader** from the main hub
2. **Drag and drop** PDF files or use "Open PDF" button
3. **Browse bookshelf** to access previously converted books
4. **Use OCR** for text extraction and searchability
5. **Enable read-aloud** for text-to-speech functionality

### Torrent Streaming
1. **Open Torrent** view from the main hub
2. **Paste magnet links** or browse for .torrent files
3. **Select files** to download and stream
4. **Monitor progress** with real-time statistics
5. **Stream immediately** or wait for complete download

### Web Content
1. **Select YouTube, Vimeo, or GoMovies** from the main hub
2. **Navigate to content** using the embedded browser
3. **Extract subtitles** automatically where available
4. **Use custom controls** for enhanced viewing experience

## Architecture

### Core Components

```
src/main/java/vlcj/llg_mc/
├── App.java                 # Main application class
├── FileDropTargetListener.java  # Drag-and-drop handling
├── LibtorrentStreamer.java  # Torrent functionality
├── SrtParser.java          # Subtitle parsing
├── SubtitleService.java     # Translation services
├── Tess4JOCR.java          # OCR processing
├── WebViewLogger.java      # Web content logging
└── util/
    └── PdfUtils.java       # PDF processing utilities
```

### Key Technologies

- **VLCJ**: Java bindings for VLC media player
- **JavaFX**: Web content and modern UI components
- **Swing**: Traditional desktop UI components
- **Apache PDFBox**: PDF manipulation and rendering
- **Tesseract**: OCR engine for text extraction
- **libtorrent**: BitTorrent protocol implementation
- **BookReader**: JavaScript library for e-book viewing

### Data Flow

1. **User Interaction** → Event handling in Swing/JavaFX
2. **Media Processing** → VLCJ for playback, PDFBox for documents
3. **Network Operations** → OkHttp for APIs, libtorrent for P2P
4. **File Operations** → Java NIO for local file handling
5. **UI Updates** → EDT for Swing, JavaFX Application Thread for WebView

## Configuration

### VLC Path Detection
The application automatically detects VLC libraries in standard project location:
- `\resources`


### OCR Configuration
- English language model included (`eng.traineddata`)
- Configurable DPI for PDF-to-image conversion (default: 150)
- Automatic fallback from PDF text extraction to OCR

### Torrent Settings
- Configurable download/upload limits
- Automatic peer discovery via DHT, LSD, and UPnP
- Sequential download mode for streaming optimization

## Troubleshooting

### Common Issues

#### VLC Not Found
```
Error: VLC media player was not found
```
**Solution**: make sure the required vlc binaries are present in the resources folder in the project's parent directory

#### JavaFX Runtime Issues
```
Error: JavaFX runtime components are missing
```
**Solution**: Use Java 21 with JavaFX modules or ensure JavaFX is properly configured

#### OCR Not Working
```
Warning: Tesseract OCR not available
```
**Solution**: Ensure Tesseract data files are in the correct location and TESSDATA_PREFIX is set

#### Torrent Connection Issues
```
Error: Failed to initialize torrent session
```
**Solution**: Check firewall settings and ensure ports 6881-6889 are available

### Performance Tips

- **Memory**: Allocate at least 4GB RAM for large PDF conversions
- **Disk Space**: Ensure sufficient space for temporary files during PDF processing
- **Network**: Stable internet connection required for torrent streaming and web content
- **GPU**: Hardware acceleration improves video playback performance

## Development

### Project Structure
```
llg-mc/
├── src/main/java/          # Java source files
├── src/main/resources/     # Application resources
│   ├── bookreader/        # BookReader library
│   ├── tessdata/          # OCR language data
│   └── icons/             # Application icons
├── src/test/java/          # Unit tests
├── target/                 # Build output
└── pom.xml                # Maven configuration
```

### Building from Source

```bash
# Full build with tests
mvn clean install

# Skip tests for faster builds
mvn clean compile -DskipTests

# Run tests only
mvn test

# Generate javadoc
mvn javadoc:javadoc
```

### IDE Setup

1. **Eclipse**: Import as Maven project
2. **IntelliJ IDEA**: Open as Maven project
3. **VS Code**: Install Java and Maven extensions

### Code Style

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Include comprehensive JavaDoc comments
- Handle exceptions appropriately
- Use logging for debugging information

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- **Code Quality**: Ensure all tests pass before submitting
- **Documentation**: Update README and JavaDoc for new features
- **Compatibility**: Test on multiple Java versions and VLC installations
- **Performance**: Profile and optimize resource-intensive operations
- **Security**: Validate all external inputs and network communications

## License

This project is licensed under the  AGPLv3 License - see the [LICENSE](https://www.gnu.org/licenses/agpl-3.0.en.html) file for details.

## Acknowledgments

- **VLCJ** project for Java VLC bindings
- **Internet Archive** for the BookReader library
- **libtorrent** project for BitTorrent functionality
- **Apache PDFBox** for PDF processing capabilities
- **Tesseract OCR** for optical character recognition

## Support

For support and questions:
- Create an issue on GitHub
- Check the troubleshooting section above
- Review the VLCJ documentation for media-related issues
- Consult the BookReader documentation for e-book functionality

---

**LLG Media Center** - Bringing together the best of media consumption in one comprehensive application.
