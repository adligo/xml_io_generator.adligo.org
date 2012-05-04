package org.adligo.xml_io.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.TemplateParserEngine;
import org.adligo.xml_io.generator.models.ClassFieldMethods;
import org.adligo.xml_io.generator.models.GeneratorContext;

public class BaseGenerator {
	ClassFieldMethods clazz;
	GeneratorContext ctx;
	Params params = new Params();
	Params toXml = new Params();
	String tagName = "";
	
	void setUpTagName() {
		if (ctx.isUseClassNamesInXml()) {
			Class<?> c = clazz.getClazz();
			tagName = c.getSimpleName();
		} else {
			tagName = ctx.getNextId();
		}
	}
	void writeFile(Class<?> clazz, Template template) throws IOException {
		String clazzName = clazz.getSimpleName() + "Generator";
		Package pkg = clazz.getPackage();
		
		String packageName = pkg.getName();
		params.addParam("package", packageName);
		params.addParam("className", clazzName);
		params.addParam("genericClass", clazz.getSimpleName());
		params.addParam("extraImports", clazz.getName());
		
		String dir = ctx.getPackageDirectory();
		String filePath = dir + File.separator + clazzName + ".java";
		File file = new File(filePath);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		String result = TemplateParserEngine.parse(template, params);
		fos.write(result.getBytes("UTF-16"));
		fos.close();
	}
}
