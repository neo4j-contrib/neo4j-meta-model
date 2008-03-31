package org.neo4j.neometa.structure;

public class RdfDatatypeRange extends SimpleStringPropertyRange
{
	private String datatype;
	
	public RdfDatatypeRange( String datatype )
	{
		this.datatype = datatype;
	}
	
	public RdfDatatypeRange()
	{
	}
	
	public String getRdfDatatype()
	{
		return this.datatype;
	}
	
	@Override
	protected String toStringRepresentation( MetaStructureProperty property )
	{
		return this.datatype;
	}
	
	@Override
	protected void fromStringRepresentation( MetaStructureProperty property,
		String stringRepresentation )
	{
		this.datatype = stringRepresentation;
	}
}
