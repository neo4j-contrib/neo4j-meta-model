package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

/**
 * Represents a restriction a class has on a property.
 */
public class MetaStructureRestriction extends MetaStructureObject
	implements MetaStructureRestrictable
{
	/**
	 * @param meta the {@link MetaStructure} instance.
	 * @param node the root node.
	 */
	public MetaStructureRestriction( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	/**
	 * @return the class which this restriction applies to.
	 */
	public MetaStructureClass getMetaClass()
	{
		return new MetaStructureClass( meta(),
			meta().neoUtil().getSingleOtherNode( node(),
				MetaStructureRelTypes.META_RESTRICTION_TO_CLASS,
				Direction.OUTGOING ) );
	}
	
	/**
	 * @return the property which this restriction applies to.
	 */
	public MetaStructureProperty getMetaProperty()
	{
		return new MetaStructureProperty( meta(),
			meta().neoUtil().getSingleOtherNode( node(),
				MetaStructureRelTypes.META_RESTRICTION_TO_PROPERTY,
				Direction.OUTGOING ) );
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
}
