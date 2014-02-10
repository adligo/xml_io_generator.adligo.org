package org.adligo.xml_io_generator;

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
	
	public void writeSourceFile(String clazzName, I_TemplateParams params) {
		String filePath = dir + File.separator + clazzName + ".java";
		try {
			
			File file = new File(filePath);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			String result = TemplateParserEngine.parse(template, params);
			
			fos.write(result.getBytes("ASCII"));
			fos.close();
		} catch (UnsupportedEncodingException x) {
			throw new IllegalStateException("the encoding ASCII is required by the java specification");
		} catch (FileNotFoundException p) {
			throw new IllegalStateException("there was a problem working with the file " + filePath
					+ p.getMessage(), p);
		} catch (IOException x) {
			throw new IllegalStateException("there was a problem working with the file " + filePath
					+ x.getMessage(), x);
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
