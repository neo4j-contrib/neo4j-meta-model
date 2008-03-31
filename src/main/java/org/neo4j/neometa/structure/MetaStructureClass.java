package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public class MetaStructureClass extends MetaStructureThing
{
	public MetaStructureClass( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	private Collection<MetaStructureClass> hierarchyCollection(
		Direction direction )
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_IS_SUBCLASS_OF, direction, meta(),
			MetaStructureClass.class );
	}
	
	@Override
	public Collection<MetaStructureClass> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaStructureClass> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}
	
	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaStructureRelTypes.META_IS_SUBCLASS_OF;
	}
	
	public Collection<MetaStructureProperty> getProperties()
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.OUTGOING, meta(), MetaStructureProperty.class );
	}
}
