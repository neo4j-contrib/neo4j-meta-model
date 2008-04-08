package org.neo4j.neometa.structure;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;

/**
 * Represents a property which may be in one or more class' domain.
 */
public class MetaStructureProperty extends MetaStructureThing
{
	private static final String KEY_MIN_CARDINALITY = "min_cardinality";
	private static final String KEY_MAX_CARDINALITY = "max_cardinality";
	private static final String KEY_COLLECTION_CLASS = "collection_class";
	
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
	
	/**
	 * @return a modifiable {@link Collection} of {@link MetaStructureClass}
	 * instances which this property has as domain.
	 */
	public Collection<MetaStructureClass> associatedMetaClasses()
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.INCOMING, meta(), MetaStructureClass.class );
	}

	/**
	 * Sets the range of the expected value(s) for this property. F.ex.
	 * a string, a number or a an instance of a {@link MetaStructureClass}.
	 * @param range
	 */
	public void setRange( PropertyRange range )
	{
		range.store( this );
	}
	
	/**
	 * @return the {@link PropertyRange} set with
	 * {@link #setRange(PropertyRange)}.
	 */
	public PropertyRange getRange()
	{
		return PropertyRange.loadRange( this );
	}
	
	/**
	 * Sets the minimum cardinality of this property. {@code null} means
	 * no restriction.
	 * @param cardinalityOrNull the minimum cardinality to set.
	 */
	public void setMinCardinality( Integer cardinalityOrNull )
	{
		setOrRemoteProperty( KEY_MIN_CARDINALITY, cardinalityOrNull );
	}
	
	/**
	 * @return the mimimum cardinality set for this property. Can return
	 * {@code null} which means no restriction.
	 */
	public Integer getMinCardinality()
	{
		return ( Integer ) getProperty( KEY_MIN_CARDINALITY, null );
	}
	
	/**
	 * Sets the maximum cardinality of this property. {@code null} means
	 * no restriction.
	 * @param cardinalityOrNull the maximum cardinality to set.
	 */
	public void setMaxCardinality( Integer cardinalityOrNull )
	{
		setOrRemoteProperty( KEY_MAX_CARDINALITY, cardinalityOrNull );
	}
	
	/**
	 * @return the maximum cardinality set for this property. Can return
	 * {@code null} which means no restriction.
	 */
	public Integer getMaxCardinality()
	{
		return ( Integer ) getProperty( KEY_MAX_CARDINALITY, null );
	}
	
	/**
	 * If cardinality is >1 then this will decide the rules of the collection.
	 * F.ex {@link Set} doesn't allow duplicates whereas {@link List} will.
	 * @param collectionClassOrNull
	 */
	public void setCollectionBehaviourClass(
		Class<? extends Collection> collectionClassOrNull )
	{
		setOrRemoteProperty( KEY_COLLECTION_CLASS,
			collectionClassOrNull == null ? null :
			collectionClassOrNull.getName() );
	}
	
	/**
	 * @return the collection behaviour set with
	 * {@link #setCollectionBehaviourClass(Class)}.
	 */
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
