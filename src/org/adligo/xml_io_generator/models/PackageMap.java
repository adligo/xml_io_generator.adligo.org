package org.adligo.xml_io_generator.models;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io_generator.utils.PackageUtils;

public class PackageMap {
	private static final Log log = LogFactory.getLog(PackageMap.class);
	private Map<String,String> oldToNew =
			new HashMap<String, String>();
	
	public PackageMap(String inDir, String basePackage, String suffix) {
		if (log.isInfoEnabled()) {
			log.info("entering with " + inDir + "," + basePackage + "," + suffix);
		}
		String baseDir = PackageUtils.packageToDir(basePackage);
		String newBasePackage = basePackage;
		if (!StringUtils.isEmpty(suffix)) {
			newBasePackage = basePackage + "_" + suffix;
		}
		if (log.isInfoEnabled()) {
			log.info("adding " + basePackage + " to " + newBasePackage);
		}
		oldToNew.put(basePackage, newBasePackage);
		File base = new File(inDir + File.separator + baseDir);
		
		recurse(base, basePackage, newBasePackage);
	}

	

	private void recurse(File root, String oldBasePackage, String newBasePackage) {
		if (log.isInfoEnabled()) {
			log.info("checking " + root);
		}
		File [] children = root.listFiles();
		for (int i = 0; i < children.length; i++) {
			File dir = children[i];
			String name = dir.getName();
			if (dir.isDirectory()) {
				
				String subOld = oldBasePackage + "." + name;
				String subNew = newBasePackage + "." + name;
				oldToNew.put(subOld, subNew );
				if (log.isInfoEnabled()) {
					log.info("adding " + subOld + " to " + subNew);
				}
				recurse(dir, subOld, subNew);
			}
		}
	}

	public String get(Object key) {
		return oldToNew.get(key);
	}

	public Set<String> keySet() {
		return oldToNew.keySet();
	}

	public Collection<String> values() {
		return oldToNew.values();
	}

	public Set<Entry<String, String>> entrySet() {
		return oldToNew.entrySet();
	}
}
