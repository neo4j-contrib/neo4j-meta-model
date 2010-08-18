package examples;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.meta.model.ClassRange;
import org.neo4j.meta.model.MetaModel;
import org.neo4j.meta.model.MetaModelClass;
import org.neo4j.meta.model.MetaModelImpl;
import org.neo4j.meta.model.MetaModelNamespace;
import org.neo4j.meta.model.MetaModelProperty;
import org.neo4j.meta.model.MetaModelRelationship;
import org.neo4j.meta.model.MetaModelRelationshipRestriction;

public class SiteExamples
{

    private EmbeddedGraphDatabase neo4j;

    @Before
    public void setupNeo()
    {
        neo4j = new EmbeddedGraphDatabase( "target/var/neo4j" );
    }

    @After
    public void shutdownNeo()
    {
        neo4j.shutdown();
    }

    /**
     * A simple test to exercise the meta-model api.
     */
    @Test
    // START SNIPPET: create-simple-metamodel
    public void createSimpleMetaModel()
    {
        IndexService index = new LuceneIndexService( neo4j );
        MetaModel meta = new MetaModelImpl( neo4j, index );
        Transaction tx = neo4j.beginTx();
        try
        {
            MetaModelNamespace namespace = meta.getGlobalNamespace();

            // Create a class, use ", true" for "create it if it doesn't exist".
            MetaModelClass personClass = namespace.getMetaClass(
                    "http://metaexample.org/meta#Person", true );

            // Create a property in a similar way.
            MetaModelProperty nameProperty = namespace.getMetaProperty(
                    "http://metaexample.org/meta#name", true );

            // Tell the meta model that persons can have name properties.
            personClass.getDirectProperties().add( nameProperty );
            tx.success();
        }
        catch ( Exception e )
        {
            tx.failure();
        }
        finally
        {
            tx.finish();
        }
    }

    // END SNIPPET: create-simple-metamodel

    @Test
    // START SNIPPET: hierarchy
    public void hierarchy()
    {
        IndexService index = new LuceneIndexService( neo4j );
        MetaModel meta = new MetaModelImpl( neo4j, index );
        Transaction tx = neo4j.beginTx();
        try
        {
            MetaModelNamespace namespace = meta.getGlobalNamespace();
            MetaModelClass thing = namespace.getMetaClass( "thing", true );
            MetaModelClass person = namespace.getMetaClass( "person", true );
            thing.getDirectSubs().add( person );

            MetaModelProperty name = namespace.getMetaProperty( "name", true );
            MetaModelProperty nickName = namespace.getMetaProperty( "nickName",
                    true );
            name.getDirectSubs().add( nickName );
            tx.success();
        }
        catch ( Exception e )
        {
            tx.failure();
        }
        finally
        {
            tx.finish();
        }
    }
    // END SNIPPET: hierarchy
    @Test
    // START SNIPPET: restrictions
    public void restrictions()
    {
        IndexService index = new LuceneIndexService( neo4j );
        MetaModel meta = new MetaModelImpl( neo4j, index );
        Transaction tx = neo4j.beginTx();
        try
        {
            MetaModelNamespace namespace = meta.getGlobalNamespace();
            MetaModelClass artist = namespace.getMetaClass( "artist", true );
            MetaModelRelationship plays = namespace.getMetaRelationship( "plays", true );
            MetaModelClass drummer = namespace.getMetaClass( "drummer", true );
            MetaModelClass instrument = namespace.getMetaClass( "instrument", true );
            MetaModelClass drums = namespace.getMetaClass( "drums", true );
            drummer.getDirectSupers().add( artist );
            drums.getDirectSupers().add( instrument );
            
            // Set generic range, so that a subject 'plays' an 'instrument'
            plays.setRange( new ClassRange( instrument ) );
            
            // Set specific range for drummer, so that a 'drummer' plays 'drums'
            MetaModelRelationshipRestriction drummerPlaysRestriction =
                drummer.getRestriction( plays, true );
            drummerPlaysRestriction.setRange( new ClassRange( drums ) );
            drummerPlaysRestriction.setCardinality( 1 );
            tx.success();
        }
        catch ( Exception e )
        {
            tx.failure();
        }
        finally
        {
            tx.finish();
        }
    }
    // END SNIPPET: restrictions

}
