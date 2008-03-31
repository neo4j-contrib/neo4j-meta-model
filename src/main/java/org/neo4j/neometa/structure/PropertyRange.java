package org.neo4j.neometa.structure;

import java.text.ParseException;

import org.neo4j.api.core.Transaction;

public abstract class PropertyRange
{
	static final String KEY_RANGE_TYPE = "range_type";
	
	protected void store( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			property.node().setProperty( KEY_RANGE_TYPE, getClass().getName() );
			internalStore( property );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected abstract void internalStore( MetaStructureProperty property );
	
	protected abstract void internalLoad( MetaStructureProperty property );
	
	protected static PropertyRange loadRange( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			String rangeType = ( String ) property.node().getProperty(
				KEY_RANGE_TYPE );
			Class<?> cls = Class.forName( rangeType );
			PropertyRange result = ( PropertyRange ) cls.newInstance();
			result.internalLoad( property );
			tx.success();
			return result;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			tx.finish();
		}
	}
	
	public abstract Object rdfLiteralToJavaObject( String value )
		throws ParseException;
	
	public abstract String javaObjectToRdfLiteral( Object value );
}
