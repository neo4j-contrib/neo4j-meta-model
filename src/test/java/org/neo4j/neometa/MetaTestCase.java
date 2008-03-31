package org.neo4j.neometa;

import java.util.Collection;

import junit.framework.TestCase;

import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;

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
}
