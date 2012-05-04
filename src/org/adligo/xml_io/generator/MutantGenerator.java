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
import org.adligo.xml_io.generator.models.GeneratorContext;

public class MutantGenerator extends BaseGenerator {
	private static final Templates templates = new Templates("/org/adligo/xml_io/generator/converter_template.xml", true);
	private static final Template template = templates.getTemplate("converter");

	
	public void generate(ClassFieldMethods cfm, GeneratorContext pctx) throws IOException {
		clazz = cfm;
		ctx = pctx;
		setUpTagName();
		setupToXmlParams();
		
		writeFile(cfm.getClazz(), template);
	}
	
	private void setupFromXmlParams() {
		
		
	}
	
	private void setupToXmlParams() {
		params.addParam("toXml",toXml);
		String ns = ctx.getNamespace();
		toXml.addParam("namespace", ns);
		toXml.addParam("tagName", tagName);
		
		List<FieldMethods> fields = clazz.getFieldMethods();
		for (FieldMethods fm: fields) {
			if (fm.isAttribute()) {
				Params attributeParams = new Params();
				String attributeName = fm.getName();
				if (!ctx.isUseFieldNamesInXml()) {
					attributeName = ctx.getNextId();
				}
				toXml.addParam("attribute", attributeName, attributeParams);
				String getterName = fm.getGetterName();
				attributeParams.addParam("getter", getterName);
			} else {
				//todo
			}
		}
	}
}
