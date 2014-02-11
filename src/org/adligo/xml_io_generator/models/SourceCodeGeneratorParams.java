package org.adligo.xml_io_generator.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.StringUtils;
import org.adligo.xml_io_generator.utils.ModelDiscovery;
import org.adligo.xml_io_generator.utils.PackageUtils;

public class SourceCodeGeneratorParams {
	private static final Log log = LogFactory.getLog(SourceCodeGeneratorParams.class);
	

	
	private Map<String, List<Class<?>>> packageClassModels = new HashMap<String, List<Class<?>>>();
	private List<String> ignoreClassList = new ArrayList<String>();
	private List<String> ignoreClassesContainingList = new ArrayList<String>();
	private List<String> ignoreJarList = new ArrayList<String>();
	private String version;
	private String namespaceSuffix;
	private String basePackage;
	
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
		String val = props.getProperty(GenPropertiesConstants.USE_CLASS_NAMES_IN_XML);
		if (val != null) {
			if ("false".equalsIgnoreCase(val)) {
				useClassNamesInXml = false;
			}
		}
		
		val = props.getProperty(GenPropertiesConstants.USE_FIELD_NAMES_IN_XML);
		if (val != null) {
			if ("false".equalsIgnoreCase(val)) {
				useFieldNamesInXml = false;
			}
		}
		
		val = props.getProperty(GenPropertiesConstants.NAMESPACE_VERSION);
		if (!StringUtils.isEmpty(val)) {
			version = val;
		}
	
		val = props.getProperty(GenPropertiesConstants.NAMESPACE_SUFFIX);
		if (!StringUtils.isEmpty(val)) {
			namespaceSuffix = val;
		}
		
		val = props.getProperty(GenPropertiesConstants.OUTPUT_DIRECTORY);
		if (val == null) {
			throw new IllegalArgumentException("The SourceCodeGenrator requires a " + 
					GenPropertiesConstants.OUTPUT_DIRECTORY);
		}
		outputDirectory = val;
		
		val = props.getProperty(GenPropertiesConstants.IGNORE_CLASS_LIST);
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
		
		val = props.getProperty(GenPropertiesConstants.IGNORE_CLASSES_CONTAINING);
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
		
		
		val = props.getProperty(GenPropertiesConstants.IGNORE_JAR_LIST);
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
		
		basePackage = props.getProperty(GenPropertiesConstants.BASE_PACKAGE);
		
		PackageUtils pu = new PackageUtils(ignoreJarList);
		discoverClassesInPackage(pu, basePackage);
	}

	private void discoverClassesInPackage(PackageUtils pu, String packageName)
			throws IOException, ClassNotFoundException {
		if (pu.hasSubPackages(packageName)) {
			List<String> subPackages = pu.getSubPackages(packageName);
			for (String pkg: subPackages) {
				discoverClassesInPackageRecursive(pu, pkg);
			}
		}
		discoverClassesInSimplePackage(pu, packageName);
	}
	
	private void discoverClassesInPackageRecursive(PackageUtils pu, String packageName)
			throws IOException, ClassNotFoundException {
		if (pu.hasSubPackages(packageName)) {
			List<String> subPackages = pu.getSubPackages(packageName);
			for (String pkg: subPackages) {
				discoverClassesInPackageRecursive(pu, pkg);
			}
		}
		discoverClassesInSimplePackage(pu, packageName);
	}
	
	private void discoverClassesInSimplePackage(PackageUtils pu, String packageName)
			throws IOException, ClassNotFoundException {
		ModelDiscovery md = new ModelDiscovery(pu, packageName);
		
		List<Class<?>> classes = md.getModels();
		if (log.isInfoEnabled()) {
			log.info("there are " + classes.size() + " classes in package " + packageName);
		}
		for (Class<?> mod: classes) {
			if (log.isDebugEnabled()) {
				log.debug("checking class " +mod);
			}
			String name = mod.getName();
			boolean ignore = false;
			if (!ignoreClassList.contains(name)) {
				for (String contain: ignoreClassesContainingList) {
					if (name.indexOf(contain) != -1) {
						ignore = true;
					}
				}
				if (!ignore) {
					if (log.isDebugEnabled()) {
						log.debug("adding class " +mod);
					}
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

	public String getNamespaceSuffix() {
		return namespaceSuffix;
	}
	
}
