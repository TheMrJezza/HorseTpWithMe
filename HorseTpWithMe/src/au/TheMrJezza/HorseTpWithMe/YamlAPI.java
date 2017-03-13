package au.TheMrJezza.HorseTpWithMe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class YamlAPI {
	private File file;

	public YamlAPI(File file) {
		this.file = file;
	}

	public void comment(String comment) throws IOException {
		comment(comment, true);
	}

	public void comment(String comment, boolean append) throws IOException {
		if (!comment.startsWith("#") && !comment.equals(""))
			comment = "# " + comment;
		FileWriter fw = new FileWriter(file, append);
		fw.write(comment);
		fw.write(System.lineSeparator());
		fw.close();
	}

	public void addString(String varible, String value) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		fw.write(varible + ": " + '"' + value + '"');
		fw.write(System.lineSeparator());
		fw.close();
	}

	public void addBoolean(String varible, boolean value) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		fw.write(varible + ": " + String.valueOf(value));
		fw.write(System.lineSeparator());
		fw.close();
	}

	public void addList(String varible, List<String> list) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		fw.write(varible + ":");
		fw.write(System.lineSeparator());
		for (String val : list) {
			fw.write("- " + val);
			fw.write(System.lineSeparator());
		}
		fw.close();
	}
}