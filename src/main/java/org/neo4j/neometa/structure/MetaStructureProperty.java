package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public class MetaStructureProperty extends MetaStructureThing
{
	public MetaStructureProperty( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	private Collection<MetaStructureProperty> hierarchyCollection(
		Direction direction )
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_IS_SUBPROPERTY_OF, direction,
			meta(), MetaStructureProperty.class );
	}
	
	@Override
	public Collection<MetaStructureProperty> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaStructureProperty> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}

	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaStructureRelTypes.META_IS_SUBPROPERTY_OF;
	}
	
	public Collection<MetaStructureClass> associatedMetaClasses()
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.INCOMING, meta(), MetaStructureClass.class );
	}

	public void setRange( PropertyRange range )
	{
		range.store( this );
	}
	
	public PropertyRange getRange()
	{
		return PropertyRange.loadRange( this );
	}
}
