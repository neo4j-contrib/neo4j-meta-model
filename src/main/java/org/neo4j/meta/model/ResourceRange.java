package org.neo4j.meta.model;

import java.text.ParseException;

public class ResourceRange extends PropertyRange
{
    private static final String KEY_RESOURCE_ID = "resource_id";
    
    private String resourceId;
    
    public ResourceRange( String resourceId )
    {
        this.resourceId = resourceId;
    }
    
    @Override
    protected void internalLoad( MetaModelRestrictable<PropertyRange> owner )
    {
        resourceId = ( String ) owner.node().getProperty( KEY_RESOURCE_ID );
    }

    @Override
    protected void internalRemove( MetaModelRestrictable<PropertyRange> owner )
    {
    }

    @Override
    protected void internalStore( MetaModelRestrictable<PropertyRange> owner )
    {
        owner.node().setProperty( KEY_RESOURCE_ID, resourceId );
    }

    @Override
    public boolean isDatatype()
    {
        return false;
    }

    @Override
    public String javaObjectToRdfLiteral( Object value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object rdfLiteralToJavaObject( String value ) throws ParseException
    {
        throw new UnsupportedOperationException();
    }
}
