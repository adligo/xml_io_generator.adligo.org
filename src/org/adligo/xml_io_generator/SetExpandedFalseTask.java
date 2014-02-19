package org.adligo.xml_io_generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.i.util.shared.StringUtils;
import org.adligo.xml_io_generator.models.GenProperties;
import org.adligo.xml_io_generator.models.LibProperties;
import org.adligo.xml_io_generator.utils.ManifestParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class SetExpandedFalseTask extends Task {
	public static final String DIR_HAS_NOT_BEEN_SET = "Dir has not been set.";
	public static final String CLASSPATH_WAS_NOT_SET = "Classpath was not set";
	private static final Log log = LogFactory.getLog(SetExpandedFalseTask.class);
	public static final String PROJECT_HAS_NOT_BEEN_SET = "Project has not been set.";
	/**
	 * the lib
	 */
	private String libRoot;
	private String standAlone;
	private String clean;
	private String success;
	
	public String getLibRoot() {
		return libRoot;
	}

	public void setLibRoot(String dir) {
		this.libRoot = dir;
	}

	public String getStandAlone() {
		return standAlone;
	}

	public void setStandAlone(String standAlone) {
		this.standAlone = standAlone;
	}

	public String getClean() {
		return clean;
	}

	public void setClean(String firstBuild) {
		this.clean = firstBuild;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	@Override
	public void execute() throws BuildException {
		ManifestParser mp = new ManifestParser();
		super.log("Adligo Set Expanded False Ant Task");
		String version = mp.get(ManifestParser.IMPLEMENTATION_VERSION);
		log("Version: " + version);
		AntCommonInit.initOrReload("adligo_log.properties", 
				"Set Expanded False starting ", this);
		
		Project project = getProject();
		if (project == null) {
            throw new IllegalStateException(PROJECT_HAS_NOT_BEEN_SET);
        }
		try {
			if (StringUtils.isEmpty(libRoot)) {
				throw new IllegalStateException(DIR_HAS_NOT_BEEN_SET);
			}
			
			
			if ("true".equals(clean)) {
				setAllExpandedFalse(project);
			} else if ("true".equals(standAlone)) {
				setBasePackageExpandedFalse(project);
			}
			project.setUserProperty(success, "true");
		} catch (Exception x) {
			project.setUserProperty(success, "false");
			log.error(x.getMessage(), x);
		}
	}

	private void setAllExpandedFalse(Project project)
			throws FileNotFoundException, IOException {
		
		Properties props = LibProperties.loadProperties(libRoot);
		
		
		Set<Entry<Object,Object>> entries = props.entrySet();
		for (Entry<Object,Object> e: entries) {
			String key = (String) e.getKey();
			if (key.indexOf("expanded_jar_") == 0) {
				props.setProperty(key, "false");
			} 
		}
		LibProperties.storeProperties(libRoot, 
				"Set all expanded to false", props);
	}
	
	private void setBasePackageExpandedFalse(Project project)
			throws FileNotFoundException, IOException {

		Properties props = LibProperties.loadProperties(libRoot);
		File baseDir = project.getBaseDir();
		Properties genProps = GenProperties.loadGenProperties(baseDir.getAbsolutePath());
		
		String basePackage = (String) genProps.getProperty("basePackage");
		String baseProjectName = (String) props.get(basePackage);
		
		props.setProperty("expanded_jar_" + baseProjectName, "false");
		
		LibProperties.storeProperties(libRoot, "Set all expanded_jar_" + 
					baseProjectName + " to false", props);
	}
	
	
}
