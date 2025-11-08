package vlcj.llg_mc.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Utility class for PDF processing operations.
 */
public class PdfUtils {

	/**
	 * Converts a PDF file to a list of image files.
	 * 
	 * @param pdfPath   Path to the PDF file
	 * @param outputDir Directory to save the output images
	 * @param dpi       DPI for the output images (higher = better quality but
	 *                  larger files)
	 * @return List of paths to the generated image files
	 * @throws IOException If there's an error reading the PDF or writing the images
	 */
	public static List<String> convertPdfToImages(String pdfPath, String outputDir, int dpi) throws IOException {
		return convertPdfToImages(pdfPath, outputDir, dpi, null);
	}

	public static class PageData {
		public final String imagePath;
		public final String textContent;
		public final int pageNumber;

		public PageData(String imagePath, String textContent, int pageNumber) {
			this.imagePath = imagePath;
			this.textContent = textContent != null ? textContent : "";
			this.pageNumber = pageNumber;
		}
	}

	public static List<String> convertPdfToImages(String pdfPath, String outputDir, int dpi,
			PdfConversionProgressListener listener) throws IOException {
		System.out.println("PdfUtils.convertPdfToImages - Starting conversion of: " + pdfPath);
		List<String> imagePaths = new ArrayList<>();
		File pdfFile = new File(pdfPath);
		String baseName = pdfFile.getName().replace(".pdf", "");

		if (!pdfFile.exists()) {
			throw new IOException("PDF file not found: " + pdfPath);
		}
		System.out.println("PdfUtils - PDF file exists, size: " + pdfFile.length() + " bytes");

		// Ensure output directory exists
		File outputDirFile = new File(outputDir);
		if (!outputDirFile.exists()) {
			System.out.println("PdfUtils - Creating output directory: " + outputDir);
			boolean dirsCreated = outputDirFile.mkdirs();
			if (!dirsCreated) {
				System.err.println("PdfUtils - Failed to create output directory: " + outputDir);
			}
		}

		long overallStart = System.currentTimeMillis();

		try (PDDocument document = Loader.loadPDF(pdfFile)) {
			int pageCount = document.getNumberOfPages();
			System.out.println("PdfUtils - PDF loaded successfully, pages: " + pageCount);

			PDFRenderer pdfRenderer = new PDFRenderer(document);
			System.out.println("PdfUtils - Starting to render pages...");

			ITesseract tesseract = initTesseract();
			Path outputDirPath = outputDirFile.toPath();

			if (listener != null) {
				listener.onStart(pageCount);
			}

			// First pass: Extract all text content
			List<String> pageTexts = new ArrayList<>(pageCount);
			for (int page = 0; page < pageCount; page++) {
				try {
					String text = extractPdfText(document, page);
					pageTexts.add(text != null ? text : "");
				} catch (Exception e) {
					System.err.println("Error extracting text from page " + (page + 1) + ": " + e.getMessage());
					pageTexts.add("");
				}
			}

			// Second pass: Render images and save with text data
			for (int page = 0; page < pageCount; ++page) {
				long startTime = System.currentTimeMillis();
				System.out.println("PdfUtils - Processing page " + (page + 1) + " of " + pageCount);

				// Render each page to an image
				BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
				System.out.println("PdfUtils - Page " + (page + 1) + " rendered in "
						+ (System.currentTimeMillis() - startTime) + "ms");

				// Create output file path
				String outputPath = outputDir + File.separator + baseName + "_" + (page + 1) + ".jpg";
				File outputFile = new File(outputPath);
				System.out.println("PdfUtils - Writing page " + (page + 1) + " to " + outputPath);

				// Write the image
				boolean writeSuccess = ImageIO.write(image, "jpg", outputFile);
				if (!writeSuccess) {
					System.err.println("PdfUtils - Failed to write image for page " + (page + 1));
				} else {
					System.out.println("PdfUtils - Successfully wrote image for page " + (page + 1) + ", size: "
							+ outputFile.length() + " bytes");

					// Save text data for this page (only once)
					try {
						String textData = pageTexts.get(page);
						if (textData != null && !textData.trim().isEmpty()) {
							// Write the text file with just the base name (without .jpg.txt)
							String textFilePath = outputDir + File.separator + baseName + "_" + (page + 1) + ".txt";
							Files.write(Paths.get(textFilePath), textData.getBytes(StandardCharsets.UTF_8));
							System.out.println("Saved text data for page " + (page + 1));
						}
					} catch (Exception e) {
						System.err.println("Error saving text data for page " + (page + 1) + ": " + e.getMessage());
					}
				}
				imagePaths.add(outputFile.getAbsolutePath());

				if (listener != null) {
					long elapsed = System.currentTimeMillis() - startTime;
					listener.onPageComplete(page + 1, elapsed);
				}
			}
			System.out.println("PdfUtils - Successfully converted all pages to images");

			if (listener != null) {
				listener.onCompleted(System.currentTimeMillis() - overallStart);
			}

			return imagePaths;
		}
	}

