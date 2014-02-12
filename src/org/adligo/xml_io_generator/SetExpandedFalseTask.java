package org.adligo.xml_io_generator;

import java.io.FileInputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.StringUtils;
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
	private String firstBuild;
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

	public String getFirstBuild() {
		return firstBuild;
	}

	public void setFirstBuild(String firstBuild) {
		this.firstBuild = firstBuild;
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
			if (StringUtils.isEmpty(libRoot)) {
				throw new IllegalStateException(DIR_HAS_NOT_BEEN_SET);
			}
			if ("true".equals(firstBuild) || "true".equals(standAlone)) {
				
				Properties props = new Properties();
				FileInputStream fis = new FileInputStream(project.getProperty("user.home") + 
						"/conf/adligo/adligo_jse_lib.properties");
				props.load(fis);
				fis.close();
				
				ManifestParser mp = new ManifestParser();
				super.log("Adligo Set Expanded False Ant Task");
				String version = mp.get(ManifestParser.IMPLEMENTATION_VERSION);
				log("Version: " + version);
				AntCommonInit.initOrReload("adligo_log.properties", 
						"Set Expanded False starting ", this);
				
				StringBuilder sbOut = new StringBuilder();
				Set<Entry<Object,Object>> entries = props.entrySet();
				for (Entry<Object,Object> e: entries) {
					String key = (String) e.getKey();
					String value = (String) e.getValue();
					if (key.indexOf("expanded_jar_") == 0) {
						sbOut.append(key);
						sbOut.append("=false");
					} else {
						sbOut.append(key);
						sbOut.append("=");
						sbOut.append(value);
					}
				}
			}
			project.setUserProperty(success, "true");
		} catch (Exception x) {
			project.setUserProperty(success, "false");
			log.error(x.getMessage(), x);
			throw new BuildException(x);
		}
	}
	
	
}
