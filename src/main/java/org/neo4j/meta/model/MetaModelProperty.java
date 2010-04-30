package org.neo4j.meta.model;

import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

/**
 * Represents a property which may be in one or more class' domain.
 */
public class MetaModelProperty extends MetaModelThing
	implements MetaModelRestrictable<PropertyRange>
{
	
	public static final String INDEXING_MODE = "indexing_mode";
	/**
	 * @param model the {@link MetaModel} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaModelProperty( MetaModel model, Node node )
	{
		super( model, node );
	}
	
	private Collection<MetaModelProperty> hierarchyCollection(
		Direction direction )
	{
		return new ObjectCollection<MetaModelProperty>( graphDb(),
			node(), MetaModelRelTypes.META_IS_SUBPROPERTY_OF, direction,
			model(), MetaModelProperty.class );
	}
	
	@Override
	public Collection<MetaModelProperty> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaModelProperty> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}

	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaModelRelTypes.META_IS_SUBPROPERTY_OF;
	}
	
	/**
	 * @return a modifiable {@link Collection} of {@link MetaModelPropertyContainer}
	 * instances which this property has as domain.
	 */
	public Collection<MetaModelClass> associatedMetaPropertyContainers()
	{
		return new ObjectCollection<MetaModelClass>( graphDb(),
			node(), MetaModelRelTypes.META_HAS_PROPERTY,
			Direction.INCOMING, model(), MetaModelClass.class );
	}

	public void setRange( PropertyRange range )
	{
		PropertyRange.setOrRemoveRange( this, range );
	}
	
	public PropertyRange getRange()
	{
		return PropertyRange.loadRange( this );
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

	public void setIndexingMode( IndexingModeTypes indexingMode )
	{
		setOrRemoveProperty( INDEXING_MODE, indexingMode.name() );
	}

	public IndexingModeTypes getIndexingMode()
	{
	    return Enum.valueOf(IndexingModeTypes.class, ( String ) node().getProperty( INDEXING_MODE, IndexingModeTypes.NO_INDEX.name() ));
	}
	
	
	public void setCollectionBehaviourClass(
		Class<? extends Collection> collectionClassOrNull )
	{
		setOrRemoveProperty( KEY_COLLECTION_CLASS,
			collectionClassOrNull == null ? null :
			collectionClassOrNull.getName() );
	}
	
	public Class<? extends Collection<?>> getCollectionBehaviourClass()
	{
		try
		{
			String className = ( String ) node().getProperty(
			    KEY_COLLECTION_CLASS, null );
			// Yep generics warning, but what're you going to do?
			return className == null ? null :
				( Class<? extends Collection<?>> ) Class.forName( className ); 
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Sets the owl:inverseOf construct.
	 * @param propertyOrNull the property which is the inverse of this property,
	 * or {@code null} if no inverse.
	 */
	public void setInverseOf( MetaModelProperty propertyOrNull )
	{
		setSingleRelationshipOrNull( propertyOrNull == null ? null :
			propertyOrNull.node(), MetaModelRelTypes.META_IS_INVERSE_OF );
	}
	
	/**
	 * @return the owl:inverseOf property, or {@code null} if there's no
	 * inverse.
	 */
	public MetaModelProperty getInverseOf()
	{
		Relationship rel = getSingleRelationshipOrNull(
			MetaModelRelTypes.META_IS_INVERSE_OF );
		return rel == null ? null : new MetaModelProperty( model(),
			rel.getOtherNode( node() ) );
	}
}
