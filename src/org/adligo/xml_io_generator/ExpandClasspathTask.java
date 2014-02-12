package org.adligo.xml_io_generator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io_generator.models.SourceCodeGeneratorParams;
import org.adligo.xml_io_generator.utils.ManifestParser;
import org.adligo.xml_io_generator.utils.ZipUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import sun.util.locale.StringTokenIterator;

public class ExpandClasspathTask extends Task {
	public static final String CLASSPATH_WAS_NOT_SET = "Classpath was not set";
	private static final Log log = LogFactory.getLog(ExpandClasspathTask.class);
	public static final String PROJECT_HAS_NOT_BEEN_SET = "Project has not been set.";
	private String libRoot;
	private String classpath;
	
	public String getLibRoot() {
		return libRoot;
	}

	public void setLibRoot(String dir) {
		this.libRoot = dir;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	@Override
	public void execute() throws BuildException {
		
		Project project = getProject();
		if (project == null) {
            throw new IllegalStateException(PROJECT_HAS_NOT_BEEN_SET);
        }
		try {
			if (StringUtils.isEmpty(classpath)) {
				throw new IllegalStateException(CLASSPATH_WAS_NOT_SET);
			}
			
			Properties props = new Properties();
			FileInputStream fis = new FileInputStream(libRoot + File.separator +
					"adligo_jse_lib.properties");
			props.load(fis);
			fis.close();
			
			List<String> classpathEntries = new ArrayList<String>();
			if (classpath.indexOf(";") == -1) {
				classpathEntries.add(classpath);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(classpath, ";");
				while (tokenizer.hasMoreElements()) {
					classpathEntries.add(tokenizer.nextToken());
				}
			}
			ManifestParser mp = new ManifestParser();
			mp.readManifest(ExpandClasspathTask.class);
			super.log("Adligo Expand Classpath Ant Task");
			String version = mp.get(ManifestParser.IMPLEMENTATION_VERSION);
			log("Version: " + version);
			AntCommonInit.initOrReload("adligo_log.properties", 
					"Expanding Jars starting ", this);
			
			for (String jarOrDir: classpathEntries) {
				int dot = jarOrDir.indexOf(".");
				if (dot == -1) {
					//its a dir so recursive copy
					log.warn("Copy Dir currently not implemented", 
							new Exception("Exception for log importance only, Non Fatal."));
				} else {
					int lastSlash = jarOrDir.lastIndexOf(File.separator);
					String justNamePartOfJar = jarOrDir.substring(
							lastSlash, jarOrDir.length() - lastSlash - 4);
					String key = "expanded_jar_" + justNamePartOfJar;
					String tf = (String) props.get(key);
					if (!"true".equals(tf)) {
						ZipUtils zu = new ZipUtils();
						File in = new File(jarOrDir);
						File out = new File(libRoot + File.separator + "xml-io-exp");
						zu.unzip(in, out);
					}
				}
			}
			
		} catch (Exception x) {
			log.error(x.getMessage(), x);
			throw new BuildException(x);
		}
	}
	
	
}
