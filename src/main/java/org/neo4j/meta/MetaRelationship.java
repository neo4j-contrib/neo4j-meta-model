package org.neo4j.meta;

public interface MetaRelationship
{
	// Mandatory
	public String getNameOfType();
	public NodeType getTargetNodeType();
	
	// Optional
	public int getMaxCardinality();
	public void setMaxCardinality(
		int maxCardinalityOrMinusOneIfItDoesntMatter );
}
