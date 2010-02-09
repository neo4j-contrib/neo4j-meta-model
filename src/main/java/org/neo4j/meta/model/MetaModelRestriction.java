package org.neo4j.meta.model;

import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Represents a restriction a class has on a property.
 */
public class MetaModelRestriction extends MetaModelObject
	implements MetaModelRestrictable
{
	/**
	 * @param model the {@link MetaModel} instance.
	 * @param node the root node.
	 */
	public MetaModelRestriction( MetaModel model, Node node )
	{
		super( model, node );
	}
	
	/**
	 * @return the class which this restriction applies to.
	 */
	public MetaModelClass getMetaClass()
	{
		return new MetaModelClass( model(),
			graphDbUtil().getSingleOtherNode( node(),
				MetaModelRelTypes.META_RESTRICTION_TO_CLASS,
				Direction.OUTGOING ) );
	}
	
	/**
	 * @return the property which this restriction applies to.
	 */
	public MetaModelProperty getMetaProperty()
	{
		return new MetaModelProperty( model(),
			graphDbUtil().getSingleOtherNode( node(),
				MetaModelRelTypes.META_RESTRICTION_TO_PROPERTY,
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
		return ( Integer ) node().getProperty( KEY_MIN_CARDINALITY, null );
	}
	
	public void setMaxCardinality( Integer cardinalityOrNull )
	{
		setOrRemoteProperty( KEY_MAX_CARDINALITY, cardinalityOrNull );
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
}
