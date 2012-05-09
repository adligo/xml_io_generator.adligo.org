package org.adligo.xml_io.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.adligo.i.util.client.StringUtils;
import org.adligo.jse.util.JSECommonInit;
import org.adligo.xml_io.generator.models.ClassFieldMethods;
import org.adligo.xml_io.generator.models.GeneratorContext;
import org.adligo.xml_io.generator.models.Namespace;
import org.adligo.xml_io.generator.models.SourceCodeGeneratorParams;

public class SourceCodeGenerator {

	public static final String THERE_WAS_A_ERROR_CREATING_THE_DIRECTORY = "There was a error creating the directory ";
	public static final String THE_SOURCE_CODE_GENERATOR_REQUIRES_EITHER_A_MUTANT_OR_SINGLE_FIELD_W_CONSTRUCTOR_MODELS = 
		"The SourceCodeGenerator requires either a Mutant implementation of I_Immutable for model xml_io souce code generation.";

	public static void main(String [] args) {
		JSECommonInit.callLogDebug("main");
		
		if (args.length == 0) {
			System.err.println("SouceCodeGenerator requires a property file path!");
			return;
		}
		File file = new File(args[0]);
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			props.load(fis);
			SourceCodeGeneratorParams params = new SourceCodeGeneratorParams(props);
			generate(params);
		} catch (FileNotFoundException e) {
			System.err.println("SouceCodeGenerator was not able to find the property file " + args[0]);
			return;
		} catch (IOException e) {
			System.err.println("SouceCodeGenerator had a problem loading the the property file " + args[0]);
			return;
		} catch (Exception x) {
			x.printStackTrace();
			return;
		}
		
		
	}
	public static void generate(SourceCodeGeneratorParams params) throws IOException {
		String dir = params.getOutputDirectory();
		makeRootDir(dir);
		
		Set<String> packages = params.getPackages();
		for (String name : packages) {
			GeneratorContext ctx = new GeneratorContext();
			String pkgDir = makePackageDirectories(name, dir);
			ctx.setPackageDirectory(pkgDir);
			ctx.setPackageName(name);
			
			String version = params.getVersion();
			if (StringUtils.isEmpty(version)) {
				ctx.setVersion(version);
			}
			
			ctx.setParams(params);
			String namespace = Namespace.toNamespace(name);
			ctx.setNamespace(namespace);
			
			List<Class<?>> classes = params.getClasses(name);
			for (Class<?> clazz : classes) {
				generate(clazz, ctx);
			}
			SetupGenerator setup = new SetupGenerator();
			setup.generate(ctx);
		}
	}
	
	private static void generate(Class<?> clazz, GeneratorContext ctx) throws IOException {
		ClassFieldMethods cfm = new ClassFieldMethods(clazz);
		ctx.addClassFieldMethods(cfm);
		
		if (cfm.isMutant()) {
			MutantConverterGenerator mg = new MutantConverterGenerator();
			mg.generate(cfm,ctx);
		} else if (cfm.isValid()) {
			
		} else {
			String errorStart = cfm.logFieldMethods();
			throw new IllegalArgumentException(THE_SOURCE_CODE_GENERATOR_REQUIRES_EITHER_A_MUTANT_OR_SINGLE_FIELD_W_CONSTRUCTOR_MODELS +
					"\n\r" + clazz + "\n\r" + errorStart);
		}
		
		
	}
	
	private static String makePackageDirectories(String packageName, String rootDir) {
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
