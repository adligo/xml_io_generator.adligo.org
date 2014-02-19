package org.adligo.xml_io_generator.models;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.xml_io.shared.I_AttributeConverter;
import org.adligo.xml_io.shared.NamespaceConverters;
import org.adligo.xml_io.shared.converters.DefaultNamespaceConverters;

public class ClassFieldMethods {
	public static final String UNABLE_TO_USE_I_IMMUTABLE = "Unable to use class org.adligo.models.core.I_Immutable from the custom url class loader.";
	private static final Log log = LogFactory.getLog(ClassFieldMethods.class);
	private static Class<?> I_IMMUTABLE;
	
	public static Class<?> getI_IMMUTABLE() {
		return I_IMMUTABLE;
	}

	public static void setI_IMMUTABLE(Class<?> i_IMMUTABLE) {
		I_IMMUTABLE = i_IMMUTABLE;
	}

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
	
	private Constructor<?> constructor;
	
	public ClassFieldMethods(Class<?> clazz) throws IOException {
		if (log.isInfoEnabled()) {
			log.info("Working on class " + clazz);
		}
		this.clazz = clazz;
		if (clazz.isInterface()) {
			return;
		}
		recurseAllParentFieldMethods(clazz);
		
		if (I_IMMUTABLE.isAssignableFrom(clazz)) {
			if (log.isInfoEnabled()) {
				log.info("The class " + clazz + " is I_Immutable.");
			}
			try {
				Object item = clazz.newInstance();
				Method m = clazz.getMethod("getImmutableFieldName", new Class [] {});
				immutableFieldName = (String) m.invoke(item, new Class [] {});
				
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
				throw new IOException(UNABLE_TO_USE_I_IMMUTABLE, e);
			} catch (NoSuchMethodException e) {
				throw new IOException(UNABLE_TO_USE_I_IMMUTABLE, e);
			} catch (SecurityException e) {
				throw new IOException(UNABLE_TO_USE_I_IMMUTABLE, e);
			} catch (IllegalArgumentException e) {
				throw new IOException(UNABLE_TO_USE_I_IMMUTABLE, e);
			} catch (InvocationTargetException e) {
				throw new IOException(UNABLE_TO_USE_I_IMMUTABLE, e);
			}
		} else {
			if (fieldMethods.size() == 1) {
				Class<?> fc = fieldMethods.get(0).getFieldClass();
				NamespaceConverters nc = DefaultNamespaceConverters.getDefaultNamespaceConverters();
				I_AttributeConverter<?> ac = nc.getAttributeConverter(fc);
				
				if (!fc.isInterface() && ac == null) {
					throw new IllegalStateException("The class " + clazz +
							" has no fields and is not assignable to I_Immutable");
				}
			}
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
					log.info("FieldMethod " + fm + " caused class " + clazz + " to be a NonMutant.");
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
		if (immutableFieldType == null) {
			log.error("The immutable field type for class " + clazz + " is null.");
		}
		if (constructorType == null) {
			log.error("The constructor type for class " + clazz + " is null.");
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
	public boolean isAttribute() throws IOException {
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
	
	
	public Class<?> getAttributeClass() throws IOException {
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
		if (Serializable.class.isAssignableFrom(clazz) ) {
			bd = bd.add(new BigDecimal(getSerialVersionUID()));
		} 
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
