package org.neo4j.meta.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.IndexService;
import org.neo4j.util.GraphDatabaseUtil;

/**
 * The access point of a meta model. Is given a root node where all the
 * namespaces, properties and classes are stored/read underneath.
 */
public class MetaModelImpl implements MetaModel
{
	private GraphDatabaseService graphDb;
	private GraphDatabaseUtil graphDbUtil;
	private IndexService indexService;
	
	private Map<String, MetaModelNamespace> namespaceCache =
		Collections.synchronizedMap(
			new HashMap<String, MetaModelNamespace>() );
	
	/**
	 * @param graphDB the {@link GraphDatabaseService} used for this meta model.
	 */
	public MetaModelImpl( GraphDatabaseService graphDB, IndexService indexService )
	{
		this.graphDb = graphDB;
		this.graphDbUtil = new GraphDatabaseUtil( graphDB );
		this.indexService = indexService;
	}
	
	/**
	 * @return the {@link GraphDatabaseService} given in the constructor.
	 */
	public GraphDatabaseService graphDb()
	{
		return this.graphDb;
	}

	/**
	 * @return the {@link IndexService} given in the constructor.
	 */
	public IndexService indexService()
	{
		return this.indexService;
	}
	
	protected GraphDatabaseUtil graphDbUtil()
	{
		return this.graphDbUtil;
	}
	
	protected Node rootNode()
	{
		return graphDbUtil().getOrCreateSubReferenceNode(
			MetaModelRelTypes.REF_TO_META_SUBREF );
	}
	
	public MetaModelNamespace getNamespace( String name,
		boolean allowCreate )
	throws DuplicateNameException
	{
		assert name != null;
		return findOrCreateInCollection( getNamespaces(), name, allowCreate,
			MetaModelNamespace.class, namespaceCache );
	}
	
	public MetaModelNamespace getGlobalNamespace()
	{
		MetaModelNamespace ns = null;
		try{
			ns = findOrCreateInCollection( getNamespaces(), null, true,
					MetaModelNamespace.class, namespaceCache );
		}catch(DuplicateNameException dne){
			//Do nothing. Global namesepace has no name, so no duplication can occur
		}
		return ns;
	}
	
	protected <T extends MetaModelObject> T findOrCreateInCollection(
		Collection<T> collection, String nameOrNullForGlobal,
		boolean allowCreate, Class<T> theClass, Map<String, T> cacheOrNull )
	throws DuplicateNameException
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			T foundItem = safeGetFromCache( cacheOrNull, nameOrNullForGlobal );
			if ( foundItem != null )
			{
				tx.success();
				return foundItem;
			}
			
			for ( T item : collection )
			{
				String theName = item.getName();
				if ( nameOrNullForGlobal == null || theName == null )
				{
					if ( nameOrNullForGlobal == null && theName == null )
					{
						foundItem = item;
						break;
					}
				}
				else if ( theName.equals( nameOrNullForGlobal ) )
				{
					foundItem = item;
					break;
				}
			}
			if ( foundItem != null )
			{
				if ( cacheOrNull != null )
				{
					cacheOrNull.put( nameOrNullForGlobal, foundItem );
				}
				return foundItem;
			}
			
			if ( !allowCreate )
			{
				return null;
			}
			Node node = graphDb().createNode();
			T item = null;
			try
			{
				item = theClass.getConstructor( MetaModel.class,
					Node.class ).newInstance( this, node );
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
			if ( nameOrNullForGlobal != null )
			{
				item.setName( nameOrNullForGlobal );
			}
			collection.add( item );
			tx.success();
			return item;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private <T extends MetaModelObject> T safeGetFromCache(
		Map<String, T> cacheOrNull, String key )
	{
		T result = null;
		if ( cacheOrNull != null )
		{
			result = cacheOrNull.get( key );
			if ( result != null )
			{
				try
				{
					graphDb().getNodeById( result.node().getId() );
				}
				catch ( NotFoundException e )
				{
					cacheOrNull.remove( result );
					result = null;
				}
			}
		}
		return result;
	}
	
	public Collection<MetaModelNamespace> getNamespaces()
	{
		return new ObjectCollection<MetaModelNamespace>(
			graphDb(), rootNode(), MetaModelRelTypes.META_NAMESPACE,
			Direction.OUTGOING, this, MetaModelNamespace.class );
	}
	
	public <T> T lookup( MetaModelProperty property, PropertyLookerUpper<T> finder,
		MetaModelClass... classes )
	{
		// TODO Maybe add some form of caching here since this method will be
		// HEAVILY used. It's the main way of looking things up in the meta
		// model, f.ex. validation and conversion of values a.s.o.
		
		Transaction tx = graphDb().beginTx();
		try
		{
			T result = LookupUtil.lookup( property, finder, classes );
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	public <T> T lookup( MetaModelRelationship relationshipType, RelationshipLookerUpper<T> finder,
			MetaModelClass... classes )
	{
		// TODO Maybe add some form of caching here since this method will be
		// HEAVILY used. It's the main way of looking things up in the meta
		// model, f.ex. validation and conversion of values a.s.o.
		
		Transaction tx = graphDb().beginTx();
		try
		{
			T result = LookupUtil.lookup( relationshipType, finder, classes );
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}

}
