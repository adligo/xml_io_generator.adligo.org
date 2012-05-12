package org.adligo.xml_io.generator.models;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.xml_io.client.converters.DefaultNamespaceConverters;


public class FieldMethods {
	public static final String MAP = "Map";
	public static final String COLLECTION = "Collection";
	private static final Log log = LogFactory.getLog(FieldMethods.class);
	protected static Set<Class<?>> ATTRIBUTE_CLASSES = getAttributeClasses();
	
	private static Set<Class<?>> getAttributeClasses() {
		Set<Class<?>> toRet = new HashSet<Class<?>>();
		toRet.add(String.class);
		toRet.add(Boolean.class);
		toRet.add(Byte.class);
		toRet.add(Integer.class);
		toRet.add(Short.class);
		toRet.add(Long.class);
		toRet.add(Double.class);
		toRet.add(Float.class);
		toRet.add(BigDecimal.class);
		toRet.add(BigInteger.class);
		toRet.add(Character.class);
		toRet.add((new byte[]{}).getClass());
		toRet.add((new boolean[]{}).getClass());
		toRet.add((new char[]{}).getClass());
		return Collections.unmodifiableSet(toRet);
	}
	
	private Field field;
	private Method setter;
	private Method getter;
	
	public FieldMethods(Class<?> clazz, Field field) {
		this.field = field;
		String name = field.getName();
		Method [] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method meth = methods[i];
			String methodName = meth.getName();
			log.debug("checking if method " + methodName + " is the getter for " + name);
			
			Class<?> type = field.getType();
			if (isBoolean(type)) {
				if (new String("is" + name).equalsIgnoreCase(methodName) ) {
					Class<?> returnType = meth.getReturnType();
					Class<?> fieldClass = field.getType();
					if (returnType.isAssignableFrom(fieldClass)) {
						getter = meth;
						log.debug("it is!");
					}
				} else if (new String("has" + name).equalsIgnoreCase(methodName) ) {
					Class<?> returnType = meth.getReturnType();
					Class<?> fieldClass = field.getType();
					if (returnType.isAssignableFrom(fieldClass)) {
						getter = meth;
						log.debug("it is!");
					}
				} else if (new String("get" + name).equalsIgnoreCase(methodName) ) {
					Class<?> returnType = meth.getReturnType();
					Class<?> fieldClass = field.getType();
					if (returnType.isAssignableFrom(fieldClass)) {
						getter = meth;
						log.debug("it is!");
					}
				}
			} else if (new String("get" + name).equalsIgnoreCase(methodName) ) {
				Class<?> returnType = meth.getReturnType();
				Class<?> fieldClass = field.getType();
				if (returnType.isAssignableFrom(fieldClass)) {
					getter = meth;
					log.debug("it is!");
				}
			}
			
			log.debug("checking if method " + methodName + " is the setter for " + name);
			if (new String("set" + name).equalsIgnoreCase(methodName) ) {
				Class<?> [] params = meth.getParameterTypes();
				if (params.length == 1) {
					Class<?> param = params[0];
					Class<?> fieldClass = field.getType();
					if (param.isAssignableFrom(fieldClass)) {
						setter = meth;
						log.debug("it is!");
					}
				}
			}
			
			if (setter != null && getter != null) {
				break;
			}
		}
	}

	private boolean isBoolean(Class<?> type) {
		if (Boolean.class.equals(type)) {
			return true;
		}
		if (type.isPrimitive()) {
			String name = type.getName();
			if ("boolean".equals(name)) {
				return true;
			}
		}
		return false;
	}

	public Field getField() {
		return field;
	}

	public Method getSetter() {
		return setter;
	}
	
	public Class<?> getSetterParameterClass() {
		return setter.getParameterTypes()[0];
	}

	public Method getGetter() {
		return getter;
	}
	
	public boolean isAttribute() {
		return isAttribute(field.getType());
	}
	
	public static boolean isAttribute(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return true;
		}
		if (ATTRIBUTE_CLASSES.contains(clazz)) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		return field.getName();
	}
	
	public String getGetterName() {
		if (getter == null) {
			throw new NullPointerException("getter is null for " + field.getName());
		}
		return getter.getName();
	}
	
	public String getSetterName() {
		if (setter == null) {
			throw new NullPointerException("setter is null for " + field.getName());
		}
		return setter.getName();
	}
	public Class<?> getFieldClass() {
		return field.getType();
	}
	
	public String getFieldClassForSource() {
		Class<?> type = field.getType();
		
		if (type.equals(DefaultNamespaceConverters.CHAR_ARRAY_CLASS)) {
			return "DefaultNamespaceConverters.CHAR_ARRAY_CLASS";
		}
		if (type.equals(DefaultNamespaceConverters.BOOLEAN_ARRAY_CLASS)) {
			return "DefaultNamespaceConverters.BOOLEAN_ARRAY_CLASS";
		}
		if (type.equals(DefaultNamespaceConverters.BYTE_ARRAY_CLASS)) {
			return "DefaultNamespaceConverters.BYTE_ARRAY_CLASS";
		}
		return type.getSimpleName() + ".class";
	}
	
	public static String getClassCastableForSource(Class<?> type) {
		if (type.equals(DefaultNamespaceConverters.CHAR_ARRAY_CLASS)) {
			return "char []";
		}
		if (type.equals(DefaultNamespaceConverters.BOOLEAN_ARRAY_CLASS)) {
			return "boolean []";
		}
		if (type.equals(DefaultNamespaceConverters.BYTE_ARRAY_CLASS)) {
			return "byte []";
		}
		String toRet = type.getSimpleName();
		if (type.isPrimitive()) {
			if ("boolean".equals(toRet)) {
				return "Boolean";
			}
			if ("byte".equals(toRet)) {
				return "Byte";
			}
			if ("char".equals(toRet)) {
				return "Character";
			}
			if ("short".equals(toRet)) {
				return "Short";
			}
			if ("int".equals(toRet)) {
				return "Integer";
			}
			if ("long".equals(toRet)) {
				return "Long";
			}
			if ("double".equals(toRet)) {
				return "Double";
			}
			if ("float".equals(toRet)) {
				return "Float";
			}
		}
		return toRet;
	}
	
	public String getFieldClassNameForImport() {
		Class<?> clazz = field.getType();
		return clazz.getName();
	}

	@Override
	public String toString() {
		String toRet = "FieldMethods [field=" + field.getName();
		if (setter != null) {
			toRet = toRet + ", setter=" + setter.getName();
		} else {
			toRet = toRet + ", setter=null";
		}
		if (getter != null) {
			toRet = toRet + ", getter=" + getter.getName();
		} else {
			toRet = toRet + ", getter=null";
		}
		return toRet;
	}
	
	public Class<?>[] getSetterExceptions() {
		return setter.getExceptionTypes();
	}
	
	public Class<?>[] getGetterExceptions() {
		return getter.getExceptionTypes();
	}
}
