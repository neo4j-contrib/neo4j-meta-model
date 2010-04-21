package org.neo4j.meta.model;

import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

/**
 * Represents a relationshiptype which may be in one or more class' domain.
 */
public class MetaModelRelationship extends MetaModelPropertyContainer
	implements MetaModelRestrictable<RelationshipRange>
{
	/**
	 * @param model the {@link MetaModel} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaModelRelationship( MetaModel model, Node node )
	{
		super( model, node );
	}
	
	private Collection<MetaModelRelationship> hierarchyCollection(
		Direction direction )
	{
		return new ObjectCollection<MetaModelRelationship>( graphDb(),
			node(), MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF, direction,
			model(), MetaModelRelationship.class );
	}
	
	@Override
	public Collection<MetaModelRelationship> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaModelRelationship> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}

	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF;
	}
	
	
	
	/**
	 * @return a modifiable {@link Collection} of {@link MetaModelClass}
	 * instances which this RelationshipType has as domain.
	 */
	public Collection<MetaModelClass> associatedMetaClasses()
	{
		return new ObjectCollection<MetaModelClass>( graphDb(),
			node(), MetaModelRelTypes.META_CLASS_HAS_RELATIONSHIP,
			Direction.INCOMING, model(), MetaModelClass.class );
	}

	public void setRange( RelationshipRange range )
	{
		RelationshipRange.setOrRemoveRange( this, range );
	}
	
	public RelationshipRange getRange()
	{
		return RelationshipRange.loadRange( this );
	}
	
	public void setMinCardinality( Integer cardinalityOrNull )
	{
		setOrRemoveProperty( KEY_MIN_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMinCardinality()
	{
		return ( Integer ) node().getProperty( KEY_MIN_CARDINALITY, null );
	}
	
	public void setMaxCardinality( Integer cardinalityOrNull )
	{
		setOrRemoveProperty( KEY_MAX_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMaxCardinality()
	{
	    return ( Integer ) node().getProperty( KEY_MAX_CARDINALITY, null );
	}
	
	public void setCardinality( Integer cardinality )
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			setMinCardinality( cardinality );
			setMaxCardinality( cardinality );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	
}
