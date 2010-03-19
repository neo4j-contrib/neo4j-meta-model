package org.neo4j.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.meta.model.MetaModelRelTypes;
import org.neo4j.util.EntireGraphDeletor;

/**
 * Base class for the meta model tests.
 */
public abstract class MetaTestCase
{
	private static GraphDatabaseService graphDb;
	
	private Transaction tx;
	
	@BeforeClass
	public static void setUpDb() throws Exception
	{
		graphDb = new EmbeddedGraphDatabase( "target/var/neo4j" );
	}
	
	@Before
	public void setUpTest()
	{
        tx = graphDb().beginTx();
	}
	
	@After
	public void tearDownTest() throws Exception
	{
		tx.success();
		tx.finish();
	}
	
	@AfterClass
	public static void tearDownDb()
	{
	    graphDb.shutdown();
	}
	
	protected static GraphDatabaseService graphDb()
	{
		return graphDb;
	}

    protected <T> void assertCollection( Iterable<T> iterable, T... items )
    {
        Collection<T> collection = new ArrayList<T>();
        for ( T item : iterable )
        {
            collection.add( item );
        }
        assertCollection( collection, items );
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
		Relationship rel = graphDb().getReferenceNode().getSingleRelationship(
			MetaModelRelTypes.REF_TO_META_SUBREF, Direction.OUTGOING );
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
