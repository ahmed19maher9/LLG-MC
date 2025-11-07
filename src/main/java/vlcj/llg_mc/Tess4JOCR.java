package vlcj.llg_mc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

/**
 * OCR utility class for extracting text from images in BookReader
 */
public class Tess4JOCR {
	private static final Logger LOGGER = Logger.getLogger(Tess4JOCR.class.getName());

	private static ITesseract tesseractInstance;
	private static String tessdataPath;

	static {
		initializeTesseract();
	}

	/**
	 * Initialize Tesseract with proper tessdata path
	 */
	private static void initializeTesseract() {
		try {
			tesseractInstance = new Tesseract();

			// Try multiple possible tessdata locations
			String[] possiblePaths = {
					// Check runtime classpath first (where tessdata gets copied during build)
					"tessdata",
					// Check in project resources
					"src/main/resources/tessdata",
					"resources/tessdata",
					// Check system installations
					"C:/Program Files/Tesseract-OCR/tessdata",
					"C:/Program Files (x86)/Tesseract-OCR/tessdata",
					"/usr/share/tesseract-ocr/5/tessdata",
					"/usr/share/tesseract-ocr/tessdata",
					// Check environment variable
					System.getenv("TESSDATA_PREFIX") != null ? System.getenv("TESSDATA_PREFIX") + "/tessdata" : null
			};

			for (String path : possiblePaths) {
				if (path != null && Files.exists(Paths.get(path))) {
					tessdataPath = path;
					tesseractInstance.setDatapath(tessdataPath);
					LOGGER.info("Found tessdata at: " + tessdataPath);
					break;
				}
			}

			if (tessdataPath == null) {
				LOGGER.warning("Tessdata directory not found. OCR functionality will be limited.");
				// Set a default path that might work
				tesseractInstance.setDatapath("tessdata");
			}

			// Set language to English by default
			tesseractInstance.setLanguage("eng");

		} catch (Exception e) {
			LOGGER.severe("Failed to initialize Tesseract: " + e.getMessage());
			tesseractInstance = null;
		}
	}

	/**
	 * Extract words with bounding boxes from an image file
	 * 
	 * @param imageFile The image file to process
	 * @return List of Word objects containing text and bounding box information
	 * @throws IOException        if the image file cannot be read
	 * @throws TesseractException if OCR processing fails
	 */
	public static List<Word> getWordsWithBoundingBoxes(File imageFile) throws IOException, TesseractException {
		if (tesseractInstance == null) {
			throw new TesseractException("Tesseract not initialized");
		}

		if (!imageFile.exists()) {
			throw new IOException("Image file does not exist: " + imageFile.getAbsolutePath());
		}

		LOGGER.info("Processing image for OCR: " + imageFile.getName());

		// Load the image file into a BufferedImage
		BufferedImage bufferedImage = ImageIO.read(imageFile);

		if (bufferedImage == null) {
			throw new IOException("Failed to read image file: " + imageFile.getAbsolutePath());
		}

		// Perform OCR and get words with bounding boxes
		List<Word> words = tesseractInstance.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD);

		LOGGER.info("OCR completed. Found " + words.size() + " words in " + imageFile.getName());

		return words;
	}

	/**
	 * Extract text from an image file (without bounding boxes)
	 * 
	 * @param imageFile The image file to process
	 * @return Extracted text as a string
	 * @throws IOException        if the image file cannot be read
	 * @throws TesseractException if OCR processing fails
	 */
	public static String getTextFromImage(File imageFile) throws IOException, TesseractException {
		if (tesseractInstance == null) {
			throw new TesseractException("Tesseract not initialized");
		}

		BufferedImage bufferedImage = ImageIO.read(imageFile);
		return tesseractInstance.doOCR(bufferedImage);
	}

	/**
	 * Check if Tesseract is properly initialized
	 * 
	 * @return true if Tesseract is ready for use
	 */
	public static boolean isInitialized() {
		return tesseractInstance != null;
	}

	/**
	 * Get the tessdata path being used
	 * 
	 * @return The tessdata directory path
	 */
	public static String getTessdataPath() {
		return tessdataPath;
	}
}
