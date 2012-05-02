package org.adligo.xml_io.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.adligo.xml_io.generator.models.SourceCodeGeneratorParams;

public class SourceCodeGenerator {

	public static void generate(SourceCodeGeneratorParams params) {
		Set<String> packages = params.getPackages();
		for (String name : packages) {
			List<Class<?>> classes = params.getClasses(name);
			for (Class<?> clazz : classes) {
				
			}
		}
	}
	
	private static void generate(Class<?> clazz, SourceCodeGeneratorParams params) {
		
		
		
	}
}
