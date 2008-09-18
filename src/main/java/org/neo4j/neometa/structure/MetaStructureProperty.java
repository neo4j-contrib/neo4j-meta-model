package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;

/**
 * Represents a property which may be in one or more class' domain.
 */
public class MetaStructureProperty extends MetaStructureThing
	implements MetaStructureRestrictable
{
	/**
	 * @param meta the {@link MetaStructure} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaStructureProperty( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	private Collection<MetaStructureProperty> hierarchyCollection(
		Direction direction )
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( neo(),
			node(), MetaStructureRelTypes.META_IS_SUBPROPERTY_OF, direction,
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
	
	/**
	 * @return a modifiable {@link Collection} of {@link MetaStructureClass}
	 * instances which this property has as domain.
	 */
	public Collection<MetaStructureClass> associatedMetaClasses()
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( neo(),
			node(), MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.INCOMING, meta(), MetaStructureClass.class );
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
		setOrRemoteProperty( KEY_MIN_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMinCardinality()
	{
		return ( Integer ) getProperty( KEY_MIN_CARDINALITY, null );
	}
	
	public void setMaxCardinality( Integer cardinalityOrNull )
	{
		setOrRemoteProperty( KEY_MAX_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMaxCardinality()
	{
		return ( Integer ) getProperty( KEY_MAX_CARDINALITY, null );
	}
	
	public void setCardinality( Integer cardinality )
	{
		Transaction tx = neo().beginTx();
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

	public void setCollectionBehaviourClass(
		Class<? extends Collection> collectionClassOrNull )
	{
		setOrRemoteProperty( KEY_COLLECTION_CLASS,
			collectionClassOrNull == null ? null :
			collectionClassOrNull.getName() );
	}
	
	public Class<? extends Collection<?>> getCollectionBehaviourClass()
	{
		try
		{
			String className = ( String ) getProperty( KEY_COLLECTION_CLASS,
				null );
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
	public void setInverseOf( MetaStructureProperty propertyOrNull )
	{
		setSingleRelationshipOrNull( propertyOrNull == null ? null :
			propertyOrNull.node(), MetaStructureRelTypes.META_IS_INVERSE_OF );
	}
	
	/**
	 * @return the owl:inverseOf property, or {@code null} if there's no
	 * inverse.
	 */
	public MetaStructureProperty getInverseOf()
	{
		Relationship rel = getSingleRelationshipOrNull(
			MetaStructureRelTypes.META_IS_INVERSE_OF );
		return rel == null ? null : new MetaStructureProperty( meta(),
			rel.getOtherNode( node() ) );
	}
}
