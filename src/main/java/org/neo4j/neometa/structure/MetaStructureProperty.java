package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public class MetaStructureProperty extends MetaStructureThing
{
	private static final String KEY_MIN_CARDINALITY = "min_cardinality";
	private static final String KEY_MAX_CARDINALITY = "max_cardinality";
	private static final String KEY_COLLECTION_CLASS = "collection_class";
	
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
	
	private void setOrRemoteProperty( String key, Object value )
	{
		if ( value == null )
		{
			removeProperty( key );
		}
		else
		{
			setProperty( key, value );
		}
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
	
	public void setCollectionBehaviourClass(
		Class<? extends Collection<?>> collectionClassOrNull )
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
			return ( Class<? extends Collection<?>> ) Class.forName(
				className ); 
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
