package org.adligo.xml_io_generator.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;

public class GenProperties {
	private static final Log log = LogFactory.getLog(GenProperties.class);
	
	public static Properties loadGenProperties(String runningDir) throws IOException {
		Properties props = new Properties();
		FileInputStream fis = null;
		File dir = new File(runningDir);
		
		if (!dir.isDirectory()) {
			throw new IOException("the dir " + dir + " is not a directory, and must be passed in as arg[0]"
					+ " or baseDir if being called from the command line");
		}
		
		String propsFileDir = runningDir + File.separator + "gen.properties";
		
		
		if (log.isWarnEnabled()) {
			log.warn("attempting read of " + propsFileDir);
		}
		try {
			fis = new FileInputStream(new File(propsFileDir));
			props.load(fis);
			for (String key: GenPropertiesConstants.KEYS) {
				log.warn(key + " is " + props.getProperty(key));
			}
		} catch (FileNotFoundException e) {
			throw new IOException("SouceCodeGenerator was not able to find the property file " + runningDir +
					" at " + propsFileDir,e);
		} catch (IOException e) {
			throw new IOException("SouceCodeGenerator was not able to find the property file " + runningDir +
					" at " + propsFileDir,e);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return props;
	}
}
