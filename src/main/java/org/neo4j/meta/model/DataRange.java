package org.neo4j.meta.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents a collection of values as the range so that values of this
 * type has to "equals" one of those values.
 */
public class DataRange extends RdfDatatypeRange
{
	private static final String KEY_DATA_ARRAY = "data_array";
	
	private Collection<Object> values;
	
	/**
	 * @param datatype the rdf datatype of the values.
	 * @param values the possible values.
	 */
	public DataRange( String datatype, Object... values )
	{
		super( datatype );
		this.values = new HashSet<Object>( Arrays.asList( values ) );
	}
	
	/**
	 * Used internally.
	 */
	public DataRange()
	{
	}
	
	/**
	 * @return the values.
	 */
	public Collection<Object> getValues()
	{
		return Collections.unmodifiableCollection( this.values );
	}
	
	@Override
	protected void internalLoad( MetaModelRestrictable<PropertyRange> owner )
	{
		super.internalLoad( owner );
		String[] dataArray = ( String[] )
			owner.node().getProperty( KEY_DATA_ARRAY, new String[] {} );
		this.values = new HashSet<Object>();
		for ( String value : dataArray )
		{
			try
			{
				this.values.add( rdfLiteralToJavaObject( value ) );
			}
			catch ( ParseException e )
			{
				throw new RuntimeException( e );
			}
		}
	}
	
	@Override
	protected void internalStore( MetaModelRestrictable<PropertyRange> owner )
	{
		super.internalStore( owner );
		String[] dataArray = new String[ this.values.size() ];
		int i = 0;
		for ( Object value : this.values )
		{
			dataArray[ i++ ] = javaObjectToRdfLiteral( value );
		}
		owner.node().setProperty( KEY_DATA_ARRAY, dataArray );
	}
	
	@Override
	protected void internalRemove( MetaModelRestrictable<PropertyRange> owner )
	{
		super.internalRemove( owner );
		owner.node().removeProperty( KEY_DATA_ARRAY );
	}
	
	@Override
	public boolean isDatatype()
	{
		return true;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getRdfDatatype() + ": " +
			StringUtil.join( ", ", getValues().toArray() ) + "]";
	}
}
