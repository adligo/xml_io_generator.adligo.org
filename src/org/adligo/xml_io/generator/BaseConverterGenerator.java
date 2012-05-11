package org.adligo.xml_io.generator;

import java.io.IOException;
import java.util.Set;

import org.adligo.i.util.client.StringUtils;
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

		params.addParam("tagName", tagName);
	}
	void writeFile(Class<?> clazz, Template template) throws IOException {
		String name = clazz.getSimpleName();
		String clazzName = name + "Generator";
		ctx.addClassToConverterNames(name, clazzName);
		
		Set<String> extraImports = ctx.getExtraClassImports();
		for (String p: extraImports) {
			params.addParam("extraImport", p);
		}
		
		Package pkg = clazz.getPackage();
		
		String packageName = pkg.getName();
		String packageSuffix = ctx.getPackageSuffix();
		if (!StringUtils.isEmpty(packageSuffix)) {
			packageName = packageName + "." + packageSuffix;
		}
		params.addParam("packageName", packageName);
		params.addParam("className", clazzName);
		appendGenericClass(params);
		
		if (!StringUtils.isEmpty(packageSuffix)) {
			params.addParam("extraImport",clazz.getName());
		}
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
