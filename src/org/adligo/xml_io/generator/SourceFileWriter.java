package org.adligo.xml_io.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.adligo.models.params.client.I_TemplateParams;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.TemplateParserEngine;

public class SourceFileWriter {
	private Template template;
	private String dir;
	
	public void writeSourceFile(String clazzName, I_TemplateParams params) throws IOException {
		try {
			String filePath = dir + File.separator + clazzName + ".java";
			File file = new File(filePath);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			String result = TemplateParserEngine.parse(template, params);
			
			fos.write(result.getBytes("ASCII"));
			fos.close();
		} catch (UnsupportedEncodingException x) {
			throw new IllegalStateException("the encoding ASCII is required by the java specification");
		} catch (FileNotFoundException p) {
			throw new IOException(p);
		}
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
}
