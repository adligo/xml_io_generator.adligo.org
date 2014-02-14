package org.adligo.xml_io_generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.i.util.shared.StringUtils;
import org.adligo.models.params.shared.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io_generator.models.ClassFieldMethods;
import org.adligo.xml_io_generator.models.GeneratorContext;

public class SetupGenerator {
	private static final Templates templates = new Templates("/org/adligo/xml_io_generator/converter_setup_template.xml", true);
	private static final Template template = templates.getTemplate("setup");
	private GeneratorContext ctx;
	
	public void generate(GeneratorContext gc) throws IOException {
		ctx = gc;
		
		Params params = new Params();
		String ns = ctx.getNamespace();
		params.addParam("namespace", ns);
		String version = ctx.getPackageVersion();
		if (StringUtils.isEmpty(version)) {
			version = new Date(System.currentTimeMillis()).toString();
		}
		params.addParam("namespaceVersion", version);
		
		Set<String> extraImports = ctx.getExtraClassImports();
		for (String p: extraImports) {
			params.addParam("extraImport", p);
		}
		
		String pkg = ctx.getNewPackageName();
		params.addParam("packageName", pkg);
		
		Set<Entry<String,String>> classNames = gc.getClassToConverterNames();
		for (Entry<String,String> names: classNames) {
			String clazz = names.getKey();
			String gen = names.getValue();
			Params clazzParams = new Params();
			params.addParam("class", gen, clazzParams);
			clazzParams.addParam("className", clazz);
		}
		
		classNames = gc.getClassToAttributeGeneratorNames();
		for (Entry<String,String> names: classNames) {
			String clazz = names.getKey();
			String gen = names.getValue();
			Params clazzParams = new Params();
			params.addParam("attributeClass", gen, clazzParams);
			clazzParams.addParam("attributeClassName", clazz);
		}
		String oldPackage = gc.getOldPackageName();
		String newPackage = gc.getNewPackageName();
		if (!oldPackage.equals(newPackage)) {
			Iterator<ClassFieldMethods> cfms = gc.getClassFieldMethodsIterator();
			
			Set<String> extraIs = new HashSet<String>();
			while (cfms.hasNext()) {
				ClassFieldMethods cfm = cfms.next();
				Class<?> clazz = cfm.getClazz();
				extraIs.add(clazz.getName());
				
			}
			extraIs.removeAll(extraImports);
			for (String name: extraIs) {
				params.addParam("extraImport",name);
			}
		}
		SourceFileWriter sfw = new SourceFileWriter();
		String dir = ctx.getNewPackageDir().getAbsolutePath();
		
		sfw.setDir(dir);
		sfw.setTemplate(template);
		sfw.writeSourceFile("ConverterSetup", params);
	}
}
