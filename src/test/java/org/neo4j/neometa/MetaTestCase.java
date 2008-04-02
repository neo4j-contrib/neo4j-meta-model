package org.neo4j.neometa;

import java.util.Collection;

import junit.framework.TestCase;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.neometa.structure.MetaStructureRelTypes;
import org.neo4j.util.EntireGraphDeletor;

/**
 * Base class for the meta model tests.
 */
public abstract class MetaTestCase extends TestCase
{
	private static NeoService neo;
	
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
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	protected NeoService neo()
	{
		return neo;
	}

	protected <T> void assertCollection( Collection<T> collection, T... items )
	{
		assertEquals( items.length, collection.size() );
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
}
