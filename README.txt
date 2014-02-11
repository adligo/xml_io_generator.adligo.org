This project generates xml_io Converters for custom models
so that they may be serialized to the condensed xml_io xml format.

See the models_core_xml_io project for an example
outputDirectory where the java source code is going
ignoreClassList classes you want to ignore
ignoreClassesContaining  text which will match to ignore classes
ignoreJarList jars you want to ignore
useClassNamesInXml if false it will obsfucate them ie a, b, c 
useFieldNamesInXml if false it will obsfucate them ie a, b, c
basePackage the base package you want look at for generation to append the namespaceSuffix package to 
namespaceSuffix the simple name of the namespace to append to the base package

ie so if basePackage is org.adligo.models.core
and the namespaceSuffix is xml_io
you will get 
org.adligo.models.core.xml_io
org.adligo.models.core.xml_io.utils
org.adligo.models.core.xml_io.ids


The generator follows the following strategy to attempt to generate these Converters;
using reflection check Class fields
     for each non transient field if there is a setter and getter
        generate a Converter using the MutableModelGenerator
     else if there is a single non transient field with a matching getter
        and there is a Constructor with a single parameter using a compatible class 
        	(the same class or a interface that the class can cast to) to the single field
           generate a Converter using the ModelGenerator
     else throw a exception
          