package org.adligo.xml_io_generator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.adligo.i.util.client.StringUtils;
import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml_io.client.LetterCounter;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.FieldMethods;
import org.adligo.xml_io_generator.models.GeneratorContext;

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
	
	void addSetter(FieldMethods fm, Params parentParams) {
		Class<?> setterParamClass = fm.getSetterParameterClass();
		if (!FieldMethods.isAttribute(setterParamClass)) {
			ctx.addExtraImport(setterParamClass.getName());
		} else if (FieldMethods.isImportClass(setterParamClass)) {
			ctx.addExtraImport(setterParamClass.getName());
		}
		String fieldClassCastable = FieldMethods.getClassCastableForSource(setterParamClass);
		parentParams.addParam("classCastable", fieldClassCastable);
		
		String setter = fm.getSetterName();
		parentParams.addParam("setter", setter);
		
		Class<?> [] setterExceptions = fm.getSetterExceptions();
		if (setterExceptions.length >= 1) {
			parentParams.addParam("setterThrowsExceptions");
			for (int i = 0; i < setterExceptions.length; i++) {
				Class<?> exception = setterExceptions[i];
				parentParams.addParam("setterException", exception.getSimpleName());
				ctx.addExtraImport(exception.getName());
			}
		}
	}
	
	void addConstructorExceptions(ClassFieldMethods cfm, Params parentParams) {
		
		Class<?> [] exceptions = cfm.constructorExceptions();
		if (exceptions.length >= 1) {
			parentParams.addParam("constructorThrowsExceptions");
			for (int i = 0; i < exceptions.length; i++) {
				Class<?> exception = exceptions[i];
				parentParams.addParam("constructorException", exception.getSimpleName());
				ctx.addExtraImport(exception.getName());
			}
		}
	}
}
