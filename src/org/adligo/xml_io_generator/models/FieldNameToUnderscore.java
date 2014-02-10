package org.adligo.xml_io_generator.models;

public class FieldNameToUnderscore {

	public static String toUnderscore(String fieldName) {
		if (fieldName.contains("_")) {
			return fieldName.toUpperCase();
		}
		StringBuilder sb = new StringBuilder();
		char [] chars = fieldName.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(c);
			} else {
				sb.append(c);
			}
		}
		String result = sb.toString();
		return result.toUpperCase();
	}
}
