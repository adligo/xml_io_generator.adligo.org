package org.adligo.xml_io_generator.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;


public class PackageUtils {
	private static final Log log = LogFactory.getLog(PackageUtils.class);
	
	private List<String> cpJarEntries = new ArrayList<String>();
	private List<String> cpDirEntries = new ArrayList<String>();
	/**
	 * map of package names to class names
	 * from the input jars and dirs
	 */
	private Map<String,List<String>> packageClassMap= new HashMap<String, List<String>>();
	private File explodeTemp;
	private List<String> ignoreList = new ArrayList<String>();
	

	

	public void decompress(List<String> classpathEntries) {
		if (explodeTemp.exists()) {
			recursiveDelete(explodeTemp);
		}
		explodeTemp.mkdir();
		
		for (String entry: classpathEntries) {
			if (entry.length() >= 5) {
				String isJar = entry.substring(entry.length()-4, entry.length());
				if (".jar".equals(isJar)) {
					int lastPath = entry.lastIndexOf(File.separator);
					String jarName = entry.substring(lastPath + 1, entry.length());
					if (!ignoreList.contains(jarName)) {
						cpJarEntries.add(entry);
					}
				} else {
					cpDirEntries.add(entry);
				}
			} else {
				cpDirEntries.add(entry);
			}
		}
		ZipUtils unzip = new ZipUtils();
		for (String zipName: cpJarEntries) {
			if (log.isInfoEnabled()) {
				log.info("" + this.getClass().getName() + " says extracting jar \n"+
						zipName + " to \n" + explodeTemp.getAbsolutePath());
			}
			
			unzip.unzip(new File(zipName), explodeTemp);
		}
	}

	public void addClasses(Collection<String> packageNames) {
		for (String pkg: packageNames) {
			addPackageClasses(pkg);
		}
	}
	
	private void addPackageClasses(String packageName) {
		String path = explodeTemp.getAbsoluteFile() +
				File.separator + packageToDir(packageName);
		if (log.isInfoEnabled()) {
			log.info("checking for classes in " + path);
		}
		File dir = new File(path);
		
		String [] classes = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.indexOf(".class") != -1) {
					return true;
				}
				return false;
			}
		});
		if (classes == null) {
			if (log.isInfoEnabled()) {
				log.info("the package " + packageName + " has  NO classes.");
			}
			
		} else {
			if (log.isInfoEnabled()) {
				log.info("the package " + packageName + " has " + classes.length + " classes.");
			}
			if (classes.length >= 1) {
				if (log.isDebugEnabled()) {
					log.debug("" + this.getClass().getName() + " says adding the following classes to pacakge \n"
							+ packageName);
					for (int i = 0; i < classes.length; i++) {
						log.debug("" + classes[i]);
					}
				}
				List<String> classesList = Arrays.asList(classes);	
				List<String> classesInPackage = packageClassMap.get(packageName);
				if (classesInPackage == null) {
					packageClassMap.put(packageName, classesList);
				} else {
					classesInPackage.addAll(classesList);
				}
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	public List<String> getClassesForPackage(String packageName) throws IOException {
		List<String> toRet = packageClassMap.get(packageName);
		if (toRet == null) {
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(toRet);
	}
	
	private void recursiveDelete(File f) {
		if (f.isDirectory()) {
			File [] files = f.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					recursiveDelete(files[i]);
				}
			}
		}
		f.delete();
	}
  
	public Set<String> getPackages() {
		return packageClassMap.keySet();
	}


	public File getExplodeTemp() {
		return explodeTemp;
	}


	public void setExplodeTemp(File explodeTemp) {
		this.explodeTemp = explodeTemp;
	}


	public List<String> getIgnoreList() {
		return ignoreList;
	}


	public void setIgnoreList(List<String> ignoreList) {
		this.ignoreList = ignoreList;
	}
	
	public static String packageToDir(String basePackage) {
		StringBuilder sb = new StringBuilder();
		char [] bpChars = basePackage.toCharArray();
		for (int i = 0; i < bpChars.length; i++) {
			char c = bpChars[i];
			if (c == '.') {
				sb.append(File.separator);
			} else {
				sb.append(c);
			}
		}
		String baseDir = sb.toString();
		return baseDir;
	}

}
