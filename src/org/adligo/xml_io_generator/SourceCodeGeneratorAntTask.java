package org.adligo.xml_io_generator;

import org.adligo.ant_log.AntCommonInit;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.xml_io_generator.utils.ManifestParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class SourceCodeGeneratorAntTask extends Task {
	private static final Log log = LogFactory.getLog(SourceCodeGeneratorAntTask.class);
	public static final String PROJECT_HAS_NOT_BEEN_SET = "Project has not been set.";
	private String dir;
	private String classpath;
	
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	@Override
	public void execute() throws BuildException {
		try {
			Project project = getProject();
			if (project == null) {
	            throw new IllegalStateException(PROJECT_HAS_NOT_BEEN_SET);
	        }
			ManifestParser mp = new ManifestParser();
			mp.readManifest(SourceCodeGeneratorAntTask.class);
			super.log("Adligo Source Code Generator Ant Task");
			String version = mp.get(ManifestParser.IMPLEMENTATION_VERSION);
			log("Version: " + version);
			AntCommonInit.initOrReload("adligo_log.properties", 
					"Source Code Generator starting ", this);
			String classpathList = project.getUserProperty(classpath);
			SourceCodeGenerator.main(new String[] {dir, classpathList});
		} catch (Exception x) {
			log.error(x.getMessage(), x);
			throw new BuildException(x);
		}
	}
	
	
}
