package org.adligo.xml_io.generator.models;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.I_Immutable;

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
	 * it the class has a single argument constructor 
	 * which can be cast to one of its fields
	 * @return
	 */
	public boolean isValid() {
		if (I_Immutable.class.isAssignableFrom(clazz)) {
			try {
				I_Immutable item = (I_Immutable) clazz.newInstance();
				String fieldName = item.getImmutableFieldName();
				if (fieldName == null) {
					throw new IllegalArgumentException("I_Immutable " + 
							clazz + " should not return null from getImmutableFieldName()");
				}
				FieldMethods fieldM = null;
				for (FieldMethods fm: fieldMethods) {
					Field field = fm.getField();
					String name = field.getName();
					if (fieldName.equals(name)) {
						fieldM = fm;
						break;
					}
				}
				if (fieldM == null) {
					throw new IllegalArgumentException("I_Immutable " + 
							clazz + " does not have a non transient field " + fieldName);
				}
				Field field = fieldM.getField();
				Class<?> fieldClass = field.getType();
				
				Constructor<?> [] constructors =  clazz.getConstructors();
				for (int i = 0; i < constructors.length; i++) {
					Constructor<?> con = constructors[i];
					Class<?> [] paramTypes = con.getParameterTypes();
					if (paramTypes.length == 1) {
						Class<?> paramType = paramTypes[0];
						
						Constructor<?> [] fieldConstructors =  fieldClass.getConstructors();
						for (int j = 0; j < fieldConstructors.length; j++) {
							Constructor<?> fieldCon= constructors[i];
							Class<?> [] fieldParamTypes = fieldCon.getParameterTypes();
							if (fieldParamTypes.length == 1) {
								Class<?> fieldParamType = fieldParamTypes[0];
								
								/*
								 * the constructor of the field's type has a single argument that 
								 * matches the single argument constructor of the immutable class
								 * so this is the match.
								 */
								if (paramType.equals(fieldParamType)) {
									return true;
								}
							}
						}
					}
				}
			} catch (InstantiationException e) {
				log.error("the class " + clazz + " requires a default constructor.", e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
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

	public BigDecimal calculateFieldVersion() {
		long result = 0;
		result = VersionCalculator.calculate(clazz);
		result = result * 10;
		for (int i = 0; i < fieldMethods.size(); i++) {
			FieldMethods fm = fieldMethods.get(i);
			String name = fm.getName();
			
			result = result + VersionCalculator.calculate(name);
			Field field = fm.getField();
			Class<?> type = field.getType();
			result = result + VersionCalculator.calculate(type);
		}
		
		BigDecimal bd = new BigDecimal(result);
		bd = bd.divide(new BigDecimal(VersionCalculator.DIVISOR));
		bd = bd.add(new BigDecimal(getSerialVersionUID()));
		return bd;
	}
	
	public long getSerialVersionUID() {
		try {
			Field serialVersionUID = clazz.getDeclaredField("serialVersionUID");
			serialVersionUID.setAccessible(true);
			return serialVersionUID.getLong(clazz);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("unable to identify serialVersionUID for class " + clazz, e);
		}
	}
}
