package org.neo4j.meta;

import java.util.Collection;
import java.util.Collections;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

public class MetaManager
{
	private final NeoService embeddedNeo;
	private final NeoUtil neoUtil;
	
	public MetaManager( NeoService embeddedNeo )
	{
		Transaction tx = Transaction.begin();
		try
		{	
			this.embeddedNeo = embeddedNeo;
			this.neoUtil = new NeoUtil( embeddedNeo );
			ensureMetaNodeSpace();	
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void ensureMetaNodeSpace()
	{
        Transaction tx = Transaction.begin();
		try
		{
			if ( getMetaRoot() == null )
			{
				getNeo().getReferenceNode().createRelationshipTo(
				    getNeo().createNode(),
				    MetaRelTypes.META_SUBREFERENCE_ROOT );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	
	NeoService getNeo()
	{
		return this.embeddedNeo;
	}
	
	NeoUtil getNeoUtil()
	{
		return this.neoUtil;
	}
	
	private Node getMetaRoot()
	{
		Transaction tx = Transaction.begin();
		try
		{
			Node referenceNode = getNeo().getReferenceNode();
			Relationship referenceRel = referenceNode.getSingleRelationship(
			    MetaRelTypes.META_SUBREFERENCE_ROOT, Direction.OUTGOING );
			Node metaRoot = null;
			if ( referenceRel != null )
			{
				metaRoot = referenceRel.getOtherNode( referenceNode );
			}
			tx.success();
			return metaRoot;
		}
		finally
		{
			tx.finish();
		}				
	}
	
	public NodeType createNodeType( String name )
	{
		if ( name == null )
		{
			throw new IllegalArgumentException( "Can't create a node type " +
				"with null name" );
		}
		
		Transaction tx = Transaction.begin();
		try
		{
			if ( getNodeTypeByNameOrNull( name ) != null )
			{
				throw new IllegalArgumentException( "A node type named '" +
					name + "' already exists" );
			}
			NodeType newNodeType = this.createAndAddNodeType( name );
			tx.success();
			return newNodeType;
		}
		finally
		{
			tx.finish();
		}		
	}
	
	// Assumes it's in tx
	private NodeType createAndAddNodeType( String name )
	{
		NodeTypeImpl newNodeType = new NodeTypeImpl(
			getNeo().createNode(), this );
		newNodeType.setName( name );
		nodeTypeSet().add( newNodeType );
		return newNodeType;
	}
	
	// Package private so for example NodeTypeImpl can invoke it and delete
	// the rel that connects the meta root to its underlying node (via the
	// Collection.remove() method)
	Collection<NodeType> nodeTypeSet()
	{
		return new MetaWrapperRelationshipSet(
			this,
			getMetaRoot(),
			MetaRelTypes.META_ROOT_TO_NODE_TYPE,
			Direction.OUTGOING,
			NodeTypeImpl.class,
			MetaWrapperRelationshipSet.AssociationLevel.AGGREGATE );
	}
	
	public Collection<NodeType> getNodeTypes()
	{
		return Collections.unmodifiableCollection( nodeTypeSet() );
	}
	
	// Linear lookup for now (maybe always -- we may be O(n) but n will almost
	// certainly always be < 100
	private NodeType getNodeTypeByNameOrNull( String name )
	{
		for ( NodeType type : getNodeTypes() )
		{
			if ( type.getName().equals( name ) )
			{
				return type;
			}
		}
		return null;
	}
	
	public boolean hasNodeTypeByName( String name )
	{
		return getNodeTypeByNameOrNull( name ) != null;
	}
	
	public NodeType getNodeTypeByName( String name )
    {
		NodeType nodeType = getNodeTypeByNameOrNull( name );
		if ( nodeType == null )
		{
			throw new IllegalArgumentException( "No node type with name '" +
				name + "' exists" );
		}
		return nodeType;
    }
	
	public NodeType getNodeTypeByNode( Node node )
	{
		NodeType nodeType = new NodeTypeImpl( node, this );
		if ( !nodeTypeSet().contains( nodeType ) )
		{
			throw new IllegalArgumentException( "Node " + node +
				" doesn't represent a NodeType" );
		}
		return nodeType;
	}

	public NodeCapsule getNodeCapsule( Node underlyingNode )
	{
		return new NodeCapsule( underlyingNode );
	}
}
