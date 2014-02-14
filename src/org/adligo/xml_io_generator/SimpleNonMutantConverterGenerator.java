package org.adligo.xml_io_generator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.models.params.shared.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.FieldMethods;
import org.adligo.xml_io_generator.models.FieldNameToUnderscore;
import org.adligo.xml_io_generator.models.GeneratorContext;

public class SimpleNonMutantConverterGenerator extends BaseConverterGenerator {
	private static final Log log = LogFactory.getLog(SimpleNonMutantConverterGenerator.class);
	
	private static final Templates templates = new Templates("/org/adligo/xml_io_generator/converter_template.xml", true);
	private static final Template template = templates.getTemplate("simpleImutableConverter");

	
	public void generate(ClassFieldMethods cfm, GeneratorContext pctx) throws IOException {
		clazz = cfm;
		
		ctx = pctx;
		log.info("working on generators for class " + cfm.getClazz());
		
		BigDecimal version = clazz.calculateFieldVersion();
		String versionString = version.toPlainString();
		params.addParam("version", versionString);
		ctx.addToPackageVersion(version);
		
		String genericClassName = clazz.getClazz().getSimpleName();
		ctx.addClassToAttributeConverterNames(genericClassName, genericClassName + "Generator");
		setUpTagName();
		setupToXmlParams();
		Set<String> extraImports = new HashSet<String>();
		addAttributes(params);
		addConstructorExceptions(clazz, params, extraImports);
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

	private void addAttributes(Params parent) {
		List<FieldMethods> fields = clazz.getFieldMethods();
		Class<?> type = clazz.getImmutableFieldType();
		String name = clazz.getImmutableFieldName();
		parent.addParam("constructorClass", type.getSimpleName());
		
		
		for (FieldMethods fm: fields) {
			String fname = fm.getName();
			if (log.isDebugEnabled()) {
				log.debug("working on field " + fname);
			}
			if (fm.isAttribute()) {

				Params attributeParams = new Params();
				String fieldName = fm.getName();
				if (name.equals(fieldName)) {
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
				}
			} 
		}
	}
}
