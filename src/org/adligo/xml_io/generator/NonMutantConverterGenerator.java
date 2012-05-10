package org.adligo.xml_io.generator;

import java.io.IOException;

import org.adligo.xml_io.generator.models.ClassFieldMethods;
import org.adligo.xml_io.generator.models.FieldMethods;
import org.adligo.xml_io.generator.models.GeneratorContext;

public class NonMutantConverterGenerator extends BaseConverterGenerator {
	Class<?> wrappedFieldType;
	private boolean simple = false;
	
	public void generate(ClassFieldMethods cfm, GeneratorContext pctx) throws IOException {
		clazz = cfm;
		wrappedFieldType = cfm.getImmutableFieldType();
		if (FieldMethods.ATTRIBUTE_CLASSES.contains(wrappedFieldType)) {
			simple = true;
		}
		
	}
}
