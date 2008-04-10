package org.neo4j.neometa;

import java.util.Collection;

import junit.framework.TestCase;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neometa.structure.MetaStructureRelTypes;
import org.neo4j.util.EntireGraphDeletor;

/**
 * Base class for the meta model tests.
 */
public abstract class MetaTestCase extends TestCase
{
	private static NeoService neo;
	
	private Transaction tx;
	
	@Override
	protected void setUp() throws Exception
	{
		if ( neo == null )
		{
			neo = new EmbeddedNeo( "var/test/neo" );
			Runtime.getRuntime().addShutdownHook( new Thread()
			{
				@Override
				public void run()
				{
					neo.shutdown();
				}
			} );
		}
		tx = neo().beginTx();
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		tx.success();
		tx.finish();
		super.tearDown();
	}
	
	protected NeoService neo()
	{
		return neo;
	}

	protected <T> void assertCollection( Collection<T> collection, T... items )
	{
		String collectionString = join( ", ", collection.toArray() );
		assertEquals( collectionString, items.length, collection.size() );
		for ( T item : items )
		{
			assertTrue( collection.contains( item ) );
		}
	}
	
	protected void deleteMetaModel()
	{
		Relationship rel = neo().getReferenceNode().getSingleRelationship(
			MetaStructureRelTypes.REF_TO_META_SUBREF, Direction.OUTGOING );
		Node node = rel.getEndNode();
		rel.delete();
		new EntireGraphDeletor().delete( node );
	}

	protected <T> String join( String delimiter, T... items )
	{
		StringBuffer buffer = new StringBuffer();
		for ( T item : items )
		{
			if ( buffer.length() > 0 )
			{
				buffer.append( delimiter );
			}
			buffer.append( item.toString() );
		}
		return buffer.toString();
	}
}
