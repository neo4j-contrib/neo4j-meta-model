package org.neo4j.neometa;

import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;

import junit.framework.TestCase;

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
}
