package vlcj.llg_mc;

public class Subs {
	private String name;
	private String link;

	@Override
	public String toString() {
		return name; // This will be displayed by default
	}

	public Subs(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}
}
