package org.adligo.xml_io.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.TemplateParserEngine;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io.generator.models.ClassFieldMethods;
import org.adligo.xml_io.generator.models.FieldMethods;
import org.adligo.xml_io.generator.models.FieldNameToUnderscore;
import org.adligo.xml_io.generator.models.GeneratorContext;

public class MutantConverterGenerator extends BaseConverterGenerator {
	private static final Templates templates = new Templates("/org/adligo/xml_io/generator/converter_template.xml", true);
	private static final Template template = templates.getTemplate("converter");

	
	public void generate(ClassFieldMethods cfm, GeneratorContext pctx) throws IOException {
		clazz = cfm;
		ctx = pctx;
		setUpTagName();
		setupToXmlParams();
		addAttributes(params);
		writeFile(cfm.getClazz(), template);
	}
	
	private void setupFromXmlParams() {
		
		
	}
	
	private void setupToXmlParams() {
		params.addParam("toXml",toXml);
		String ns = ctx.getNamespace();
		toXml.addParam("namespace", ns);
	}

	private void addAttributes(Params parent) {
		parent.addParam("tagName", tagName);
		List<FieldMethods> fields = clazz.getFieldMethods();
		boolean hasChildren = false;
		for (FieldMethods fm: fields) {
			if (fm.isAttribute()) {
				Params attributeParams = new Params();
				String fieldName = fm.getName();
				String attributeXml = fieldName;
				if (!ctx.isUseFieldNamesInXml()) {
					attributeXml = attributeLetterCounter.getNextId();
					if ("n".equals(attributeXml)) {
						//n is reserved for parent child objects the name attribute
						attributeXml = attributeLetterCounter.getNextId();
					}
				}
				String attributeConstant = FieldNameToUnderscore.toUnderscore(fieldName);
				parent.addParam("attribute", attributeConstant + "_ATTRIBUTE", attributeParams);
				attributeParams.addParam("attributeName", attributeXml);
				String getterName = fm.getGetterName();
				attributeParams.addParam("getter", getterName);
				appendGenericClass(attributeParams);
				
				
				String fieldClass = fm.getFieldClassForSource();
				attributeParams.addParam("fieldClass", fieldClass);
				
				String fieldClassCastable = fm.getFieldClassCastableForSource();
				attributeParams.addParam("fieldClassCastable", fieldClassCastable);
				
				String setter = fm.getSetterName();
				attributeParams.addParam("setter", setter);
			} else {
				hasChildren = true;
				Params childParams = new Params();
				String fieldName = fm.getName();
				String childName = fieldName;
				if (!ctx.isUseFieldNamesInXml()) {
					childName = childNameLetterCounter.getNextId();
				}
				String attributeConstant = FieldNameToUnderscore.toUnderscore(fieldName);
				parent.addParam("child", attributeConstant + "_CHILD", childParams);
				childParams.addParam("childName", childName);
				String getterName = fm.getGetterName();
				childParams.addParam("getter", getterName);
				appendGenericClass(childParams);
				
				String clazzName = fm.getFieldClassNameForImport();
				params.addParam("extraImport", clazzName);
				
				String fieldClassCastable = fm.getFieldClassCastableForSource();
				childParams.addParam("childClassCastable", fieldClassCastable);
				
				String setter = fm.getSetterName();
				childParams.addParam("setter", setter);
			}
		}
		if (hasChildren) {
			parent.addParam("hasChildren");
		} else {
			parent.addParam("doesNotHaveChildren");
		}
	}
}
