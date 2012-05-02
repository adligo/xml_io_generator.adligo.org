package org.adligo.xml_io.generator.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SourceCodeGeneratorParams {
	private Map<String, List<Class<?>>> packageClassModels = new HashMap<String, List<Class<?>>>();
	private String outputDirectory;
	
	public void addClass(Class<?> clazz) {
		Package pkg = clazz.getPackage();
		String name = pkg.getName();
		List<Class<?>> classes = packageClassModels.get(name);
		classes.add(clazz);
	}
	
	public Set<String> getPackages() {
		return packageClassModels.keySet();
	}
	
	public List<Class<?>> getClasses(String packageName) {
		return packageClassModels.get(packageName);
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
}
