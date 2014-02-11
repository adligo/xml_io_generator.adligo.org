package org.adligo.xml_io_generator.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenPropertiesConstants {
	/**
	 * true or false, see the field in this class useClassNamesInXml
	 */
	public static final String USE_CLASS_NAMES_IN_XML = "useClassNamesInXml";
	/**
	 * true or false, see the field in this class useFieldNamesInXml
	 */
	public static final String USE_FIELD_NAMES_IN_XML = "useFieldNamesInXml";
	
	/**
	 * client projects may take on responsible for versioning, other wise versions are calculated.
	 */
	public static final String NAMESPACE_VERSION= "namespaceVersion";
	/**
	 * the namespace suffic to add to the classes generated
	 */
	public static final String NAMESPACE_SUFFIX = "namespaceSuffix";
	/**
	 * where to put the generated source classes
	 */
	public static final String OUTPUT_DIRECTORY = "outputDirectory";
	/**
	 * a comma delimited list of classes to ignore
	 */
	public static final String IGNORE_CLASS_LIST = "ignoreClassList";
	/**
	 * a comma delimited list of text that will cause a class to be ignored
	 */
	public static final String IGNORE_CLASSES_CONTAINING = "ignoreClassesContaining";
	/**
	 * jars to ignore (not unzip into the xml_io_generator_temp for source code parsing)
	 */
	public static final String IGNORE_JAR_LIST = "ignoreJarList";
	
	public static final String BASE_PACKAGE = "basePackage";
	
	public static final List<String> KEYS = getKeys() ;
	
	private static List<String> getKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add(OUTPUT_DIRECTORY);
		keys.add(IGNORE_CLASS_LIST);
		keys.add(IGNORE_CLASSES_CONTAINING);
		keys.add(IGNORE_JAR_LIST);
		keys.add(BASE_PACKAGE);
		keys.add(USE_CLASS_NAMES_IN_XML);
		keys.add(USE_FIELD_NAMES_IN_XML);
		keys.add(NAMESPACE_SUFFIX);
		return Collections.unmodifiableList(keys);
	}
}
