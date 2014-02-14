package org.adligo.xml_io_generator.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;

public class LibProperties {
	private static final Log log = LogFactory.getLog(LibProperties.class);
	
	public static void storeProperties(String libRoot, 
			String comment, Properties props) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(new File(libRoot + File.separator +
				"adligo_jse_lib.properties"));
		props.store(fos,comment);
		fos.close();
	}
	public static Properties loadProperties(String libRoot) throws IOException {
		Properties props = new Properties();
		FileInputStream fis = null;
		File dir = new File(libRoot);
		
		if (!dir.isDirectory()) {
			throw new IOException("the dir " + dir + " is not a directory, and must be passed in as arg[0]"
					+ " or libRoot if being called from the command line");
		}
		
		String propsFile = dir + File.separator + "adligo_jse_lib.properties";
		
		
		if (log.isWarnEnabled()) {
			log.warn("attempting read of " + propsFile);
		}
		try {
			fis = new FileInputStream(new File(propsFile));
			props.load(fis);
		} catch (FileNotFoundException e) {
			throw new IOException("Not able to find the property file " + dir +
					" at " + propsFile,e);
		} catch (IOException e) {
			throw new IOException("Not able to find the property file " + dir +
					" at " + propsFile,e);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return props;
	}
}
