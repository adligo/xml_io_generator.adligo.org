package org.adligo.xml_io.generator.models;

import org.adligo.xml_io.client.LetterCounter;

public class GeneratorContext {
	private String packageDirectory;
	private LetterCounter counter = new LetterCounter();
	private String namespace;
	private SourceCodeGeneratorParams params;
	
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
}
