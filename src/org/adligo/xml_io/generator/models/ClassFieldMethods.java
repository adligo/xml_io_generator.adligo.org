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
	/**
	 * should be set during the validate method
	 */
	private Class<?> immutableFieldType;
	/**
	 * should be set during the validate method
	 */
	private Class<?> constructorType;
	/**
	 * this is the name of the field in the immutable class that needs to be serialized;
	 */
	private String immutableFieldName;
	
	private Constructor constructor;
	
	public ClassFieldMethods(Class<?> clazz) {
		this.clazz = clazz;
		if (clazz.isInterface()) {
			return;
		}
		recurseAllParentFieldMethods(clazz);
		if (I_Immutable.class.isAssignableFrom(clazz)) {
			try {
				I_Immutable item = (I_Immutable) clazz.newInstance();
				immutableFieldName = item.getImmutableFieldName();
				if (immutableFieldName == null) {
					throw new IllegalArgumentException("I_Immutable " + 
							clazz + " should not return null from getImmutableFieldName()");
				}
				FieldMethods fieldM = null;
				for (FieldMethods fm: fieldMethods) {
					Field field = fm.getField();
					String name = field.getName();
					if (log.isInfoEnabled()) {
						log.info("checking field " + name);
					}
					if (immutableFieldName.equals(name)) {
						fieldM = fm;
						break;
					}
				}
				if (fieldM == null) {
					throw new IllegalArgumentException("I_Immutable " + 
							clazz + " does not have a non transient field " + immutableFieldName);
				}
				fieldMethods.clear();
				fieldMethods.add(fieldM);
				
				Field field = fieldM.getField();
				Class<?> fieldClass = field.getType();
				//loop through the constructors
				Constructor<?> [] constructors =  clazz.getConstructors();
				for (int i = 0; i < constructors.length; i++) {
					Constructor<?> con = constructors[i];
					Class<?> [] paramTypes = con.getParameterTypes();
					if (paramTypes.length == 1) {
						Class<?> constructorParamType = paramTypes[0];
						
						/*
						 * the constructor of the field's type has a single argument that 
						 * matches the single argument constructor of the immutable class
						 * so this is the match.
						 */
						if (constructorParamType.isAssignableFrom(fieldClass)) {
							immutableFieldType = fieldClass;
							constructorType = constructorParamType;
							constructor = con;
						}
					}
				}
			} catch (InstantiationException e) {
				log.error("the class " + clazz + " requires a default constructor.", e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
		} else {
			
		}
	}

	/**
	 * this should add all the fields to the fieldMethod instance
	 * respecting inheritance so parent first so that fields with the 
	 * same name use the leaf most instance.
	 * 
	 * @param clazz
	 */
	void recurseAllParentFieldMethods(Class<?> clazz) {
		Field [] fields = clazz.getDeclaredFields();
		
		Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			if (!Object.class.equals(superClazz)) {
				recurseAllParentFieldMethods(superClazz);
			}
		}
		
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
				if (log.isInfoEnabled()) {
					log.info("FieldMethod " + fm + " caused class " + clazz + " to not be a mutant.");
				}
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

		if (immutableFieldType != null && constructorType != null) {
			return true;
		}
		if (clazz.isEnum()) {
			return true;
		}
		return false;
	}
	
	public boolean isEnum() {
		return clazz.isEnum();
	}
	/**
	 * assumes isValid has been called
	 */
	public boolean isSimpleImmutable() {
		if (FieldMethods.ATTRIBUTE_CLASSES.contains(immutableFieldType)) {
			return true;
		}
		return false;
	}
	
	/**
	 *  a recursive method to find if this is a SimpleAttribute class
	 *  at it's root ie LongIdentifier -> LongIdentifierMutant -> Long yes it is
	 *  User -> UserMutant -> multiple fields no it is NOT
	 * @return
	 */
	public boolean isAttribute() {
		if (FieldMethods.isAttribute(clazz)) {
			return true;
		}
		if (isValid()) {
			if (isEnum()) {
				return true;
			}
			ClassFieldMethods cfm = new ClassFieldMethods(immutableFieldType);
			return cfm.isAttribute();
		} else if (fieldMethods.size() == 1) {
			FieldMethods fm = fieldMethods.get(0);
			Field field = fm.getField();
			Class<?> clazz = field.getType();
			ClassFieldMethods cfm = new ClassFieldMethods(clazz);
			return cfm.isAttribute();
		}
		return false;
	}
	
	
	public Class<?> getAttributeClass() {
		if (FieldMethods.isAttribute(clazz)) {
			return clazz;
		}
		if (isValid()) {
			ClassFieldMethods cfm = new ClassFieldMethods(immutableFieldType);
			return cfm.getAttributeClass();
		} else if (fieldMethods.size() == 1) {
			FieldMethods fm = fieldMethods.get(0);
			Field field = fm.getField();
			Class<?> clazz = field.getType();
			ClassFieldMethods cfm = new ClassFieldMethods(clazz);
			return cfm.getAttributeClass();
		}
		return null;
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

	public Class<?> getImmutableFieldType() {
		return immutableFieldType;
	}

	public String getImmutableFieldName() {
		return immutableFieldName;
	}

	public Class<?> getConstructorType() {
		return constructorType;
	}
	
	public Class<?>[] constructorExceptions() {
		return constructor.getExceptionTypes();
	}
}