	public interface PdfConversionProgressListener {
		void onStart(int totalPages);

		void onPageComplete(int pageNumber, long elapsedMillis);

		void onCompleted(long totalMillis);
	}

	/**
	 * Cleans up temporary image files.
	 * 
	 * @param imagePaths List of image file paths to delete
	 */
	public static void cleanupTempImages(List<String> imagePaths) {
		if (imagePaths == null)
			return;

		for (String path : imagePaths) {
			try {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				Path textPath = getTextPathForImage(file.toPath());
				if (textPath != null) {
					Files.deleteIfExists(textPath);
				}
			} catch (Exception e) {
				// Log or handle the error if needed
				System.err.println("Error deleting temporary file: " + path);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generates HTML content for the BookReader to display the PDF pages.
	 * 
	 * @param pdfPath    Path to the original PDF file (for display name)
	 * @param imagePaths List of image paths to display in the BookReader
	 * @return HTML content as a string
	 */
	public static String generateBookReaderHtml(String pdfPath, List<String> imagePaths) {
		System.out.println("PdfUtils.generateBookReaderHtml - Generating HTML for " + pdfPath);
		System.out.println("PdfUtils - Number of images: " + imagePaths.size());

		StringBuilder html = new StringBuilder();
		List<String> pageTexts = loadPageTexts(imagePaths);

		// HTML header
		html.append("<!DOCTYPE html>");
		html.append("<html><head>");
		html.append("<meta charset='UTF-8'>");
		html.append("<title>" + escapeHtml(pdfPath) + "</title>");
		html.append("<style id='llg-cursor-styles'>");
		html.append("body, #BookReader, #BookReader * { cursor: text !important; }");
		html.append(".BRtoolbar { background-color: #2c3e50 !important; }");
		html.append(
				"#BookReader { position: relative; -webkit-user-select: text; -moz-user-select: text; -ms-user-select: text; user-select: text; }");
		html.append(".BRpage { position: relative; }");
		html.append(".BRpagecontainer { position: relative; cursor: text !important; }");
		html.append(".BRpageimage { position: relative; z-index: 1; pointer-events: none !important; }");
		html.append(".BRtextLayer { ");
		html.append("  position: absolute; ");
		html.append("  left: 0; ");
		html.append("  top: 0; ");
		html.append("  width: 100%; ");
		html.append("  height: 100%; ");
		html.append("  z-index: 2; ");
		html.append("  pointer-events: auto !important; ");
		html.append("  -webkit-user-select: text; ");
		html.append("  -moz-user-select: text; ");
		html.append("  -ms-user-select: text; ");
		html.append("  user-select: text; ");
		html.append("  overflow: visible; ");
		html.append("}");
		html.append(".BRtextLayer > div { ");
		html.append("  position: absolute; ");
		html.append("  white-space: pre; ");
		html.append("  color: transparent; ");
		html.append("  line-height: 1.0; ");
		html.append("  cursor: text; ");
		html.append("  -webkit-user-select: text; ");
		html.append("  -moz-user-select: text; ");
		html.append("  -ms-user-select: text; ");
		html.append("  user-select: text; ");
		html.append("  pointer-events: auto; ");
		html.append("}");
		html.append(".BRtextLayer > div::selection { background: rgba(0, 0, 255, 0.3); }");
		html.append("</style>");

		// JavaScript to handle text selection and cursor
		StringBuilder js = new StringBuilder();
		js.append("<script>\n");
		js.append("// Store the page texts in a global variable\n");
		js.append("var pageTexts = [");
		for (int i = 0; i < pageTexts.size(); i++) {
			if (i > 0)
				js.append(",");
			js.append("\"").append(escapeJs(pageTexts.get(i))).append("\"");
		}
		js.append("];\n\n");

		html.append("      textDiv.style.overflow = 'visible';");
		html.append("      textDiv.style.whiteSpace = 'pre';");
		html.append("      textDiv.style.color = 'transparent';");
		html.append("      textDiv.style.pointerEvents = 'auto';");
		html.append("      textDiv.style.userSelect = 'text';");
		html.append("      textDiv.style.webkitUserSelect = 'text';");
		html.append("      textLayer.appendChild(textDiv);");
		html.append("      console.log('Added text to page', index);");
		html.append("    }");
		html.append("  }");
		html.append("  ");
		html.append("  // Process all existing pages");
		html.append("  document.querySelectorAll('.BRpagecontainer').forEach((container, i) => {");
		html.append("    const index = parseInt(container.getAttribute('data-index') || i, 10);");
		html.append("    processPage(container, index);");
		html.append("  });");
		html.append("  ");
		html.append("  // Set up mutation observer for dynamically added pages");
		html.append("  const observer = new MutationObserver(mutations => {");
		html.append("    mutations.forEach(mutation => {");
		html.append("      mutation.addedNodes.forEach(node => {");
		html.append("        if (node.matches && node.matches('.BRpagecontainer')) {");
		html.append("          const index = parseInt(node.getAttribute('data-index') || '0', 10);");
		html.append("          processPage(node, index);");
		html.append("        }");
		html.append("      });");
		html.append("    });");
		html.append("  });");
		html.append("  observer.observe(document.body, { childList: true, subtree: true });");
		html.append("  ");
		html.append("  // Also process pages when BookReader reports they're ready");
		html.append("  if (window.BookReader) {");
		html.append("    const events = ['postInit', 'pageChanged', 'resize', 'zoomIn', 'zoomOut'];");
		html.append("    events.forEach(event => {");
		html.append("      window.BR[event] = new Proxy(window.BR[event] || function(){}, {");
		html.append("        apply: (target, thisArg, args) => {");
		html.append("          const result = target.apply(thisArg, args);");
		html.append("          setTimeout(() => {");
		html.append("            document.querySelectorAll('.BRpagecontainer').forEach((container, i) => {");
		html.append("              const index = parseInt(container.getAttribute('data-index') || i, 10);");
		html.append("              processPage(container, index);");
		html.append("            });");
		html.append("          });");
		html.append("          return result;");
		html.append("        }");
		html.append("      });");
		html.append("    });");
		html.append("  }");
		html.append("  ");
		html.append("  // Clean up observer when component unmounts");
		html.append("  return () => observer.disconnect();");
		html.append("              processPage(container, index);");
		html.append("            });");
		html.append("          }, 100);");
		html.append("          return result;");
		html.append("        }");
		html.append("      });");
		html.append("    });");
		html.append("  }");
		html.append("  ");
		html.append("  // Re-run periodically to catch any missed pages");
		html.append("  setInterval(() => {");
		html.append("    document.querySelectorAll('.BRpagecontainer').forEach((container, i) => {");
		// Build the JavaScript function
		js.append("function setupTextSelection() {\n");
		js.append("  console.log('Setting up text selection with', pageTexts.length, 'pages of text');\n");
		js.append("  let processedPages = new Set();\n");
		js.append("  function processPage(container, index) {\n");
		js.append("    if (processedPages.has(index)) return;\n");
		js.append("    processedPages.add(index);\n");
		js.append("    console.log('Processing page', index, container);\n");
		js.append("    // Find the page image to get dimensions\n");
		js.append("    const pageImage = container.querySelector('img');\n");
		js.append("    if (!pageImage) {\n");
		js.append("      console.log('No image found in container, retrying...');\n");
		js.append("      setTimeout(() => processPage(container, index), 100);\n");
		js.append("      return;\n");
		js.append("    }\n\n");
		js.append("    // Find or create text layer\n");
		js.append("    let textLayer = container.querySelector('.BRtextLayer');\n");
		js.append("    if (!textLayer) {\n");
		js.append("      textLayer = document.createElement('div');\n");
		js.append("      textLayer.className = 'BRtextLayer';\n");
		js.append(
				"      textLayer.style.cssText = 'position:absolute; left:0; top:0; width:100%; height:100%; z-index:2147483647; pointer-events:auto; transform-origin: 0 0;';\n");
		js.append("      container.style.position = 'relative';\n");
		js.append("      container.prepend(textLayer);\n");
		js.append("    }\n\n");
		js.append("    // Make sure the container has proper positioning\n");
		js.append("    container.style.position = 'relative';\n");
		js.append("    container.style.overflow = 'visible';\n\n");
		js.append("    // Clear existing content\n");
		js.append("    textLayer.innerHTML = '';\n\n");
		js.append("    // Add text content if available\n");
		js.append("    if (pageTexts && pageTexts[index]) {\n");
		js.append("      const textDiv = document.createElement('div');\n");
		js.append("      textDiv.textContent = pageTexts[index];\n");
		js.append(
				"      textDiv.style.cssText = 'position:absolute; left:0; top:0; width:100%; height:100%; overflow:visible; white-space:pre; color:transparent; pointer-events:auto; user-select:text; -webkit-user-select:text; line-height:1.2; font-size:1em; transform-origin: 0 0;';\n");
		js.append("      textLayer.appendChild(textDiv);\n");
		js.append("      console.log('Added text to page', index, 'with', pageTexts[index].length, 'chars');\n");
		js.append("    }\n\n");
		js.append("    // Ensure text layer stays on top and is properly positioned\n");
		js.append("    textLayer.style.zIndex = '2147483647';\n");
		js.append("    textLayer.style.pointerEvents = 'auto';\n");
		js.append("    textLayer.style.userSelect = 'text';\n");
		js.append("    textLayer.style.webkitUserSelect = 'text';\n");
		js.append("    // Prevent double-click from propagating to parent elements\n");
		js.append("    textLayer.addEventListener('dblclick', function(e) {\n");
		js.append("      e.stopPropagation();\n");
		js.append("      e.preventDefault();\n");
		js.append("      // Allow text selection to proceed normally\n");
		js.append("      const selection = window.getSelection();\n");
		js.append("      const range = document.caretRangeFromPoint(e.clientX, e.clientY);\n");
		js.append("      if (range) {\n");
		js.append("        selection.removeAllRanges();\n");
		js.append("        selection.addRange(range);\n");
		js.append("        // Select the word at the cursor position\n");
		js.append("        selection.modify('move', 'backward', 'word');\n");
		js.append("        selection.modify('extend', 'forward', 'word');\n");
		js.append("      }\n");
		js.append("      return false;\n");
		js.append("    }, true);\n");
		js.append("  }\n\n");
		js.append("  // Override BookReader's double-click handler to prevent page turning when selecting text\n");
		js.append("  if (window.BookReader) {\n");
		js.append("    const originalHandleDoubleClick = window.BookReader.prototype.handleDoubleClick;\n");
		js.append("    window.BookReader.prototype.handleDoubleClick = function(e) {\n");
		js.append("      // Only allow page turning if the click wasn't on selectable text\n");
		js.append("      const textLayers = document.querySelectorAll('.BRtextLayer');\n");
		js.append("      const clickedOnText = Array.from(textLayers).some(layer => \n");
		js.append("        layer === e.target || layer.contains(e.target)\n");
		js.append("      );\n");
		js.append("      if (clickedOnText) {\n");
		js.append("        e.stopPropagation();\n");
		js.append("        return false;\n");
		js.append("      }\n");
		js.append("      return originalHandleDoubleClick.apply(this, arguments);\n");
		js.append("    };\n");
		js.append("  }\n\n");
		js.append("  // Process all existing pages after a short delay to ensure DOM is ready\n");
		js.append("  function processAllPages() {\n");
		js.append("    const containers = document.querySelectorAll('.BRpagecontainer');\n");
		js.append("    console.log('Found', containers.length, 'page containers');\n");
		js.append("    containers.forEach((container, i) => {\n");
		js.append("      const index = parseInt(container.getAttribute('data-index') || i, 10);\n");
		js.append("      // Use setTimeout to prevent UI blocking\n");
		js.append("      setTimeout(() => processPage(container, index), 0);\n");
		js.append("    });\n");
		js.append("  }\n\n");
		js.append("  // Initial processing\n");
		js.append("  processAllPages();\n");
		js.append("  // Also process after a delay to catch any late-loaded content\n");
		js.append("  setTimeout(processAllPages, 500);\n");
		js.append("  setTimeout(processAllPages, 2000);\n\n");
		js.append("  // Set up mutation observer for dynamically added pages\n");
		js.append("  const observer = new MutationObserver(mutations => {\n");
		js.append("    mutations.forEach(mutation => {\n");
		js.append("      mutation.addedNodes.forEach(node => {\n");
		js.append("        if (node.matches && node.matches('.BRpagecontainer')) {\n");
		js.append("          const index = parseInt(node.getAttribute('data-index') || '0', 10);\n");
		js.append("          processPage(node, index);\n");
		js.append("        }\n");
		js.append("      });\n");
		js.append("    });\n");
		js.append("  });\n\n");
		js.append("  observer.observe(document.body, { childList: true, subtree: true });\n\n");
		js.append("  // Also process pages when BookReader reports they're ready\n");
		js.append("  if (window.BookReader) {\n");
		js.append("    const events = ['postInit', 'pageChanged', 'resize', 'zoomIn', 'zoomOut'];\n");
		js.append("    events.forEach(event => {\n");
		js.append("      window.BR[event] = new Proxy(window.BR[event] || function(){}, {\n");
		js.append("        apply: (target, thisArg, args) => {\n");
		js.append("          const result = target.apply(thisArg, args);\n");
		js.append("          setTimeout(() => {\n");
		js.append("            document.querySelectorAll('.BRpagecontainer').forEach((container, i) => {\n");
		js.append("              const index = parseInt(container.getAttribute('data-index') || i, 10);\n");
		js.append("              processPage(container, index);\n");
		js.append("            });\n");
		js.append("          }, 100);\n");
		js.append("          return result;\n");
		js.append("        }\n");
		js.append("      });\n");
		js.append("    });\n");
		js.append("  }\n\n");
		js.append("  // Re-run periodically to catch any missed pages\n");
		js.append("  setInterval(() => {\n");
		js.append("    document.querySelectorAll('.BRpagecontainer').forEach((container, i) => {\n");
		js.append("      const index = parseInt(container.getAttribute('data-index') || i, 10);\n");
		js.append("      if (!processedPages.has(index)) {\n");
		js.append("        processPage(container, index);\n");
		js.append("      }\n");
		js.append("    });\n");
		js.append("  }, 1000);\n");
		js.append("}\n\n");

		// Add CSS styles
		js.append("const style = document.createElement('style');\n");
		js.append("style.textContent = `\n");
		js.append("  #BookReader {\n");
		js.append("    cursor: text !important;\n");
		js.append("    -webkit-touch-callout: text !important;\n");
		js.append("    -webkit-user-select: text !important;\n");
		js.append("    -khtml-user-select: text !important;\n");
		js.append("    -moz-user-select: text !important;\n");
		js.append("    -ms-user-select: text !important;\n");
		js.append("    user-select: text !important;\n");
		js.append("  }\n\n");
		js.append("  .BRpage {\n");
		js.append("    position: relative;\n");
		js.append("    -webkit-user-select: text !important;\n");
		js.append("    user-select: text !important;\n");
		js.append("  }\n\n");
		js.append("  .BRpagecontainer {\n");
		js.append("    cursor: text !important;\n");
		js.append("    -webkit-user-select: text !important;\n");
		js.append("    user-select: text !important;\n");
		js.append("  }\n\n");
		js.append("  .BRpagecontainer * {\n");
		js.append("    cursor: text !important;\n");
		js.append("    -webkit-user-select: text !important;\n");
		js.append("    user-select: text !important;\n");
		js.append("  }\n\n");
		js.append("  .BRtextLayer {\n");
		js.append("    position: absolute;\n");
		js.append("    left: 0;\n");
		js.append("    top: 0;\n");
		js.append("    right: 0;\n");
		js.append("    bottom: 0;\n");
		js.append("    overflow: hidden;\n");
		js.append("    line-height: 1.0;\n");
		js.append("    pointer-events: auto !important;\n");
		js.append("    opacity: 1 !important;\n");
		js.append("    z-index: 2;\n");
		js.append("  }\n\n");
		js.append("  .BRtextLayer > div {\n");
		js.append("    position: absolute;\n");
		js.append("    white-space: pre;\n");
		js.append("    color: transparent;\n");
		js.append("    line-height: 1.0;\n");
		js.append("    cursor: text;\n");
		js.append("    -webkit-user-select: text !important;\n");
		js.append("    user-select: text !important;\n");
		js.append("    pointer-events: auto !important;\n");
		js.append("  }\n\n");
		js.append("  .BRtextLayer > div::selection {\n");
		js.append("    background: rgba(0, 0, 255, 0.3);\n");
		js.append("  }\n\n");
		js.append("  .BRpageimage {\n");
		js.append("    pointer-events: none !important;\n");
		js.append("    user-select: none !important;\n");
		js.append("  }\n");
		js.append("`;\n");
		js.append("document.head.appendChild(style);\n\n");

		// Initialize the text selection
		js.append("// Initialize text selection after BookReader is ready\n");
		js.append("if (window.BookReader) {\n");
		js.append("  const oldInit = window.BookReader.prototype.init;\n");
		js.append("  window.BookReader.prototype.init = function() {\n");
		js.append("    const result = oldInit.apply(this, arguments);\n");
		js.append("    setTimeout(setupTextSelection, 100);\n");
		js.append("    return result;\n");
		js.append("  };\n");
		js.append("} else {\n");
		js.append("  // If BookReader isn't loaded yet, wait for it\n");
		js.append("  document.addEventListener('BookReader:ready', setupTextSelection);\n");
		js.append("  // Also try to set up after a delay in case the event was missed\n");
		js.append("  setTimeout(setupTextSelection, 2000);\n");
		js.append("}\n");

		// Close the script tag
		js.append("</script>\n");

		// Add the JavaScript to the HTML
		html.append(js.toString());
		html.append("    document.body.appendChild(toggleButton);");
		html.append("  }");
		html.append("  const applyState = () => {");
		html.append(
				"    toggleButton.textContent = selectionEnabled ? 'Disable Text Selection' : 'Enable Text Selection';");
		html.append("    toggleButton.classList.toggle('llg-active', selectionEnabled);");
		html.append("    function ensureTextLayer() {");
		html.append("      const containers = document.querySelectorAll('.BRpagecontainer');");
		html.append("      console.log('Found', containers.length, 'page containers');");
		html.append("      containers.forEach((container, index) => {");
		html.append("        let textLayer = container.querySelector('.BRtextLayer');");
		html.append("        // Create text layer if it doesn't exist");
		html.append("        if (!textLayer) {");
		html.append("          textLayer = document.createElement('div');");
		html.append("          textLayer.className = 'BRtextLayer';");
		html.append("          textLayer.style.position = 'absolute';");
		html.append("          textLayer.style.left = '0';");
		html.append("          textLayer.style.top = '0';");
		html.append("          textLayer.style.width = '100%';");
		html.append("          textLayer.style.height = '100%';");
		html.append("          textLayer.style.zIndex = '10';");
		html.append("          textLayer.style.pointerEvents = 'auto';");
		html.append("          // Insert the text layer as the first child of the container");
		html.append("          container.insertBefore(textLayer, container.firstChild);");
		html.append("          console.log('Created text layer for page', index);");
		html.append("        }");
		html.append("        // Only update if we have text and the text layer is empty");
		html.append("        if (pageTexts && pageTexts[index] && !textLayer.hasChildNodes()) {");
		html.append("          console.log('Adding text to page', index);");
		html.append("          // Clear any existing content");
		html.append("          textLayer.innerHTML = '';");
		html.append("          // Create a container for the text");
		html.append("          const textDiv = document.createElement('div');");
		html.append("          textDiv.textContent = pageTexts[index];");
		html.append("          textDiv.style.position = 'absolute';");
		html.append("          textDiv.style.left = '0';");
		html.append("          textDiv.style.top = '0';");
		html.append("          textDiv.style.width = '100%';");
		html.append("          textDiv.style.height = '100%';");
		html.append("          textDiv.style.overflow = 'visible';");
		html.append("          textDiv.style.whiteSpace = 'pre';");
		html.append("          textDiv.style.color = 'transparent';");
		html.append("          textDiv.style.pointerEvents = 'auto';");
		html.append("          textDiv.style.userSelect = 'text';");
		html.append("          textDiv.style.webkitUserSelect = 'text';");
		html.append("          textLayer.appendChild(textDiv);");
		html.append("          // Ensure the container has proper positioning");
		html.append("          container.style.position = 'relative';");
		html.append("          // Position the text to match the image");
		html.append("          const img = container.querySelector('img');");
		html.append("          if (img) {");
		html.append("            textLayer.style.width = img.offsetWidth + 'px';");
		html.append("            textLayer.style.height = img.offsetHeight + 'px';");
		html.append("            console.log('Set text layer size to', img.offsetWidth, 'x', img.offsetHeight);");
		html.append("          }");
		html.append("        }");
		html.append("      });");
		html.append("      // Re-run after a short delay to catch any dynamically loaded pages");
		html.append("      setTimeout(ensureTextLayer, 1000);");
		html.append("    };");
		html.append("    ensureTextLayer();");
		html.append("    // Re-run after a short delay to catch any dynamically loaded pages");
		html.append("    setTimeout(ensureTextLayer, 1000);");
		html.append("  };");
		html.append("  const syncOverlays = () => {");
		html.append("    document.querySelectorAll('.BRpagecontainer').forEach(container => {");
		html.append("      const indexAttr = container.getAttribute('data-index');");
		html.append("      const index = indexAttr ? Number(indexAttr) : NaN;");
		html.append("      if (Number.isInteger(index) && texts[index]) {");
		html.append("        ensureOverlay(container, texts[index]);");
		html.append("      } else {");
		html.append("        ensureOverlay(container, '');");
		html.append("      }");
		html.append("    });");
		html.append("    applyState();");
		html.append("  };");
		html.append(
				"  const events = [BookReader.eventNames.PostInit, BookReader.eventNames.pageChanged, BookReader.eventNames.resize, BookReader.eventNames.zoomIn, BookReader.eventNames.zoomOut];");
		html.append("  events.forEach(eventName => br.bind(eventName, syncOverlays));");
		html.append("  syncOverlays();");
		html.append("};");
		html.append("    });\n");
		html.append("    applyState();\n");
		html.append("  };\n");
		html.append(
				"  const events = [BookReader.eventNames.PostInit, BookReader.eventNames.pageChanged, BookReader.eventNames.resize, BookReader.eventNames.zoomIn, BookReader.eventNames.zoomOut];\n");
		html.append("  events.forEach(eventName => br.bind(eventName, syncOverlays));\n");
		html.append("  syncOverlays();\n");
		html.append("};\n");
		html.append("function initBookReader() {");
		html.append("  try {");
		html.append("    if (typeof BookReader === 'undefined') {");
		html.append("      console.error('BookReader is not defined');");
		html.append("      return false;");
		html.append("    }");
		html.append("  ");
		html.append("  var options = {");

		// Add image data
		html.append("    data: [");

		for (int i = 0; i < imagePaths.size(); i++) {
			if (i > 0) {
				html.append(",");
			}
			// Get the file and convert to URI with proper encoding
			File imgFile = new File(imagePaths.get(i));
			String fileUrl = imgFile.toURI().toASCIIString();
			// Ensure proper URL encoding
			fileUrl = fileUrl.replace(" ", "%20").replace("'", "%27").replace("(", "%28").replace(")", "%29");

			System.out.println("PdfUtils - Adding image " + (i + 1) + ": " + fileUrl);

			// Each page is in its own array (for potential multi-page spreads)
			html.append("[");
			html.append("{");
			html.append("width: 1200, ");
			html.append("height: 1600, ");
			html.append("uri: '" + fileUrl + "'");
			html.append("}");
			html.append("]");
		}

		// Get thumbnail URL (first page or empty string if no pages)
		String thumbnailUrl = "";
		if (!imagePaths.isEmpty()) {
			File thumbFile = new File(imagePaths.get(0));
			thumbnailUrl = thumbFile.toURI().toASCIIString().replace(" ", "%20").replace("'", "%27").replace("(", "%28")
					.replace(")", "%29");
			System.out.println("PdfUtils - Thumbnail URL: " + thumbnailUrl);
		}

		html.append("],");
		html.append("    bookTitle: '" + escapeJs(new File(pdfPath).getName()) + "',");
		html.append("    thumbnail: '" + thumbnailUrl + "',");
		html.append("    defaults: '2up',");
		html.append("    enableSearch: false,");
		html.append("    enableTtsPlugin: true,");
		html.append("    showToolbar: true,");
		html.append("    showLogo: false,");
		html.append("    showShare: false,");
		html.append("    showFullscreen: true,");
		html.append("    showDownload: false,");
		html.append(
				"    showLink: false,\n    showToolbarNextPrevious: true,\n    textSelector: '.BRtextLayer > div',");
		html.append("    showAutoPlay: false,");
		html.append("    showPdf: false,");
		html.append("    showFontControls: false,");
		html.append("    showScreenshot: false,");
		html.append("    enablePageNumbers: true,");
		html.append("    pageProgression: 'ltr',");
		html.append("    ui: 'full',");
		html.append("    el: '#BookReader',");
		html.append("    imagesBaseURL: ''");
		html.append("  };");

		html.append("    }");
		html.append("  } catch (e) {");
		html.append("    console.error('Error in initBookReader:', e);");
		html.append("    var errorDiv = document.createElement('div');");
		html.append("    errorDiv.style.padding = '20px';");
		html.append("    errorDiv.style.color = 'white';");
		html.append("    errorDiv.style.fontFamily = 'Arial, sans-serif';");
		html.append("    errorDiv.style.backgroundColor = '#e74c3c';");
		html.append("    errorDiv.style.borderRadius = '5px';");
		html.append("    errorDiv.style.margin = '20px';");
		html.append("    errorDiv.innerHTML = '<h2>Error initializing BookReader</h2>' +");
		html.append("      '<p>' + (e && e.message ? e.message : String(e)) + '</p>' +");
		html.append("      '<p><small>Check browser console for details.</small></p>';");
		html.append("    document.getElementById(\"BookReader\").appendChild(errorDiv);");
		html.append("    return false;");
		html.append("  }");
		html.append("}");

		// Initialize when all scripts are loaded
		html.append("if (window.jQuery) {");
		html.append("  $(document).ready(function() {");
		html.append("    console.log('jQuery is loaded, initializing BookReader...');");
		html.append("    if (!initBookReader()) {");
		html.append("      console.log('Retrying BookReader initialization...');");
		html.append("      setTimeout(initBookReader, 1000);");
		html.append("    }");
		html.append("  });");
		html.append("} else {");
		html.append("  console.error('jQuery not loaded');");
		html.append("  document.addEventListener('DOMContentLoaded', function() {");
		html.append("    console.log('DOM loaded, trying to initialize BookReader...');");
		html.append("    if (!initBookReader()) {");
		html.append("      console.log('Retrying BookReader initialization...');");
		html.append("      setTimeout(initBookReader, 1000);");
		html.append("    }");
		html.append("  });");
		html.append("}");

		html.append("</script>");
		html.append("</body></html>");

		return html.toString();
	}

	private static final String PAGE_TEXT_SUFFIX = ".txt";

	public static String generateBookReaderManifest(String pdfPath, List<String> imagePaths, String folderName) {
		StringBuilder manifest = new StringBuilder();
		String relativeBase = "../books/" + encodePathComponent(folderName) + "/";

		manifest.append('{');
		manifest.append("\"bookTitle\":\"");
		manifest.append(escapeJs(new File(pdfPath).getName()));
		manifest.append("\",");
		manifest.append("\"thumbnail\":");
		if (!imagePaths.isEmpty()) {
			String firstName = new File(imagePaths.get(0)).getName();
			manifest.append("\"");
			manifest.append(relativeBase);
			manifest.append(encodePathComponent(firstName));
			manifest.append("\"");
		} else {
			manifest.append("\"\"");
		}
		manifest.append(",\"data\":[");

		for (int i = 0; i < imagePaths.size(); i++) {
			if (i > 0) {
				manifest.append(',');
			}
			String fileName = new File(imagePaths.get(i)).getName();
			manifest.append("[{\"width\":1200,\"height\":1600,\"uri\":\"");
			manifest.append(relativeBase);
			manifest.append(encodePathComponent(fileName));
			manifest.append("\"}]");
		}

		manifest.append("]}");
		return manifest.toString();
	}

	private static ITesseract initTesseract() {
		try {
			Tesseract tesseract = new Tesseract();
			String tessDataPath = Optional.ofNullable(System.getenv("TESSDATA_PREFIX"))
					.map(path -> path.endsWith(File.separator) ? path : path + File.separator).orElse(null);
			if (tessDataPath != null && new File(tessDataPath).exists()) {
				tesseract.setDatapath(tessDataPath);
			}
			tesseract.setLanguage("eng");
			tesseract.setVariable("user_defined_dpi", "300");
			return tesseract;
		} catch (Exception e) {
			System.err.println("PdfUtils - Failed to initialize Tesseract: " + e.getMessage());
			return null;
		}
	}

	private static String extractPageText(PDDocument document, int pageIndex, BufferedImage image,
			ITesseract tesseract) {
		String text = extractPdfText(document, pageIndex);
		if (text != null && !text.isBlank()) {
			return text;
		}
		if (tesseract == null) {
			return "";
		}
		try {
			String ocrText = tesseract.doOCR(image);
			if (ocrText != null && !ocrText.isBlank()) {
				return ocrText;
			}
		} catch (TesseractException e) {
			System.err.println("PdfUtils - OCR failed for page " + (pageIndex + 1) + ": " + e.getMessage());
		}
		return "";
	}

	private static String extractPdfText(PDDocument document, int pageIndex) {
		try {
			PDFTextStripper textStripper = new PDFTextStripper();
			textStripper.setSortByPosition(true);
			int pageNumber = pageIndex + 1;
			textStripper.setStartPage(pageNumber);
			textStripper.setEndPage(pageNumber);
			String text = textStripper.getText(document);
			return text != null ? text.trim() : "";
		} catch (IOException e) {
			System.err
					.println("PdfUtils - Failed to extract text from page " + (pageIndex + 1) + ": " + e.getMessage());
			return "";
		}
	}

	private static void writePageText(Path outputDir, String baseName, int pageNumber, String text) {
		if (text == null) {
			text = "";
		}
		try {
			Path textPath = getPageTextPath(outputDir, baseName, pageNumber);
			Files.createDirectories(textPath.getParent());
			Files.writeString(textPath, text, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("PdfUtils - Failed to write text for page " + pageNumber + ": " + e.getMessage());
		}
	}

	private static Path getPageTextPath(Path outputDir, String baseName, int pageNumber) {
		String fileName = baseName + "_" + pageNumber + PAGE_TEXT_SUFFIX;
		return outputDir.resolve(fileName);
	}

	private static Path getTextPathForImage(Path imagePath) {
		if (imagePath == null) {
			return null;
		}
		String fileName = imagePath.getFileName().toString();
		int dot = fileName.lastIndexOf('.');
		if (dot <= 0) {
			return imagePath.resolveSibling(fileName + PAGE_TEXT_SUFFIX);
		}
		String base = fileName.substring(0, dot);
		return imagePath.resolveSibling(base + PAGE_TEXT_SUFFIX);
	}

	private static List<String> loadPageTexts(List<String> imagePaths) {
		if (imagePaths == null || imagePaths.isEmpty()) {
			return List.of();
		}
		List<String> texts = new ArrayList<>(imagePaths.size());
		for (String imagePath : imagePaths) {
			Path textPath = getTextPathForImage(Paths.get(imagePath));
			String value = "";
			if (textPath != null && Files.exists(textPath)) {
				try {
					value = Files.readString(textPath, StandardCharsets.UTF_8);
				} catch (IOException e) {
					System.err.println("PdfUtils - Failed to read text layer from " + textPath + ": " + e.getMessage());
				}
			}
			texts.add(Optional.ofNullable(value).orElse(""));
		}
		return texts;
	}

	// Helper method to escape HTML special characters
	private static String escapeHtml(String input) {
		if (input == null)
			return "";
		return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	// Helper method to escape JavaScript string literals
	private static String escapeJs(String input) {
		if (input == null)
			return "";
		return input.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n")
				.replace("\r", "\\r").replace("\t", "\\t");
	}

	private static String encodePathComponent(String input) {
		if (input == null)
			return "";
		return input.replace(" ", "%20").replace("'", "%27").replace("(", "%28").replace(")", "%29");
	}
}
