package org.adligo.xml_io.generator;

import java.io.IOException;
import java.lang.reflect.Field;

import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml_io.client.LetterCounter;
import org.adligo.xml_io.generator.models.ClassFieldMethods;
import org.adligo.xml_io.generator.models.GeneratorContext;

public class BaseConverterGenerator {
	ClassFieldMethods clazz;
	GeneratorContext ctx;
	Params params = new Params();
	Params toXml = new Params();
	String tagName = "";
	LetterCounter attributeLetterCounter = new LetterCounter();
	LetterCounter childNameLetterCounter = new LetterCounter();
	
	void setUpTagName() {
		if (ctx.isUseClassNamesInXml()) {
			Class<?> c = clazz.getClazz();
			tagName = c.getSimpleName();
		} else {
			tagName = ctx.getNextId();
		}
		
	}
	void writeFile(Class<?> clazz, Template template) throws IOException {
		String name = clazz.getSimpleName();
		String clazzName = name + "Generator";
		ctx.addClassToConverterNames(name, clazzName);
		
		try {
			Field serialVersionUID = clazz.getDeclaredField("serialVersionUID");
			serialVersionUID.setAccessible(true);
			Long value = serialVersionUID.getLong(clazz);
			params.addParam("version", value);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		}
		
		Package pkg = clazz.getPackage();
		
		String packageName = pkg.getName();
		params.addParam("package", packageName);
		params.addParam("className", clazzName);
		
		appendGenericClass(params);
		params.addParam("extraImports", clazz.getName());
		
		SourceFileWriter sfw = new SourceFileWriter();
		String dir = ctx.getPackageDirectory();
		sfw.setDir(dir);
		sfw.setTemplate(template);
		sfw.writeSourceFile(clazzName, params);
	}
	
	void appendGenericClass(Params parent) {
		Class<?> c = clazz.getClazz();
		String name = c.getSimpleName();
		parent.addParam("genericClass", name);
	}
	
}
