package org.neo4j.neometa.model;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;
import org.neo4j.neometa.structure.MetaStructureThing;

public class MetaClassMapping
{
	private static Map<Class<? extends MetaObject<?>>,
		Class<? extends MetaStructureThing>> mapping =
			new HashMap<Class<? extends MetaObject<?>>,
				Class<? extends MetaStructureThing>>();
	static
	{
		mapping.put( MetaClass.class, MetaStructureClass.class );
		mapping.put( MetaProperty.class, MetaStructureProperty.class );
	}
	
	public static Class<? extends MetaStructureThing> getMetaStructureClass(
		Class<? extends MetaObject<?>> metaClass )
	{
		return mapping.get( metaClass );
	}
}
