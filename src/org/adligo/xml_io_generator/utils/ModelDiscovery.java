package org.adligo.xml_io_generator.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ModelDiscovery {
	private List<Class<?>> models = new ArrayList<Class<?>>();
	
	public ModelDiscovery(PackageUtils pu, String packageName) throws IOException, ClassNotFoundException {
		List<String> classNames = pu.getClassesForPackage(packageName);
		for (String name: classNames) {
			String nameWithOutDotClass = name.substring(0, name.length() - 6);
			Class<?> clazz = Class.forName(packageName + "." + nameWithOutDotClass);
			if (!clazz.isInterface()) {
				models.add(clazz);
			}
		}
	}
	
	public List<Class<?>> getModels() {
		return models;
	}
	
}
