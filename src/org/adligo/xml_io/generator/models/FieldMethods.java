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

public class FieldMethods {
	private static final Log log = LogFactory.getLog(FieldMethods.class);
	private static Set<Class<?>> ATTRIBUTE_CLASSES = getAttributeClasses();
	
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
					if (returnType.equals(field.getType())) {
						getter = meth;
						log.debug("it is!");
					}
				} else if (new String("has" + name).equalsIgnoreCase(methodName) ) {
					Class<?> returnType = meth.getReturnType();
					if (returnType.equals(field.getType())) {
						getter = meth;
						log.debug("it is!");
					}
				} else if (new String("get" + name).equalsIgnoreCase(methodName) ) {
					Class<?> returnType = meth.getReturnType();
					if (returnType.equals(field.getType())) {
						getter = meth;
						log.debug("it is!");
					}
				}
			} else if (new String("get" + name).equalsIgnoreCase(methodName) ) {
				Class<?> returnType = meth.getReturnType();
				if (returnType.equals(field.getType())) {
					getter = meth;
					log.debug("it is!");
				}
			}
			
			log.debug("checking if method " + methodName + " is the setter for " + name);
			if (new String("set" + name).equalsIgnoreCase(methodName) ) {
				Class<?> [] params = meth.getParameterTypes();
				if (params.length == 1) {
					if (field.getType().equals(params[0])) {
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

	public Method getGetter() {
		return getter;
	}
	
	public boolean isAttribute() {
		Class<?> clazz = field.getType();
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
		return getter.getName();
	}
	
	public String getSetterName() {
		return setter.getName();
	}
}
