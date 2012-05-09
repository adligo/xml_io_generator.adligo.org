package org.adligo.xml_io.generator;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.i.util.client.StringUtils;
import org.adligo.models.params.client.Params;
import org.adligo.xml.parsers.template.Template;
import org.adligo.xml.parsers.template.Templates;
import org.adligo.xml_io.generator.models.GeneratorContext;

public class SetupGenerator {
	private static final Templates templates = new Templates("/org/adligo/xml_io/generator/converter_setup_template.xml", true);
	private static final Template template = templates.getTemplate("setup");
	private GeneratorContext ctx;
	
	public void generate(GeneratorContext gc) throws IOException {
		ctx = gc;
		
		Params params = new Params();
		String ns = ctx.getNamespace();
		params.addParam("namespace", ns);
		String version = ctx.getVersion();
		if (StringUtils.isEmpty(version)) {
			version = new Date(System.currentTimeMillis()).toString();
		}
		params.addParam("namespaceVersion", version);
		
		String pkg = ctx.getPackageName();
		params.addParam("package", pkg);
		
		Set<Entry<String,String>> classNames = gc.getClassToConverterNames();
		for (Entry<String,String> names: classNames) {
			String clazz = names.getKey();
			String gen = names.getValue();
			Params clazzParams = new Params();
			params.addParam("class", gen, clazzParams);
			clazzParams.addParam("className", clazz);
		}
		
		SourceFileWriter sfw = new SourceFileWriter();
		String dir = ctx.getPackageDirectory();
		sfw.setDir(dir);
		sfw.setTemplate(template);
		sfw.writeSourceFile("ConverterSetup", params);
	}
}
