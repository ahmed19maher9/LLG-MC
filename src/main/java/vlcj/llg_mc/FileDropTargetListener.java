package vlcj.llg_mc;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaListPlayerComponent;

public class FileDropTargetListener implements DropTargetListener {
	protected static String filePath;
	private final EmbeddedMediaListPlayerComponent mediaListPlayerComponent;

	public FileDropTargetListener(EmbeddedMediaListPlayerComponent mediaListPlayerComponent) {
		this.mediaListPlayerComponent = mediaListPlayerComponent;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable transferable = dtde.getTransferable();
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				for (File file : fileList) {
					String absolutePath = file.getAbsolutePath();
					String fileExtension = getFileExtension(file);

					if (isSubtitleFile(fileExtension)) {
						new SrtParser();
						List<SrtParser.Subtitle> subtitles = SrtParser.parse(absolutePath);

						App.registerSubtitleForCurrentMedia(absolutePath, subtitles, mediaListPlayerComponent);
					} else {
						filePath = absolutePath;
						mediaListPlayerComponent.mediaListPlayer().list().media().add(filePath);
						System.out.println("Added to playlist: " + filePath);
						App.playlistModel.addElement(file.getName());
						App.playlistView.revalidate();
						App.playlistView.repaint();
						/* play file if no playlist */
						if (mediaListPlayerComponent.mediaListPlayer().list().media().count() == 1) {
							mediaListPlayerComponent.mediaListPlayer().controls().play();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dtde.dropComplete(true);
	}

	private String getFileExtension(File file) {
		// (same implementation as before)
		String fileName = file.getName();
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1).toLowerCase();
		}
		return "";
	}

	private boolean isSubtitleFile(String extension) {
		// (same implementation as before)
		return extension.equals("srt") || extension.equals("ass") || extension.equals("sub") || extension.equals("idx");
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
}