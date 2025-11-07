package vlcj.llg_mc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SrtParser {

	static class Subtitle {
		long startTime;
		long endTime;
		String text;
	}

	public static List<Subtitle> parse(String content) throws IOException {
		List<Subtitle> subtitles = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(content))) {
			String line;
			Subtitle currentSubtitle = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					if (currentSubtitle != null) {
						subtitles.add(currentSubtitle);
						currentSubtitle = null;
					}
					continue;
				}

				if (currentSubtitle == null) {
					// Try to parse timecodes (e.g., 00:00:01,500 --> 00:00:03,000)
					if (line.contains("-->")) {
						currentSubtitle = new Subtitle();
						String[] times = line.split("-->");
						currentSubtitle.startTime = parseTimeToMillis(times[0]);
						currentSubtitle.endTime = parseTimeToMillis(times[1]);
					}
				} else {
					if (currentSubtitle.text == null) {
						currentSubtitle.text = line;
					} else {
						currentSubtitle.text += "\n" + line;
					}
				}
			}
			if (currentSubtitle != null) {
				subtitles.add(currentSubtitle);
			}
		}
		return subtitles;
	}

	private static long parseTimeToMillis(String timeStr) {
		String[] parts = timeStr.trim().split("[:,]");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		int seconds = Integer.parseInt(parts[2]);
		int milliseconds = Integer.parseInt(parts[3]);
		return (hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds;
	}
}