package org.adligo.xml_io.generator.models;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;

public class ClassFieldMethods {
	private static final Log log = LogFactory.getLog(ClassFieldMethods.class);
	private Class<?> clazz;
	private List<FieldMethods> fieldMethods = new ArrayList<FieldMethods>();
	
	public ClassFieldMethods(Class<?> clazz) {
		this.clazz = clazz;
		Field [] fields = clazz.getDeclaredFields();
		
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			int modifiers = f.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				if (!Modifier.isTransient(modifiers)) {
					FieldMethods fm = new FieldMethods(clazz, f);
					fieldMethods.add(fm);
				}
			}
		}
	}
	
	/**
	 * return true if this class has a getter and setter for each 
	 * non transient method
	 * @return
	 */
	public boolean isMutant() {
		for (FieldMethods fm : fieldMethods) {
			if (fm.getGetter() == null || fm.getSetter() == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * if this class has a single field
	 * and if that fields type matches with a single parameter constructor
	 * @return
	 */
	public boolean isValid() {
		if (fieldMethods.size() == 1) {
			FieldMethods fm = fieldMethods.get(0);
			Field field = fm.getField();
			Class<?> type = field.getType();
			
			Constructor<?> [] constructors =  clazz.getConstructors();
			for (int i = 0; i < constructors.length; i++) {
				Constructor<?> con = constructors[i];
				Class<?> [] paramTypes = con.getParameterTypes();
				if (paramTypes.length == 1) {
					Class<?> paramType = paramTypes[0];
					if (type.isAssignableFrom(paramType)) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public String logFieldMethods() {
		StringBuilder sb = new StringBuilder();
		sb.append("The Class " + clazz + " has the following fields, getters and setters \r\n");
		for (FieldMethods fm: fieldMethods) {
			Field field = fm.getField();
			String name = field.getName();
			sb.append(name);
			sb.append(" ");
			
			Method getter = fm.getGetter();
			if (getter != null) {
				String getterName = getter.getName();
				sb.append(getterName);
				sb.append(" ");
			} else {
				sb.append("NO_GETTER ");
			}
			
			Method setter = fm.getSetter();
			if (setter != null) {
				String setterName = setter.getName();
				sb.append(setterName);
			} else {
				sb.append("NO_SETTER ");
			}
			sb.append("\r\n");
		}
		String toRet = sb.toString();
		return toRet;
		
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public List<FieldMethods> getFieldMethods() {
		return fieldMethods;
	}
}
