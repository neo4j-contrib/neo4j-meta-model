package org.neo4j.meta;

import java.util.Collection;
import org.neo4j.api.core.Direction;

public interface NodeType
{
	/**
	 * The unique name of this type.
	 * @return the name of this type
	 */
	public String getName();
	
	/**
	 * The version of this type, unused as of now.
	 * @return the (unused) version of this type
	 */
	public int getVersion();

	/**
	 * Accessor for this node type's direct super types. Rationale for direct:
	 * <ul>
	 * 	<li>This method is used for injecting types in the type hierarchy.
	 * (I.e. <code>nodeType.directSuperType().add( newSuperType );</code>)
	 * If it had returned all the transitive super types, then another API would
	 * have been needed for that.</li>
	 * 	<li>Any way, you're usually not interested in getting a node's all
	 * transitive types. The most common use case for that is actually in order
	 * to figure out property or relationship constraints, and the type system
	 * should help you with that. For that, we provide the non-direct
	 * relationship and property accessor methods (for example
	 * <code>getRequiredProperties()</code>).</li>
	 * </ul>
	 */ 
	public Collection<NodeType> directSuperTypes();
	/**
	 * Accessor for this node type's direct sub types. Why direct? See
	 * {@link #directSuperTypes()}.
	 */ 
	public Collection<NodeType> directSubTypes();
	/**
	 * Returns <code>true</code> if the passed-in type is a (transitive)
	 * subtype of this type. For checking direct subtypes, use {@link
	 * #directSubTypes()}.
	 */
	public boolean isSubTypeOf( NodeType potentialSuperType );
	
	/**
	 * Adds a required property with the given key to this type.
	 * @param key the unique key
	 * @throws IllegalArgumentException if there's already a property with
	 * the given key in this class, in any of its supertypes or in any of its
	 * subtypes
	 * @return the new property
	 */
	public MetaProperty addRequiredProperty( String key );
	/**
	 * Removes the property, if it's directly attached to this type.
	 * @param property the property that will be removed
	 * @return whether the property was removed
	 */
	public boolean removeRequiredProperty( MetaProperty property );	
	/**
	 * Returns the property with a given key, if the property is defined in
	 * type or any of its (transitive) super types, or else <code>null</code>.
	 * @param key the key of the property
	 * @return the property with a given key, if it's a transitively required
	 * property for this type
	 */
	public MetaProperty getRequiredProperty( String key );	
	/**
	 * Returns all properties that are required for nodes of this type. It's
	 * transitive, see {@link #getRequiredProperty(String)}.
	 * return all properties that are required for nodes of this type
	 */
	public Iterable<MetaProperty> getRequiredProperties();
	/**
	 * Returns the property constraints that are defined by this very type.
	 */
	public Iterable<MetaProperty> getDirectRequiredProperties();
	
	/**
	 * Adds a relationship type as allowed from this node type in a specific
	 * direction to another node type. It will be rejected with an
	 * <code>IllegalArgumentException</code> if there's already an existing
	 * relationship of the same type and direction (but a different node type).
	 * If the user wants to model that, they will have to inject as a super
	 * type of all the end point node types.
	 */
	public MetaRelationship addAllowedRelationship( String type,
		Direction direction, NodeType targetNodeType );
	
	/**
	 * Removes a meta relationship, if it's directly attached to this node. If
	 * the relationship was removed, this method returns <code>true</code>.
	 * After this method has successfully completed, it's invalid to invoke
	 * any methods on the relationship.
	 * @param relationship the relationship to remove
	 * @return whether the operation was successful
	 */
	public boolean removeAllowedRelationship( MetaRelationship relationship );
	
	/**
	 * Get the meta relationship representing a relationship with a type name
	 * of <code>type</code> going in <code>direction</code> from this node
	 * type, or <code>null</code> if nothing exists.
	 * @param type the name of the relationship type that this meta relationship
	 * represents
	 * @param direction the direction
	 * @return the meta relationship
	 */
	public MetaRelationship getAllowedRelationship( String type,
		Direction direction );
	
	/**
	 * Get all relationship types that can be attached to nodes of this type
	 * in the given direction (including those specified by super types).
	 * @param direction the direction
	 * @return all relationship types in the given direction
	 */
	public Iterable<MetaRelationship> getAllowedRelationships(
		Direction direction );
	
	/**
	 * Get the relationship types in the given direction that are specified
	 * by exactly this type (and not by a super type).
	 * @param direction the direction
	 * @return the relationship types directly attached to this node type
	 */
	public Iterable<MetaRelationship> getDirectAllowedRelationships(
		Direction direction );
	
	/**
	 * Deletes this node type by detaching it from all its subtypes and
	 * supertypes and deleting the meta relationships and meta properties that
	 * are attached to this very node type (not to its super types).
	 */
	public void delete();
}
