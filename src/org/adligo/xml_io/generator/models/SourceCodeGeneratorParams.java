package org.adligo.xml_io.generator.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io.generator.utils.ModelDiscovery;
import org.adligo.xml_io.generator.utils.PackageUtils;

public class SourceCodeGeneratorParams {
	/**
	 * true or false, see the field in this class useClassNamesInXml
	 */
	public static final String USE_CLASS_NAMES_IN_XML_PROPERTY = "useClassNamesInXml";
	/**
	 * true or false, see the field in this class useFieldNamesInXml
	 */
	public static final String USE_FIELD_NAMES_IN_XML_PROPERTY = "useFieldNamesInXml";
	/**
	 * package list 
	 */
	public static final String PACKAGE_LIST_PROPERTY = "packageList";
	
	/**
	 * client projects are responsible for versioning.
	 */
	public static final String NAMESPACE_VERSION_PROPERTY = "namespaceVersion";
	/**
	 * where to put the generated source classes
	 */
	public static final String OUTPUT_DIRECTORY_PROPERTY = "outputDirectory";
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
	
	private Map<String, List<Class<?>>> packageClassModels = new HashMap<String, List<Class<?>>>();
	private List<String> ignoreClassList = new ArrayList<String>();
	private List<String> ignoreClassesContainingList = new ArrayList<String>();
	private List<String> ignoreJarList = new ArrayList<String>();
	private String version;
	
	private String outputDirectory;
	/**
	 * if true the class simple name will be used in the xml/schmea ie DomainName
	 * otherwise the LetterCounter will use something like a or aa
	 */
	private boolean useClassNamesInXml = true;
	/**
	 * if true the field name will be used in the xml/schmea ie id
	 * otherwise the LetterCounter will use something like a or aa
	 */
	private boolean useFieldNamesInXml = true;
	
	public SourceCodeGeneratorParams() {
		
	}
	
	public SourceCodeGeneratorParams(Properties props) throws IOException, ClassNotFoundException {
		String val = props.getProperty(USE_CLASS_NAMES_IN_XML_PROPERTY);
		if (val != null) {
			if ("false".equalsIgnoreCase(val)) {
				useClassNamesInXml = false;
			}
		}
		
		val = props.getProperty(USE_FIELD_NAMES_IN_XML_PROPERTY);
		if (val != null) {
			if ("false".equalsIgnoreCase(val)) {
				useFieldNamesInXml = false;
			}
		}
		
		val = props.getProperty(NAMESPACE_VERSION_PROPERTY);
		if (!StringUtils.isEmpty(val)) {
			version = val;
		}
		
		val = props.getProperty(OUTPUT_DIRECTORY_PROPERTY);
		if (val == null) {
			throw new IllegalArgumentException("The SourceCodeGenrator requires a " + OUTPUT_DIRECTORY_PROPERTY);
		}
		outputDirectory = val;
		
		val = props.getProperty(IGNORE_CLASS_LIST);
		if (val != null) {
			if (val.indexOf(",") == -1) {
				ignoreClassList.add(val);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(val, ",");
				while (tokenizer.hasMoreElements()) {
					String name = tokenizer.nextToken();
					ignoreClassList.add(name);
				}
			}
		}
		
		val = props.getProperty(IGNORE_CLASSES_CONTAINING);
		if (val != null) {
			if (val.indexOf(",") == -1) {
				ignoreClassesContainingList.add(val);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(val, ",");
				while (tokenizer.hasMoreElements()) {
					String name = tokenizer.nextToken();
					ignoreClassesContainingList.add(name);
				}
			}
		}
		
		
		val = props.getProperty(IGNORE_JAR_LIST);
		if (val != null) {
			if (val.indexOf(",") == -1) {
				ignoreJarList.add(val);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(val, ",");
				while (tokenizer.hasMoreElements()) {
					String name = tokenizer.nextToken();
					ignoreJarList.add(name);
				}
			}
		}
		
		val = props.getProperty(PACKAGE_LIST_PROPERTY);
		if (val == null) {
			throw new IllegalArgumentException("The property " + PACKAGE_LIST_PROPERTY + " is required");
		}
		PackageUtils pu = new PackageUtils(true, ignoreJarList);
		if (val.indexOf(",") == -1) {
			discoverClassesInPackage(pu, val);
		} else {
			StringTokenizer tokenizer = new StringTokenizer(val, ",");
			while (tokenizer.hasMoreElements()) {
				String packageName = tokenizer.nextToken();
				discoverClassesInPackage(pu, packageName);
			}
		}
	}

	private void discoverClassesInPackage(PackageUtils pu, String packageName)
			throws IOException, ClassNotFoundException {
		ModelDiscovery md = new ModelDiscovery(pu, packageName);
		List<Class<?>> classes = md.getModels();
		for (Class<?> mod: classes) {
			String name = mod.getName();
			boolean ignore = false;
			if (!ignoreClassList.contains(name)) {
				for (String contain: ignoreClassesContainingList) {
					if (name.indexOf(contain) != -1) {
						ignore = true;
					}
				}
				if (!ignore) {
					addClass(mod);
				}
			}
		}
	}
	
	public void addClass(Class<?> clazz) {
		Package pkg = clazz.getPackage();
		String name = pkg.getName();
		List<Class<?>> classes = packageClassModels.get(name);
		if (classes == null) {
			classes = new ArrayList<Class<?>>();
			packageClassModels.put(name, classes);
		}
		classes.add(clazz);
	}
	
	public Set<String> getPackages() {
		return packageClassModels.keySet();
	}
	
	public List<Class<?>> getClasses(String packageName) {
		return packageClassModels.get(packageName);
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public boolean isUseClassNamesInXml() {
		return useClassNamesInXml;
	}

	public void setUseClassNamesInXml(boolean useClassNamesInXml) {
		this.useClassNamesInXml = useClassNamesInXml;
	}

	public boolean isUseFieldNamesInXml() {
		return useFieldNamesInXml;
	}

	public void setUseFieldNamesInXml(boolean useFieldNamesInXml) {
		this.useFieldNamesInXml = useFieldNamesInXml;
	}

	public List<String> getIgnoreClassesContainingList() {
		return ignoreClassesContainingList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
