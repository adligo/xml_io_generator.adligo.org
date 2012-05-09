package org.adligo.xml_io.generator.models;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io.client.LetterCounter;

public class GeneratorContext {
	private String packageDirectory;
	private String packageName;
	private LetterCounter counter = new LetterCounter();
	private String namespace;
	private SourceCodeGeneratorParams params;
	private Map<String,String> classToGeneratorNames = new HashMap<String, String>();
	private List<ClassFieldMethods> classFieldMethods = new ArrayList<ClassFieldMethods>();
	private String packageVersion;
	private BigDecimal packageVersionNumber = new BigDecimal(0);
	private String packageSuffix;
	
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

	public String getPackageSuffix() {
		return packageSuffix;
	}

	public void setPackageSuffix(String packageSuffix) {
		this.packageSuffix = packageSuffix;
	}
}
