package org.neo4j.meta.model;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * The range of {@link Node}s that can be instance of a {@link MetaModelClass}.  
 */
public abstract class InstanceRange extends Range<InstanceRange>
{
	
	protected void store(  MetaModelRestrictable<InstanceRange> owner )
	{
		// MP: This isn't very good, should be in the constructor, but we can't
		// really trust the developer to supply the correct instance instance.
		// So we do this internally when the MetaStructureInstance#setRange
		// method is called. Possible cause of bugs/errors.
		this.owner = owner;
		
		Transaction tx = graphDb( owner.model() ).beginTx();
		try
		{
			removeRange( owner );
			owner.node().setProperty( KEY_RANGE_IMPL, getClass().getName() );
			internalStore( owner );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected static void removeRange(  MetaModelRestrictable<InstanceRange> owner )
	{
		InstanceRange range = loadRange( owner );
		if ( range != null )
		{
			owner.node().removeProperty( KEY_RANGE_IMPL );
			range.internalRemove( owner );
		}
	}
	
	protected static InstanceRange loadRange( MetaModelRestrictable<InstanceRange> owner )
	{
		Transaction tx = graphDb( owner.model() ).beginTx();
		try
		{
			String rangeType = ( String ) owner.node().getProperty(
				KEY_RANGE_IMPL, null );
			if ( rangeType == null )
			{
				return null;
			}
			Class<?> cls = Class.forName( rangeType );
			InstanceRange result = ( InstanceRange ) cls.newInstance();
			result.owner = owner;
			result.internalLoad( owner );
			tx.success();
			return result;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected static void setOrRemoveRange( MetaModelRestrictable<InstanceRange> owner,
		InstanceRange range )
	{
		Transaction tx = graphDb( owner.model() ).beginTx();
		try
		{
			InstanceRange.removeRange( owner );
			if ( range != null )
			{
				range.store( owner );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
}
