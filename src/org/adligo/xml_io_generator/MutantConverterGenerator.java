package org.adligo.xml_io_generator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.models.params.shared.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io.shared.I_AttributeConverter;
import org.adligo.xml_io.shared.I_AttributeSetter;
import org.adligo.xml_io.shared.I_FieldSetter;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.FieldMethods;
import org.adligo.xml_io_generator.models.FieldNameToUnderscore;
import org.adligo.xml_io_generator.models.GeneratorContext;

public class MutantConverterGenerator extends BaseConverterGenerator {
	private static final Log log = LogFactory.getLog(MutantConverterGenerator.class);
	
	private static final Templates templates = new Templates("/org/adligo/xml_io_generator/converter_template.xml", true);
	private static final Template template = templates.getTemplate("converter");

	
	public void generate(ClassFieldMethods cfm, GeneratorContext pctx) throws IOException {
		clazz = cfm;
		
		ctx = pctx;
		log.info("working on generators for class " + cfm.getClazz());
		
		BigDecimal version = clazz.calculateFieldVersion();
		String versionString = version.toPlainString();
		params.addParam("version", versionString);
		ctx.addToPackageVersion(version);
		
		setUpTagName();
		setupToXmlParams();
		Set<String> extraImports = new HashSet<String>();
		addAttributes(params, extraImports);
		for (String imp: extraImports) {
			ctx.addExtraImport(imp);
		}
		writeFile(cfm.getClazz(), template);
	}


	

	
	private void setupToXmlParams() {
		params.addParam("toXml",toXml);
		String ns = ctx.getNamespace();
		toXml.addParam("namespace", ns);
	}

	private void addAttributes(Params parent, Set<String> extraImports) throws IOException {
		List<FieldMethods> fields = clazz.getFieldMethods();
		boolean hasChildren = false;
		if (fields.size() > 0) {
			extraImports.add(Collections.class.getName());
			
			extraImports.add(I_FieldSetter.class.getName());
			extraImports.add(I_AttributeSetter.class.getName());
			
			extraImports.add(Map.class.getName());
			extraImports.add(HashMap.class.getName());
		}
		for (FieldMethods fm: fields) {
			
			if (fm.isAttribute()) {
				addAttributeParams(parent, fm, extraImports);
			} else {
				Class<?> fieldType = fm.getFieldClass();
				ClassFieldMethods cfm = new ClassFieldMethods(fieldType);
				if (cfm.isAttribute()) {
					addAttributeParams(parent, fm, extraImports);
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
					
					Class<?> clazz = fm.getFieldClass();
					
					if (!FieldMethods.isAttribute(clazz)) {
						String clazzName = fm.getFieldClassNameForImport();
						extraImports.add(clazzName);
					}
					
					addSetter(fm, childParams, extraImports);
				}
			}
		}
		if (hasChildren) {
			parent.addParam("hasChildren");
		} else {
			parent.addParam("doesNotHaveChildren");
		}
		if (clazz.isAttribute()) {
			extraImports.add(I_AttributeConverter.class.getName());
			Params attribParams = new Params();
			parent.addParam("attributeConverter", attribParams);
			attribParams.addParam("genericClass", clazz.getClazz().getSimpleName());
			FieldMethods fm = fields.get(0);
			Class<?> attribConstructorClass = clazz.getAttributeClass();
			extraImports.add(attribConstructorClass.getName());
			attribParams.addParam("constructorClass", attribConstructorClass.getSimpleName());
			addAttributeParams(attribParams, fm, extraImports);
		}
	}


	private void addAttributeParams(Params parent, FieldMethods fm, Set<String> extraImports) throws IOException  {
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
		
		Class<?> clazz = fm.getFieldClass();
		if (!FieldMethods.isAttribute(clazz)) {
			String clazzName = fm.getFieldClassNameForImport();
			extraImports.add(clazzName);
		}
		
		String fieldClass = fm.getFieldClassForSource();
		attributeParams.addParam("fieldClass", fieldClass);
		
		
		addSetter(fm, attributeParams, extraImports);
	}


	
}
