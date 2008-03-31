package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;

public class MetaStructureNamespace extends MetaStructureObject
{
	public MetaStructureNamespace( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	public MetaStructureClass getMetaClass( String name, boolean allowCreate )
	{
		return meta().findOrCreateInCollection( getMetaClasses(), name,
			allowCreate, MetaStructureClass.class );
	}
	
	public Collection<MetaStructureClass> getMetaClasses()
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_CLASS, Direction.OUTGOING, meta(),
			MetaStructureClass.class );
	}
	
	public MetaStructureProperty getMetaProperty( String name,
		boolean allowCreate )
	{
		return meta().findOrCreateInCollection( getMetaProperties(), name,
			allowCreate, MetaStructureProperty.class );
	}
	
	public Collection<MetaStructureProperty> getMetaProperties()
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_PROPERTY, Direction.OUTGOING, meta(),
			MetaStructureProperty.class );
	}
}
