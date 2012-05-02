This project generates xml_io Converters for custom models
so that they may be serialized to the condensed xml_io xml format.

The generator follows the following strategy to attempt to generate these Converters;
using reflection check Class fields
     for each non transient field if there is a setter and getter
        generate a Converter using the MutableModelGenerator
     else if there is a single non transient field with a matching getter
        and there is a Constructor with a single parameter using a compatible class 
        	(the same class or a interface that the class can cast to) to the single field
           generate a Converter using the ModelGenerator
     else throw a exception
          