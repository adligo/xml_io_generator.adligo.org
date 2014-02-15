package org.adligo.xml_io_generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.i.util.shared.StringUtils;
import org.adligo.xml_io_generator.models.GenProperties;
import org.adligo.xml_io_generator.utils.ManifestParser;
import org.adligo.xml_io_generator.utils.PackageUtils;
import org.adligo.xml_io_generator.utils.ZipUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class ExpandClasspathTask extends Task {
	public static final String CLASSPATH_WAS_NOT_SET = "Classpath was not set";
	private static final Log log = LogFactory.getLog(ExpandClasspathTask.class);
	public static final String PROJECT_HAS_NOT_BEEN_SET = "Project has not been set.";
	private String libRoot;
	private String classpath;
	private String clean;
	private String standAlone;
	private String success;
	
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

	public String getClean() {
		return clean;
	}

	public void setClean(String firstBuild) {
		this.clean = firstBuild;
	}

	public String getStandAlone() {
		return standAlone;
	}

	public void setStandAlone(String standAlone) {
		this.standAlone = standAlone;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
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
			String classpathFromAnt = project.getProperty(classpath);
			
			List<String> classpathEntries = new ArrayList<String>();
			if (classpathFromAnt.indexOf(";") == -1) {
				classpathEntries.add(classpathFromAnt);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(classpathFromAnt, ";");
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
			if ("true".equalsIgnoreCase(clean)) {
				deleteAll();
			} else if ("true".equalsIgnoreCase(standAlone)) {
				deleteBasePackageExtract(project);
			}
			for (String jarOrDir: classpathEntries) {
				expandOrCopyfilesToXmlIoExp(props, jarOrDir);
			}
			FileOutputStream fos = new FileOutputStream(libRoot + File.separator + 
					"adligo_jse_lib.properties");
			props.store(fos, "Expanded several jars which don't need to "
					+ "be expaneded again until a full jse-main-build.xml");
			fos.close();
			project.setUserProperty(success, "true");
		} catch (Exception x) {
			project.setUserProperty(success, "false");
			log.error(x.getMessage(), x);
		}
	}

	private void expandOrCopyfilesToXmlIoExp(Properties props, String jarOrDir) {
		int dot = jarOrDir.indexOf(".");
		if (dot == -1) {
			//its a dir so recursive copy
			log.warn("Copy Dir currently not implemented didn't expand " + jarOrDir, 
					new Exception("Exception for log importance only, Non Fatal."));
		} else {
			if (log.isInfoEnabled()) {
				log.info("checking if expansion is needed for " + jarOrDir);
			}
			char [] chars = jarOrDir.toCharArray();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c == File.separatorChar) {
					sb = new StringBuilder();
				} else if (c == '.') {
					break;
				} else {
					sb.append(c);
				}
			}
			String justNamePartOfJar = sb.toString();
			
			String key = "expanded_jar_" + justNamePartOfJar;
			String tf = (String) props.get(key);
			if (!"true".equals(tf)) {
				ZipUtils zu = new ZipUtils();
				File in = new File(jarOrDir);
				File out = new File(libRoot + File.separator + "xml-io-exp");
				if (log.isWarnEnabled()) {
					log.warn("expanding " + jarOrDir);
					log.warn("to " + libRoot + File.separator + "xml-io-exp");
				}
				zu.unzip(in, out);
				props.setProperty(key, "true");
			}
		}
	}

	private void deleteBasePackageExtract(Project project) throws IOException {
		File baseDir = project.getBaseDir();
		Properties genProps = GenProperties.loadGenProperties(baseDir.getAbsolutePath());
		String basePackage = genProps.getProperty("basePackage");
		
		String pkg = PackageUtils.packageToDir(basePackage);
		String myClasses = libRoot + File.separator + 
				"xml-io-exp" + File.separator + 
				pkg;
		if (log.isWarnEnabled()) {
			log.warn("checking " + myClasses);
		}
		File myF = new File(myClasses);
		if (myF != null) {
			if (log.isWarnEnabled()) {
				log.warn("deleting " + myF);
			}
			recursiveDelete(myF);
		}
	}

	private void deleteAll() throws IOException{
		File [] subs = new File(libRoot + File.separator + 
				"xml-io-exp").listFiles();
		if (subs != null) {
			for (int i = 0; i < subs.length; i++) {
				File file = subs[i];
				if (log.isInfoEnabled()) {
					log.info("deleting " + file);
				}
				recursiveDelete(file);
			}
		}
	}
	
	

	private void recursiveDelete(File base) throws IOException {
		File [] children = base.listFiles();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				recursiveDelete(children[i]);
			}
		}
		if (base.exists()) {
			if (!base.delete()) {
				throw new IOException("Failed to delete " + base);
			}
		}
	}
	
}
