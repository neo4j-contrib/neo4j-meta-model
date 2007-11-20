package org.neo4j.meta;

public interface MetaProperty
{	
	public String getKey();
	public Class<?> getValueType();
	public void setValueType( Class<?> valueTypeOrNullIfItDoesntMatter );
}
