package org.neo4j.meta.model;

import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.util.RelationshipSet;

/**
 * An implementation of a {@link Collection} which handles {@link Node}s
 * representing instance which complies to a {@link MetaModelClass}.
 */
public class InstanceCollection extends RelationshipSet<Node>
{
	private static final String KEY_COUNT = "instance_count";
	
	private MetaModel model;
	
	/**
	 * 
	 * @param node the {@link Node} which holds the relationships.
	 * @param model the {@link MetaModel} instance.
	 */
	public InstanceCollection( GraphDatabaseService graphDb,
		Node node, MetaModel model )
	{
		super( graphDb, node, MetaModelRelTypes.META_IS_INSTANCE_OF,
			Direction.INCOMING );
		this.model = model;
	}
	
	@Override
	protected Node getNodeFromItem( Object item )
	{
		return ( Node ) item;
	}
	
	@Override
	protected Node newObject( Node node, Relationship rel )
	{
		return node;
	}
	
//	@Override
//	public boolean add( Node item )
//	{
//		Transaction tx = meta.neo().beginTx();
//		try
//		{
//			boolean result = super.add( item );
//			if ( result )
//			{
//				int count = size() + 1;
//				getUnderlyingNode().setProperty( KEY_COUNT, count );
//			}
//			tx.success();
//			return result;
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
//	
//	@Override
//	protected void removeItem( Relationship rel )
//	{
//		super.removeItem( rel );
//		int count = size() - 1;
//		getUnderlyingNode().setProperty( KEY_COUNT, count );
//	}
//	
//	@Override
//	public int size()
//	{
//		return ( ( Number ) meta.neoUtil().getProperty(
//			getUnderlyingNode(), KEY_COUNT, 0 ) ).intValue();
//	}
}
