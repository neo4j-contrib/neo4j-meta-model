package org.neo4j.meta;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.util.Link;

public class MetaRelationshipImpl extends MetaNodeWrapper implements
    MetaRelationship
{
	private static final String PROPERTY_KEY_MAX_CARDINALITY =
		"max_cardinality";
	private static final String PROPERTY_KEY_NAME_OF_TYPE = "name_of_type";
	
	public MetaRelationshipImpl( Node underlyingNode, MetaManager metaManager )
	{
		super( underlyingNode, metaManager );
	}
	
	public String getNameOfType()
	{
		return ( String ) getPropertyFromNode( PROPERTY_KEY_NAME_OF_TYPE );
	}

	void setNameOfType( String nameOfType )
	{
		setPropertyOnNode( PROPERTY_KEY_NAME_OF_TYPE, nameOfType );
	}
	
	private Link<NodeType> targetNodeTypeLink()
	{
		return new MetaLinkImpl(
			getMetaManager(),
			getUnderlyingNode(),
			MetaRelTypes.META_NODE_TYPE_VIA_REL_TO_NODE_TYPE,
			Direction.OUTGOING,
			NodeTypeImpl.class );
	}
	
	public NodeType getTargetNodeType()
	{
		return targetNodeTypeLink().get();
	}
	
	public void setTargetNodeType( NodeType nodeType )
	{
		targetNodeTypeLink().set( nodeType );
	}
	
	public int getMaxCardinality()
	{
		return ( Integer ) getPropertyFromNode( PROPERTY_KEY_MAX_CARDINALITY );
	}
	
	public void setMaxCardinality(
		int maxCardinalityOrMinusOneIfItDoesntMatter )
	{
		if ( maxCardinalityOrMinusOneIfItDoesntMatter == -1 )
		{
			getUnderlyingNode().removeProperty( PROPERTY_KEY_MAX_CARDINALITY );
		}
		else
		{
			setPropertyOnNode( PROPERTY_KEY_MAX_CARDINALITY,
				maxCardinalityOrMinusOneIfItDoesntMatter );
		}
	}

	@Override
    protected void cascadingDelete()
    {
	    if ( targetNodeTypeLink().has() )
	    {
	    	targetNodeTypeLink().remove();
	    }
	    super.cascadingDelete();
    }

}
