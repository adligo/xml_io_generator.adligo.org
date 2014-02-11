package org.adligo.xml_io_generator.models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SourceCodeGeneratorParams {
	private String path;
	
	private List<String> classpath;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<String> getClasspath() {
		return classpath;
	}
	public List<URL> getClasspathAsUrls() throws MalformedURLException {
		List<URL> cpList = new ArrayList<URL>();
		for (String entry: classpath) {
			cpList.add(new URL("file://" + entry));
		}
		return cpList;
	}
	
	public void setClasspath(List<String> classpath) {
		this.classpath = new ArrayList(classpath);
	}
}
