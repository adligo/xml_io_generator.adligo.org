package org.adligo.xml_io.generator.models;

/**
 * note the version calculator is intended to create a non random
 * version number for each class, which generally increments as fields
 * are added.
 * 
 * @author scott
 *
 */
public class VersionCalculator {
	public static final int DIVISOR = 10000000;
	public static final int PACKGE_DIVISOR = 10;
	
	public static int calculate(String name) {
		int result = 0;
		char [] chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			result = result + chars[i];
		}
		return result;
	}
	
	public static int calculate(Class<?> clazz) {
		int result = 0;
		String name = clazz.getName();
		return calculate(name);
	}
}
