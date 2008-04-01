package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

/**
 * Represents a class in the meta model.
 */
public class MetaStructureClass extends MetaStructureThing
{
	/**
	 * @param meta the {@link MetaStructure} instance.
	 * @param node the {@link Node} to wrap.
	 */
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
	
	/**
	 * @return a modifiable collection of all properties related to this class.
	 */
	public Collection<MetaStructureProperty> getProperties()
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.OUTGOING, meta(), MetaStructureProperty.class );
	}
	
	/**
	 * @return a modifiable collection of instances of this class.
	 */
	public Collection<Node> getInstances()
	{
		return new MetaStructureInstanceCollection( node(), meta() );
	}
}
