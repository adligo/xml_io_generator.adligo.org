package org.adligo.xml_io_generator.models;

import org.adligo.models.core.shared.DomainName;
import org.adligo.models.core.shared.InvalidParameterException;

public class Namespace {

	/**
	 * assumes the first two namespace parts of the packageName
	 * are the url
	 * ie adligo.org.foo becomes;
	 * http://www.adligo.org/foo
	 * 
	 * @param packageName
	 * @return
	 */
	public static String toNamespace(String packageName) {
		try {
			DomainName dn = new DomainName(packageName);
			int size = dn.getComponentSize();
			if (size < 3) {
				throw new IllegalStateException("The package name must have at least 3 components ie a.b.c");
			}
			String one = dn.getComponent(0);
			String two = dn.getComponent(1);
			
			StringBuilder sb = new StringBuilder();
			sb.append("http://www." + two + "." + one + "/");
			for (int i = 2; i < dn.getComponentSize(); i++) {
				String comp = dn.getComponent(i);
				if (i != 2) {
					sb.append("/");
				}
				sb.append(comp);
			}
			return sb.toString();
		} catch (InvalidParameterException x) {
			throw new IllegalStateException(x);
		}
	}
}
