package org.adligo.xml_io.generator.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;


public class PackageUtils {
	private static final Log log = LogFactory.getLog(PackageUtils.class);
	
	private List<String> cpJarEntries = new ArrayList<String>();
	private List<String> cpDirEntries = new ArrayList<String>();
	/**
	 * map of package names to class names
	 */
	private Map<String,List<String>> packageClassMap= new HashMap<String, List<String>>();
	private File explodeTemp;
	private List<String> ignoreList = new ArrayList<String>();
	
	public PackageUtils() {
		this( null);
	}

	public PackageUtils( List<String> p) {
		if (p != null) {
			ignoreList.addAll(p);
		}
		explodeTemp = new File("xml_io_generator_temp");
		if (explodeTemp.exists()) {
			recursiveDelete(explodeTemp);
		}
		explodeTemp.mkdir();
		
		Properties props = System.getProperties();
		Set<Object> keys = props.keySet();
		for (Object key: keys) {
			if (log.isDebugEnabled()) {
				log.debug("" + this.getClass().getName() + " says " + key + "=" + System.getProperty(key.toString()));
			}
		}
		
		String classpath = System.getProperty("java.class.path");
		System.out.println("" + this.getClass().getName() + " says yes classpath is " + classpath);
		StringTokenizer st = new StringTokenizer(classpath, ":");
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			String isJar = token.substring(token.length()-4, token.length());
			if (".jar".equals(isJar)) {
				int lastPath = token.lastIndexOf(File.separator);
				String jarName = token.substring(lastPath + 1, token.length());
				if (!ignoreList.contains(jarName)) {
					cpJarEntries.add(token);
				}
			} else {
				cpDirEntries.add(token);
			}
		}
		for (String dirName: cpDirEntries) {
			addPackageClassNames(dirName, dirName, "");
		}
		ZipUtils unzip = new ZipUtils();
		for (String zipName: cpJarEntries) {
			if (log.isDebugEnabled()) {
				log.debug("" + this.getClass().getName() + " says extracting jar \n"+
						zipName + " to \n" + explodeTemp.getAbsolutePath());
			}
			
			unzip.unzip(new File(zipName), explodeTemp);
		}
		String explodName = explodeTemp.getAbsolutePath();
		addPackageClassNames(explodName, explodName, "");
	}
	

	private void addPackageClassNames(final String rootDirName, final String dirName, final String packageName) {
		File dir = new File(dirName);
		String [] classes = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.indexOf(".class") != -1) {
					return true;
				}
				return false;
			}
		});
		if (classes != null) {
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

		//recurse
		File [] childDirs = dir.listFiles();
		if (childDirs == null) {
			return;
		}
		for (int i = 0; i < childDirs.length; i++) {
			File thisDir = childDirs[i];
			if (!thisDir.isDirectory()) {
				//skip loop block
			} else {
				String subName = thisDir.getName();
				if (log.isDebugEnabled()) {
					log.debug("" + this.getClass().getName() + " says checking directory " + subName);
				}
				String newPackageName = "";
				if (packageName.length() == 0) {
					newPackageName = subName;
				} else {
					newPackageName = packageName + "." + subName;
				}
				String newDirName = dirName + File.separator + subName;
				
				if (log.isDebugEnabled()) {
					log.debug("" + this.getClass().getName() + " says package " +newPackageName  + 
							" is under directory" + rootDirName);
				}
				addPackageClassNames(rootDirName, newDirName, newPackageName);
			}
		}
	}
	
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
}
