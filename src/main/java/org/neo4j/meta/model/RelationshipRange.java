package org.neo4j.meta.model;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * The range of a relationshiptype. i.e. a set of classes that
 * can have a relationship with a given {@link MetaModelClass}).
 */
public abstract class RelationshipRange
{
	static final String KEY_RANGE_IMPL = "range_implementation_class";
	
	private MetaModelRestrictable<RelationshipRange> owner;
	
	protected MetaModelRestrictable<RelationshipRange> getOwner()
	{
		return this.owner;
	}
	
	private static GraphDatabaseService graphDb( MetaModel meta )
	{
		return ( ( MetaModelImpl ) meta ).graphDb();
	}
	
	protected void store( MetaModelRestrictable<RelationshipRange> owner )
	{
		// MP: This isn't very good, should be in the constructor, but we can't
		// really trust the developer to supply the correct property instance.
		// So we do this internally when the MetaStructureProperty#setRange
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
	
	protected static void removeRange( MetaModelRestrictable<RelationshipRange> owner )
	{
		RelationshipRange range = loadRange( owner );
		if ( range != null )
		{
			owner.node().removeProperty( KEY_RANGE_IMPL );
			range.internalRemove( owner );
		}
	}
	
	protected abstract void internalStore( MetaModelRestrictable<RelationshipRange> owner );
	
	protected abstract void internalRemove( MetaModelRestrictable<RelationshipRange> owner );
	
	protected abstract void internalLoad( MetaModelRestrictable<RelationshipRange> owner );
	
	protected static RelationshipRange loadRange( MetaModelRestrictable<RelationshipRange> owner )
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
			RelationshipRange result = ( RelationshipRange ) cls.newInstance();
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
	
	protected static void setOrRemoveRange( MetaModelRestrictable<RelationshipRange> owner,
		RelationshipRange range )
	{
		Transaction tx = graphDb( owner.model() ).beginTx();
		try
		{
			RelationshipRange.removeRange( owner );
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
