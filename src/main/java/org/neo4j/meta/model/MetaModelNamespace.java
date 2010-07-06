package org.neo4j.meta.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Represents a namespace in the meta model structure. A namespace is useful
 * when there would be conflicting names of properties or classes.
 */
public class MetaModelNamespace extends MetaModelObject
{
    private Map<String, MetaModelClass> classCache = Collections.synchronizedMap( new HashMap<String, MetaModelClass>() );
    private Map<String, MetaModelProperty> propertyCache = Collections.synchronizedMap( new HashMap<String, MetaModelProperty>() );
    private Map<String, MetaModelRelationship> relationshipTypeCache = Collections.synchronizedMap( new HashMap<String, MetaModelRelationship>() );

    /**
     * @param model the {@link MetaModel} instance.
     * @param node the {@link Node} to wrap.
     */
    public MetaModelNamespace( MetaModel model, Node node )
    {
        super( model, node );
    }

    /**
     * Returns (and optionally creates) a {@link MetaModelClass} instance (with
     * underlying {@link Node}).
     * 
     * @param name the name of the class.
     * @param allowCreate if {@code true} and no class by the given {@code name}
     *            exists then it is created.
     * @return the {@link MetaModelClass} in this namespace with the given
     *         {@code name}.
     */
    public MetaModelClass getMetaClass( String name, boolean allowCreate )
            throws DuplicateNameException
    {
        return ( (MetaModelImpl) model() ).findOrCreateInCollection(
                getMetaClasses(), name, allowCreate, MetaModelClass.class,
                classCache );
    }

    /**
     * @return a modifiable collection of all {@link MetaModelClass} instances
     *         for this namespace.
     */
    public Collection<MetaModelClass> getMetaClasses()
    {
        return new ObjectCollection<MetaModelClass>( node(),
                MetaModelRelTypes.META_CLASS, Direction.OUTGOING, model(),
                MetaModelClass.class );
    }

    /**
     * Returns (and optionally creates) a {@link MetaModelProperty} instance
     * (with underlying {@link Node}).
     * 
     * @param name the name of the property.
     * @param allowCreate if {@code true} and no property by the given {@code
     *            name} exists then it is created.
     * @return the {@link MetaModelProperty} in this namespace with the given
     *         {@code name}.
     */
    public MetaModelProperty getMetaProperty( String name, boolean allowCreate )
            throws DuplicateNameException
    {
        return ( (MetaModelImpl) model() ).findOrCreateInCollection(
                getMetaProperties(), name, allowCreate,
                MetaModelProperty.class, propertyCache );
    }

    /**
     * Returns (and optionally creates) a {@link MetaModelRelationship} instance
     * (with underlying {@link Node}).
     * 
     * @param name the name of the property.
     * @param allowCreate if {@code true} and no property by the given {@code
     *            name} exists then it is created.
     * @return the {@link MetaModelRelationship} in this namespace with the
     *         given {@code name}.
     */
    public MetaModelRelationship getMetaRelationship( String name,
            boolean allowCreate ) throws DuplicateNameException
    {
        return ( (MetaModelImpl) model() ).findOrCreateInCollection(
                getMetaRelationships(), name, allowCreate,
                MetaModelRelationship.class, relationshipTypeCache );
    }

    /**
     * Renames an instance of a {@link MetaModelObject}
     * 
     * @param oldName the current name of the meta model object.
     * @param newName the new name for the meta model object.
     */
    public void rename( String oldName, String newName )
    {
        Node metaObjectNode = indexService().getSingleNode( KEY_NAME, oldName );
        if ( metaObjectNode == null )
            throw new RuntimeException(
                    "Attempt to rename non-existing meta object" );
        else
        {
            if ( indexService().getSingleNode( KEY_NAME, newName ) != null )
                throw new RuntimeException(
                        "Attempt to rename meta object to an existing name" );
            else
            {
                metaObjectNode.setProperty( KEY_NAME, newName );
                indexService().removeIndex( metaObjectNode, KEY_NAME );
                indexService().index( metaObjectNode, KEY_NAME, newName );
                classCache.remove( oldName );
                classCache.put( newName, new MetaModelClass( model(),
                        metaObjectNode ) );
            }
        }
    }

    /**
     * Removes a {@link MetaModelObject}
     * 
     * @param name the name of the meta model object.
     * @param forced if {@code true} meta model object will be removed including
     *            all its {@link Relationship}s If (@code false) an exception
     *            will be thrown when the meta model object has
     *            {@link Relationship}s
     */
    public void remove( String name, Boolean forced )
    {
        Node metaObjectNode = indexService().getSingleNode( KEY_NAME, name );
        if ( forced )
        {
            for ( Relationship rel : metaObjectNode.getRelationships() )
            {
                rel.delete();
            }
            for ( String key : metaObjectNode.getPropertyKeys() )
            {
                metaObjectNode.removeProperty( key );
            }
            metaObjectNode.delete();
        }
        else
        {
            if ( metaObjectNode.hasRelationship() )
            {
                throw new RuntimeException(
                        "Meta object cannot be removed, it has relationships with other meta objects" );
            }
            else
            {
                for ( String key : metaObjectNode.getPropertyKeys() )
                {
                    metaObjectNode.removeProperty( key );
                }
                metaObjectNode.delete();
            }
        }
    }

    /**
     * @return a modifiable collection of all {@link MetaModelProperty}
     *         instances for this namespace.
     */
    public Collection<MetaModelProperty> getMetaProperties()
    {
        return new ObjectCollection<MetaModelProperty>( node(),
                MetaModelRelTypes.META_PROPERTY, Direction.OUTGOING, model(),
                MetaModelProperty.class );
    }

    /**
     * @return a modifiable collection of all {@link MetaModelRelationship}
     *         instances for this namespace.
     */
    public Collection<MetaModelRelationship> getMetaRelationships()
    {
        return new ObjectCollection<MetaModelRelationship>( node(),
                MetaModelRelTypes.META_RELATIONSHIP, Direction.OUTGOING,
                model(), MetaModelRelationship.class );
    }

    @Override
    public String toString()
    {
        String name = (String) node().getProperty( KEY_NAME, "GLOBAL" );
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
