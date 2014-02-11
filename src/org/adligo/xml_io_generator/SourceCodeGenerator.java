package org.adligo.xml_io_generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.log.client.LogPlatform;
import org.adligo.i.util.client.StringUtils;
import org.adligo.jse.util.JSEPlatform;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.GenPropertiesConstants;
import org.adligo.xml_io_generator.models.GeneratorContext;
import org.adligo.xml_io_generator.models.Namespace;
import org.adligo.xml_io_generator.models.SourceCodeGeneratorParams;

public class SourceCodeGenerator {
	private static final Log log = LogFactory.getLog(SourceCodeGenerator.class);
	
	public static final String THERE_WAS_A_ERROR_CREATING_THE_DIRECTORY = "There was a error creating the directory ";
	public static final String THE_SOURCE_CODE_GENERATOR_REQUIRES_EITHER_A_MUTANT_OR_SINGLE_FIELD_W_CONSTRUCTOR_MODELS = 
		"The SourceCodeGenerator requires either a Mutant or A class with constructor parmeter for souce code generation.";

	public static void main(String [] args) throws Exception {
		System.out.println("SourceCodeGenerator running" );
		
		String path = args[0];
		if (StringUtils.isEmpty(path)) {
			path = new File(".").getAbsolutePath();
		} 
		System.out.println("SourceCodeGenerator path is " + path );
		if (args.length >= 2) {
			String classpath = args[1];
			StringTokenizer tokenizer = new StringTokenizer(classpath, ",");
			List<URL> cpList = new ArrayList<URL>();
			while (tokenizer.hasMoreElements()) {
				String token = tokenizer.nextToken();
				cpList.add(new URL(token));
			}
			new URLClassLoader(cpList.toArray(new URL[cpList.size()]));
		}
		
		File runningDir = new File(path);
		
		if (!runningDir.isDirectory()) {
			System.out.println("The first argument passed in must be a directory.");
			log.error("The first argument passed in must be a directory.");
			return;
		}
		JSEPlatform.init();
		if (!LogPlatform.isInit()) {
			LogPlatform.init(path + File.separator + "adligo_log.properties");
		}
		System.out.println("running in " + path);
		
		System.err.println("reading  " + path + File.separator + "gen.properties");
		File propsFile = new File(path + File.separator + "gen.properties");

		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(propsFile);
			props.load(fis);
			for (String key: GenPropertiesConstants.KEYS) {
				log.warn(key + " is " + props.getProperty(key));
			}
			
			log.warn("starting souce code generation");
			SourceCodeGeneratorParams params = new SourceCodeGeneratorParams(props);
			generate(params);
		} catch (FileNotFoundException e) {
			System.err.println("SouceCodeGenerator was not able to find the property file " + args[0] +
					" at " + propsFile.getAbsolutePath());
			e.printStackTrace();
			log.error(e.getMessage(), e);
			return;
		} catch (IOException e) {
			System.err.println("SouceCodeGenerator had a problem loading the the property file " + args[0] +
					" at " + propsFile.getAbsolutePath());
			e.printStackTrace();
			log.error(e.getMessage(), e);
			return;
		} catch (Exception x) {
			x.printStackTrace();
			log.error(x.getMessage(), x);
			return;
		}
		
		
	}
	public static void generate(SourceCodeGeneratorParams params) throws IOException {
		String dir = params.getOutputDirectory();
		String suffix = params.getNamespaceSuffix();
		makeRootDir(dir);
		
		
		Set<String> packages = params.getPackages();
		if (log.isInfoEnabled()) {
			log.info("there are " + packages.size() + " packages");
		}
		for (String name : packages) {
			if (log.isInfoEnabled()) {
				log.info("working on package " + name);
			}
			GeneratorContext ctx = new GeneratorContext();
			String pkgDir = makePackageDirectories(name, dir, suffix);
			ctx.setPackageDirectory(pkgDir);
			ctx.setPackageName(name);
			ctx.setPackageSuffix(suffix);
			
			String version = params.getVersion();
			if (!StringUtils.isEmpty(version)) {
				ctx.setPackageVersion(version);
			}
			
			ctx.setParams(params);
			String namespace = Namespace.toNamespace(name);
			ctx.setNamespace(namespace);
			
			List<Class<?>> classes = params.getClasses(name);
			for (Class<?> clazz : classes) {
				ctx.clearExtraClassImports();
				//mutants must be done first for complex non mutants
				generateMutant(clazz, ctx);
			}
			for (Class<?> clazz : classes) {
				ctx.clearExtraClassImports();
				generateNonMutant(clazz, ctx);
			}
			SetupGenerator setup = new SetupGenerator();
			setup.generate(ctx);
		}
	}
	
	private static void generateMutant(Class<?> clazz, GeneratorContext ctx) throws IOException {
		ClassFieldMethods cfm = new ClassFieldMethods(clazz);
		ctx.addClassFieldMethods(cfm);
		
		if (cfm.isMutant()) {
			if (log.isInfoEnabled()) {
				log.info("generating mutant for " + clazz);
			}
			MutantConverterGenerator mg = new MutantConverterGenerator();
			mg.generate(cfm,ctx);
			ctx.addMutant(clazz);
		} else {
			if (log.isInfoEnabled()) {
				log.info(clazz + " is not a mutant");
			}
		}
	}
	
	private static void generateNonMutant(Class<?> clazz, GeneratorContext ctx) throws IOException {
		ClassFieldMethods cfm = new ClassFieldMethods(clazz);
		ctx.addClassFieldMethods(cfm);
		
		if (cfm.isMutant()) {
			//do nothing
		} else if (cfm.isValid()) {
			if (cfm.isSimpleImmutable()) {
				if (log.isInfoEnabled()) {
					log.info("generating simple immutable generator for " + clazz);
				}
				SimpleNonMutantConverterGenerator gen = new SimpleNonMutantConverterGenerator();
				gen.generate(cfm, ctx);
			} else if (cfm.isEnum()) {
				if (log.isInfoEnabled()) {
					log.info("NOT generating enum generator for " + clazz + 
							" enums not currently supported");
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("generating non mutant generator for " + clazz);
				}
				NonMutantConverterGenerator gen = new NonMutantConverterGenerator();
				gen.generate(cfm, ctx);
			}
		} else {
			String errorStart = cfm.logFieldMethods();
			throw new IllegalArgumentException(THE_SOURCE_CODE_GENERATOR_REQUIRES_EITHER_A_MUTANT_OR_SINGLE_FIELD_W_CONSTRUCTOR_MODELS +
					"\n\r" + clazz + "\n\r" + errorStart);
		}
		
		
	}
	private static String makePackageDirectories(String packageName, String rootDir, String suffix) {
		StringTokenizer token = new StringTokenizer(packageName, ".");
	
		StringBuilder sb = new StringBuilder();
		sb.append(rootDir);
		
		while (token.hasMoreElements()) {
			sb.append(File.separator);
			String dir = token.nextToken();
			sb.append(dir);
		}
		String dirs = sb.toString();
		File directories = new File(dirs);
		if (!directories.exists()) {
			if (!directories.mkdirs()) {
				throw new IllegalStateException(THERE_WAS_A_ERROR_CREATING_THE_DIRECTORY + dirs);
			}
		}
		if (!StringUtils.isEmpty(suffix)) {
			dirs = dirs + File.separator + suffix;
			directories = new File(dirs);
			if (!directories.exists()) {
				if (!directories.mkdirs()) {
					throw new IllegalStateException(THERE_WAS_A_ERROR_CREATING_THE_DIRECTORY + dirs);
				}
			}
		}
		return dirs;
	}

	private static void makeRootDir(String rootDir) {
		String dir = rootDir;
		File directory =  new File(dir);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IllegalStateException(THERE_WAS_A_ERROR_CREATING_THE_DIRECTORY + dir);
			}
		}
	}
}
