package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

/**
 * The access point of a meta model. Is given a root node where all the
 * namespaces, properties and classes are stored/read underneath.
 */
public class MetaStructure
{
	private NeoService neo;
	private NeoUtil neoUtil;
	private DynamicMetaRelTypes dynamicRelTypes = new DynamicMetaRelTypes();
	
	/**
	 * @param neo the {@link NeoService} used for this meta model.
	 */
	public MetaStructure( NeoService neo )
	{
		this.neo = neo;
		this.neoUtil = new NeoUtil( neo );
		this.dynamicRelTypes = new DynamicMetaRelTypes();
	}
	
	/**
	 * @return the {@link NeoService} given in the constructor.
	 */
	public NeoService neo()
	{
		return this.neo;
	}
	
	protected NeoUtil neoUtil()
	{
		return this.neoUtil;
	}
	
	protected Node rootNode()
	{
		return neoUtil().getOrCreateSubReferenceNode(
			MetaStructureRelTypes.REF_TO_META_SUBREF );
	}
	
	protected DynamicMetaRelTypes dynamicRelTypes()
	{
		return this.dynamicRelTypes;
	}
	
	/**
	 * Returns (and optionally creates) a {@link MetaStructureNamespace}
	 * instance (with underlying {@link Node}).
	 * @param name the name for the namespace.
	 * @param allowCreate if {@code true} and no namespace by the given
	 * {@code name} exists then it is created.
	 * @return the {@link MetaStructureNamespace} in this namespace with the
	 * given {@code name}.
	 */
	public MetaStructureNamespace getNamespace( String name,
		boolean allowCreate )
	{
		assert name != null;
		return findOrCreateInCollection( getNamespaces(), name, allowCreate,
			MetaStructureNamespace.class );
	}
	
	/**
	 * @return the global namespace (without a name) which always exists.
	 * It's actually created on demand the first time. A call to
	 * {@link MetaStructureNamespace#getName()} will fail for this namespace.
	 */
	public MetaStructureNamespace getGlobalNamespace()
	{
		return findOrCreateInCollection( getNamespaces(), null, true,
			MetaStructureNamespace.class );
	}
	
	protected <T extends MetaStructureObject> T findOrCreateInCollection(
		Collection<T> collection, String nameOrNullForGlobal,
		boolean allowCreate, Class<T> theClass )
	{
		Transaction tx = neo().beginTx();
		try
		{
			for ( T item : collection )
			{
				String theName = item.getName();
				if ( nameOrNullForGlobal == null || theName == null )
				{
					if ( nameOrNullForGlobal == null && theName == null )
					{
						return item;
					}
				}
				else if ( theName.equals( nameOrNullForGlobal ) )
				{
					return item;
				}
			}
			
			if ( !allowCreate )
			{
				return null;
			}
			Node node = neo().createNode();
			T item = null;
			try
			{
				item = theClass.getConstructor( MetaStructure.class,
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
	
	/**
	 * @return a modifiable collection of all {@link MetaStructureNamespace}
	 * instances for this meta model.
	 */
	public Collection<MetaStructureNamespace> getNamespaces()
	{
		return new MetaStructureObjectCollection<MetaStructureNamespace>(
			rootNode(), MetaStructureRelTypes.META_NAMESPACE,
			Direction.OUTGOING, this, MetaStructureNamespace.class );
	}
	
	/**
	 * Looks up a value from the meta model, considering restrictions and
	 * hierarchy.
	 * @param <T> the type of the returned value.
	 * @param property the property to get a value from (also considering
	 * restrictions).
	 * @param finder the value finder for a specific value, f.ex.
	 * minimum cardinality.
	 * @param classes the classes to look in.
	 * @return the found value or {@code null} if no value was found.
	 */
	public <T> T lookup( MetaStructureProperty property, LookerUpper<T> finder,
		MetaStructureClass... classes )
	{
		// TODO Maybe add some form of caching here since this method will be
		// HEAVILY used. It's the main way of looking things up in the meta
		// model, f.ex. validation and conversion of values a.s.o.
		
		Transaction tx = neo().beginTx();
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
	
	/**
	 * Looks up the min cardinality property.
	 */
	public static LookerUpper<Integer> LOOKUP_MIN_CARDINALITY =
		new LookerUpper<Integer>()
	{
		public Integer get( MetaStructureRestrictable restrictable )
		{
			return restrictable.getMinCardinality();
		}
	};

	/**
	 * Looks up the max cardinality property.
	 */
	public static LookerUpper<Integer> LOOKUP_MAX_CARDINALITY =
		new LookerUpper<Integer>()
	{
		public Integer get( MetaStructureRestrictable restrictable )
		{
			return restrictable.getMaxCardinality();
		}
	};

	/**
	 * Looks up the property range property.
	 */
	public static LookerUpper<PropertyRange> LOOKUP_PROPERTY_RANGE =
		new LookerUpper<PropertyRange>()
	{
		public PropertyRange get( MetaStructureRestrictable restrictable )
		{
			return restrictable.getRange();
		}
	};
}
