package org.adligo.xml_io_generator;

import java.util.List;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io_generator.models.SourceCodeGeneratorParams;
import org.adligo.xml_io_generator.utils.ManifestParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class SourceCodeGeneratorTask extends Task {
	public static final String UNKNOWN_LIB_ROOT_WITH_STAND_ALONE_TRUE = "Unknown libRoot with standAlone!=true.";
	public static final String SUCCESS_PROPERTY_WAS_NOT_SET = "Success Property was not set";
	public static final String CLASSPATH_WAS_NOT_SET = "Classpath was not set";
	private static final Log log = LogFactory.getLog(SourceCodeGeneratorTask.class);
	public static final String PROJECT_HAS_NOT_BEEN_SET = "Project has not been set.";
	private String classpath;
	private String success;
	/**
	 * the lib_root from the ant build
	 * usually ${user.home}/conf/adligo
	 */
	private String libRoot;
	/**
	 * if running the build.xml file for a project outside of
	 * jse-main-build.xml
	 */
	private String standAlone;
	

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String p) {
		this.success = p;
	}

	public String getLibRoot() {
		return libRoot;
	}

	public void setLibRoot(String libRoot) {
		this.libRoot = libRoot;
	}

	public String getStandAlone() {
		return standAlone;
	}

	public void setStandAlone(String standAlone) {
		this.standAlone = standAlone;
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
			if (StringUtils.isEmpty(success)) {
				throw new IllegalStateException(SUCCESS_PROPERTY_WAS_NOT_SET);
			}
			
			
			ManifestParser mp = new ManifestParser();
			mp.readManifest(SourceCodeGeneratorTask.class);
			super.log("Adligo Source Code Generator Ant Task");
			String version = mp.get(ManifestParser.IMPLEMENTATION_VERSION);
			log("Version: " + version);
			AntCommonInit.initOrReload("adligo_log.properties", 
					"Source Code Generator starting ", this);
			
			String classpathEntries = project.getUserProperty(classpath);
			if (StringUtils.isEmpty(classpathEntries)) {
				classpathEntries = project.getProperty(classpath);
			}
			List<String> classpathList = SourceCodeGenerator.toList(classpathEntries);
			SourceCodeGeneratorParams params = new SourceCodeGeneratorParams();
			String dir = project.getBaseDir().getAbsolutePath();
			params.setPath(dir);
			params.setClasspath(classpathList);
			if ("true".equalsIgnoreCase(standAlone)) {
				params.setStandAlone(true);
			} else {
				if (StringUtils.isEmpty(libRoot)) {
					throw new Exception(UNKNOWN_LIB_ROOT_WITH_STAND_ALONE_TRUE);
				}
			}
			params.setLibRoot(libRoot);
			SourceCodeGenerator.run(params);
			
			project.setUserProperty(success, "true");
		} catch (Exception x) {
			project.setUserProperty(success, "false");
			log.error(x.getMessage(), x);
			throw new BuildException(x);
		}
	}
	
	
}
