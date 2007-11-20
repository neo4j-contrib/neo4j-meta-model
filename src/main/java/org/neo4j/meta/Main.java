package org.neo4j.meta;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Transaction;

// TODO: actually, we should probably split up the Meta* interfaces so that
// we can expose a read-only meta model to a client? Or something like that.
// I mean, we want to compute and smartly tick up version numbers when
// constraints are added/removed. For example. Let's do it in a good API way.
// Yea.
public class Main
{
	private static NeoService embeddedNeoInstance = null;
	private static MetaManager metaManager = null;
	private static AtomicBoolean shutdownInitiated = new AtomicBoolean( false );

	private static NeoService getNeo()
	{
		return embeddedNeoInstance;
	}

	private static MetaManager getMetaManager()
	{
		return metaManager;
	}

	private static void initialize()
	{
		embeddedNeoInstance = new EmbeddedNeo( "var" );
		metaManager = new MetaManager( getNeo() );
		Map<String, Serializable> shellConfig = Collections.emptyMap();
		getNeo().enableRemoteShell( shellConfig );
		System.out.println( "Neo started" );
	}

	public static void main( String[] args ) throws InterruptedException
	{
		addShutdownHook();
		initialize();
		if ( args.length > 0 && args[0].equals( "-c" ) )
		{
			createPersonType();
		}
		if ( args.length > 0 && args[0].equals( "-t" ) )
		{
			testStuff();
		}
		listAllNodeTypes();
		while ( true )
		{
			Thread.sleep( 500 );
		}
		// shutdown();
	}

	private static void listAllNodeTypes()
	{
		for ( NodeType type : getMetaManager().getNodeTypes() )
		{
			System.out.println();
			listNodeType( type );
		}
	}

	private static void listNodeType( NodeType type )
	{
		System.out.println( "Node type " + type.getName() + "[version "
		    + type.getVersion() + "]" );
		System.out.println( "Required properties: " +
			( type.getRequiredProperties().iterator().hasNext()
				? "none" : "" ) );
		for ( MetaProperty property : type.getRequiredProperties() )
		{
			System.out.println( "\tKey: " + property.getKey() );
			if ( property.getValueType() != null )
			{
				System.out.println( "\tValue type: " +
					property.getValueType() );
			}
		}
		listRelationships( type, Direction.INCOMING );
		listRelationships( type, Direction.OUTGOING );
	}
	
	private static void listRelationships( NodeType type, Direction dir )
	{
		System.out.println( "Allowed " + dir.name().toLowerCase() +
			" relationships: " +
			( type.getAllowedRelationships( dir ).iterator().hasNext()
				? "none" : "" ) );
		for ( MetaRelationship rel : type.getAllowedRelationships( dir ) )
		{
			System.out.println( "\tType: " + rel.getNameOfType() );
		}		
	}

	private static void createPersonType()
	{
		System.out.println( "Creating person type" );
		Transaction tx = Transaction.begin();
		try
		{
			NodeType newType = getMetaManager().createNodeType( "Person" );
			newType.addRequiredProperty( "name" );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private static void testStuff()
	{
		System.out.println( "Testing stuff" );
		NodeType personType = getMetaManager().getNodeTypeByName( "Person" );
		MetaProperty nameProperty = personType.getRequiredProperty( "name" );
		System.out.println( "Setting value" );
		nameProperty.setValueType( Boolean.class );
	}

	private static void shutdown()
	{
		if ( shutdownInitiated.compareAndSet( false, true ) )
		{
			System.out.println( "Shutting down..." );
			try
			{
				getNeo().shutdown();
				embeddedNeoInstance = null;
			}
			catch ( NullPointerException npe )
			{
				// Means we've already shut down or never started
			}
			catch ( Throwable t )
			{
				System.err.println( "Error shutting down Neo: " + t );
			}
		}
	}

	private static void addShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				shutdown();
			}
		} );
	}
}
