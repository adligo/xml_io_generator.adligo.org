package org.adligo.xml_io_generator.models;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io.client.LetterCounter;

public class GeneratorContext {
	private LetterCounter counter = new LetterCounter();
	private String namespace;
	private SourceCodeGeneratorMemory memory;
	private Map<String,String> classToGeneratorNames = new HashMap<String, String>();
	private List<ClassFieldMethods> classFieldMethods = new ArrayList<ClassFieldMethods>();
	private String packageVersion;
	private BigDecimal packageVersionNumber = new BigDecimal(0);
	private List<Class<?>> mutants = new ArrayList<Class<?>>();
	private Map<String,String> classToAttributeGeneratorNames = new HashMap<String, String>();
	private Set<String> extraImports = new HashSet<String>();
	private Set<String> extraClassImports = new HashSet<String>();
	private Set<Class<?>> awareOfClasses = new HashSet<Class<?>>();
	private String oldPackageName;
	private File oldPackageDir;
	private String newPackageName;
	private File newPackageDir;
	
	public GeneratorContext() {
		awareOfClasses.addAll(FieldMethods.ATTRIBUTE_CLASSES);
	}
	
	public String getNextId() {
		return counter.getNextId();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public SourceCodeGeneratorMemory getMemory() {
		return memory;
	}

	public void setMemory(SourceCodeGeneratorMemory params) {
		this.memory = params;
	}

	public boolean isUseClassNamesInXml() {
		return memory.isUseClassNamesInXml();
	}

	public boolean isUseFieldNamesInXml() {
		return memory.isUseFieldNamesInXml();
	}
	
	public void addClassToConverterNames(String className, String converterName) {
		classToGeneratorNames.put(className, converterName);
	}

	public void addClassToAttributeConverterNames(String className, String converterName) {
		classToAttributeGeneratorNames.put(className, converterName);
	}
	
	public Set<Entry<String,String>> getClassToConverterNames() {
		return classToGeneratorNames.entrySet();
	}
	

	public boolean addClassFieldMethods(ClassFieldMethods e) {
		return classFieldMethods.add(e);
	}

	public void clearClassFieldMethods() {
		classFieldMethods.clear();
	}

	public Iterator<ClassFieldMethods> getClassFieldMethodsIterator() {
		return classFieldMethods.iterator();
	}

	
	public void addToPackageVersion(BigDecimal p) {
		packageVersionNumber = packageVersionNumber.add(p);
	}

	public String getPackageVersion() {
		if (StringUtils.isEmpty(packageVersion)) {
			BigDecimal toRet = packageVersionNumber.divide(
					new BigDecimal(VersionCalculator.PACKGE_DIVISOR));
			return toRet.toPlainString();
		}
		return packageVersion;
	}

	public void setPackageVersion(String packageVersion) {
		this.packageVersion = packageVersion;
	}

	public BigDecimal getPackageVersionNumber() {
		return packageVersionNumber;
	}

	public void setPackageVersionNumber(BigDecimal packageVersionNumber) {
		this.packageVersionNumber = packageVersionNumber;
	}

	public void addMutant(Class<?> c) {
		mutants.add(c);
	}
	
	public boolean isMutant(Class<?> c) {
		if (mutants.contains(c)) {
			return true;
		}
		return false;
	}

	public Set<Entry<String, String>> getClassToAttributeGeneratorNames() {
		return classToAttributeGeneratorNames.entrySet();
	}

	public Set<String> getExtraPackageImports() {
		return Collections.unmodifiableSet(extraImports);
	}

	public Set<String> getExtraClassImports() {
		return Collections.unmodifiableSet(extraClassImports);
	}
	
	public void addExtraImport(ClassLoader loader, String p) {
		extraImports.add(p);
		extraClassImports.add(p);
		try {
			Class<?> c = loader.loadClass(p);
			awareOfClasses.add(c);
		} catch (ClassNotFoundException x) {
			throw new IllegalArgumentException(x);
		}
	}
	
	public void clearExtraClassImports() {
		extraClassImports.clear();
	}
	
	public boolean isAwareOfClass(Class<?> c) {
		return awareOfClasses.contains(c);
	}
	
	public void clearExtraPackateImports() {
		extraImports.clear();
	}
	
	public boolean isGeneratedClassesInThisPackage(String simpleName) {
		Collection<String> classes = classToGeneratorNames.values();
		return classes.contains(simpleName);
	}

	public String getOldPackageName() {
		return oldPackageName;
	}

	public void setOldPackageName(String oldPackageName) {
		this.oldPackageName = oldPackageName;
	}

	public File getOldPackageDir() {
		return oldPackageDir;
	}

	public void setOldPackageDir(File oldPackageDir) {
		this.oldPackageDir = oldPackageDir;
	}

	public String getNewPackageName() {
		return newPackageName;
	}

	public void setNewPackageName(String newPackageName) {
		this.newPackageName = newPackageName;
	}

	public File getNewPackageDir() {
		return newPackageDir;
	}

	public void setNewPackageDir(File newPackageDir) {
		this.newPackageDir = newPackageDir;
	}

	public ClassLoader getClassloader() {
		return memory.getClassloader();
	}
	
}
