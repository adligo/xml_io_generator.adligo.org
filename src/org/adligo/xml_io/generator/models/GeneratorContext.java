package org.adligo.xml_io.generator.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.xml_io.client.LetterCounter;

public class GeneratorContext {
	private String packageDirectory;
	private String packageName;
	private LetterCounter counter = new LetterCounter();
	private String namespace;
	private SourceCodeGeneratorParams params;
	private Map<String,String> classToGeneratorNames = new HashMap<String, String>();
	private List<ClassFieldMethods> classFieldMethods = new ArrayList<ClassFieldMethods>();
	private String version = new Date(System.currentTimeMillis()).toString();
	
	public String getNextId() {
		return counter.getNextId();
	}

	public String getPackageDirectory() {
		return packageDirectory;
	}

	public void setPackageDirectory(String packageDirectory) {
		this.packageDirectory = packageDirectory;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public SourceCodeGeneratorParams getParams() {
		return params;
	}

	public void setParams(SourceCodeGeneratorParams params) {
		this.params = params;
	}

	public boolean isUseClassNamesInXml() {
		return params.isUseClassNamesInXml();
	}

	public boolean isUseFieldNamesInXml() {
		return params.isUseFieldNamesInXml();
	}
	
	public void addClassToConverterNames(String className, String converterName) {
		classToGeneratorNames.put(className, converterName);
	}

	public Set<Entry<String,String>> getClassToConverterNames() {
		return classToGeneratorNames.entrySet();
	}
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean addClassFieldMethods(ClassFieldMethods e) {
		return classFieldMethods.add(e);
	}

	public void clearClassFieldMethods() {
		classFieldMethods.clear();
	}

	public Iterator<ClassFieldMethods> classFieldMethodsIterator() {
		return classFieldMethods.iterator();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
