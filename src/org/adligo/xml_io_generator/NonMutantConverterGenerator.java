package org.adligo.xml_io_generator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io.client.I_AttributeConverter;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.FieldMethods;
import org.adligo.xml_io_generator.models.FieldNameToUnderscore;
import org.adligo.xml_io_generator.models.GeneratorContext;

public class NonMutantConverterGenerator extends BaseConverterGenerator {
	private static final Log log = LogFactory.getLog(NonMutantConverterGenerator.class);
	
	private static final Templates templates = new Templates("/org/adligo/xml_io/generator/converter_template.xml", true);
	private static final Template template = templates.getTemplate("immutableConverter");

	private Class<?> immutableFieldType;
	
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
		addAttributes(params);
		addConstructorExceptions(clazz, params);
		writeFile(cfm.getClazz(), template);
	}

	
	private void setupToXmlParams() {
		params.addParam("toXml",toXml);
		String ns = ctx.getNamespace();
		toXml.addParam("namespace", ns);
	}

	private void addAttributes(Params parent) {
		
		immutableFieldType = clazz.getImmutableFieldType();
		ctx.addExtraImport(immutableFieldType.getName());
		ClassFieldMethods cfm = new ClassFieldMethods(immutableFieldType);
		List<FieldMethods> fields = cfm.getFieldMethods();
		if (!ctx.isAwareOfClass(immutableFieldType)) {
			ctx.addExtraImport(immutableFieldType.getName());
		}
		parent.addParam("genericMutantClass", immutableFieldType.getSimpleName());
		boolean hasChildren = false;
		for (FieldMethods fm: fields) {
			String fname = fm.getName();
			if (log.isDebugEnabled()) {
				log.debug("working on field " + fname);
			}
			
			if (fm.isAttribute()) {
				addAttributeParams(parent, fm);
			} else {
				Class<?> fieldType = fm.getFieldClass();
					
				ClassFieldMethods cfmField = new ClassFieldMethods(fieldType);
				if (cfmField.isAttribute()) {
					addAttributeParams(parent, fm);
				} else {
					hasChildren = true;
					Params childParams = new Params();
					String fieldName = fm.getName();
					String childName = fieldName;
					if (!ctx.isUseFieldNamesInXml()) {
						childName = childNameLetterCounter.getNextId();
					}
					String attributeConstant =  immutableFieldType.getSimpleName() + "Generator." +
							FieldNameToUnderscore.toUnderscore(fieldName);
					parent.addParam("child", attributeConstant + "_CHILD", childParams);
					childParams.addParam("childName", childName);
					String getterName = fm.getGetterName();
					childParams.addParam("getter", getterName);
					appendGenericClass(childParams);
					
					String clazzName = fm.getFieldClassNameForImport();
					ctx.addExtraImport(clazzName);
					
					childParams.addParam("genericMutantClass", immutableFieldType.getSimpleName());
						
					addSetter(fm, childParams);
				}
			}
		}
		if (hasChildren) {
			parent.addParam("hasChildren");
		} else {
			parent.addParam("doesNotHaveChildren");
		}
		if (clazz.isAttribute()) {
			ctx.addExtraImport(I_AttributeConverter.class.getName());
			Params attribParams = new Params();
			parent.addParam("attributeConverter", attribParams);
			attribParams.addParam("genericClass", clazz.getClazz().getSimpleName());
			FieldMethods fm = fields.get(0);
			Class<?> attribConstructorClass = clazz.getAttributeClass();
			ctx.addExtraImport(attribConstructorClass.getName());
			attribParams.addParam("constructorClass", attribConstructorClass.getSimpleName());
			addAttributeParams(attribParams, fm);
		}
	}


	private void addAttributeParams(Params parent, FieldMethods fm) {
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
		String firstFieldName = immutableFieldType.getSimpleName();
		String shortGenClassName = immutableFieldType.getSimpleName() + "Generator";
		if (!ctx.isGeneratedClassesInThisPackage(shortGenClassName)) {
			try {
				Class<?> genClass = Class.forName(firstFieldName + "Generator");
				if (!ctx.isAwareOfClass(genClass)) {
					ctx.addExtraImport(genClass.getName());
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Error with attribute of class " + firstFieldName, e);
			}
		}
		
		String attributeConstant = shortGenClassName + "." +
			FieldNameToUnderscore.toUnderscore(fieldName);
		parent.addParam("attribute", attributeConstant + "_ATTRIBUTE", attributeParams);
		attributeParams.addParam("attributeName", attributeXml);
		String getterName = fm.getGetterName();
		attributeParams.addParam("getter", getterName);
		appendGenericClass(attributeParams);
		
		attributeParams.addParam("genericMutantClass", immutableFieldType.getSimpleName());
		
		Class<?> clazz = fm.getFieldClass();
		if (!FieldMethods.isAttribute(clazz)) {
			String clazzName = fm.getFieldClassNameForImport();
			ctx.addExtraImport(clazzName);
		}
		
		String fieldClass = fm.getFieldClassForSource();
		attributeParams.addParam("fieldClass", fieldClass);
		
		addSetter(fm, attributeParams);
	}
}
