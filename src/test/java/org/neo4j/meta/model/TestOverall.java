package org.neo4j.meta.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.meta.MetaTestCase;
import org.neo4j.meta.model.ClassRange;
import org.neo4j.meta.model.DataRange;
import org.neo4j.meta.model.DatatypeClassRange;
import org.neo4j.meta.model.PropertyLookerUpper;
import org.neo4j.meta.model.MetaModel;
import org.neo4j.meta.model.MetaModelClass;
import org.neo4j.meta.model.MetaModelImpl;
import org.neo4j.meta.model.MetaModelNamespace;
import org.neo4j.meta.model.MetaModelProperty;
import org.neo4j.meta.model.MetaModelRestrictable;
import org.neo4j.meta.model.MetaModelPropertyRestriction;
import org.neo4j.meta.model.RdfUtil;

public class TestOverall extends MetaTestCase
{
    /**
     * Tests some basic stuff.
     */
    @Test
	public void testSome()
	{
		MetaModel structure = new MetaModelImpl( graphDb(), indexService() );
		assertEquals( 0, structure.getNamespaces().size() );
		MetaModelNamespace namespace = structure.getGlobalNamespace();
		assertNull( namespace.getName() );
		assertEquals( 1, structure.getNamespaces().size() );
		assertEquals( namespace, structure.getGlobalNamespace() );
		
		assertNull( structure.getNamespace( "something",
			false ) );
		MetaModelNamespace anotherNamespace = structure.getNamespace(
			"something", true );
		assertNotNull( anotherNamespace );
		
		assertEquals( 0, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		assertNull( namespace.getMetaClass( "http://test#Thing", false ) );
		MetaModelClass thingClass =
			namespace.getMetaClass( "http://test#Thing", true );
		assertNotNull( thingClass );
		assertNotNull( namespace.getMetaClass( "http://test#Thing", false ) );
		assertEquals( 1, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		
		MetaModelClass phoneClass =
			namespace.getMetaClass( "http://test#Phone", true );
		assertEquals( 2, namespace.getMetaClasses().size() );
		thingClass.getDirectSubs().add( phoneClass );
		MetaModelProperty phoneTypeProperty =
			namespace.getMetaProperty( "http://test#phoneType", true );
		phoneTypeProperty.setRange( new DataRange(
			RdfUtil.NS_XML_SCHEMA + "string", "home", "work", "cell" ) );
		assertCollection( ( ( DataRange ) phoneTypeProperty.getRange() ).
			getValues(), "home", "work", "cell" );
		assertEquals( 1, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneTypeProperty );
		assertCollection( phoneTypeProperty.associatedMetaPropertyContainers(),
			phoneClass );
		
		MetaModelProperty phoneNumberProperty =
			namespace.getMetaProperty( "http://test#phoneNumber", true );
		assertEquals( 2, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneNumberProperty );
		
		MetaModelClass personClass =
			namespace.getMetaClass( "http://test#Person", true );
		personClass.getDirectSupers().add( thingClass );
		MetaModelProperty nameProperty =
			namespace.getMetaProperty( "http://test#name", true );
		nameProperty.setRange( new DatatypeClassRange( String.class ) );
		assertEquals( DatatypeClassRange.class,
			nameProperty.getRange().getClass() );
		assertEquals( String.class, ( ( DatatypeClassRange )
			nameProperty.getRange() ).getRangeClass() );
		MetaModelProperty givenNameProperty =
			namespace.getMetaProperty( "http://test#givenName", true );
		nameProperty.getDirectSubs().add( givenNameProperty );
		MetaModelRelationship phoneProperty =
			namespace.getMetaRelationship( "http://test#phone", true );
		phoneProperty.setRange( new ClassRange( phoneClass ) );
		assertEquals( phoneProperty.getName(), ( ( ClassRange )
			phoneProperty.getRange() ).getRelationshipTypeToUse().name() );
		doTestRelationshipTypeRestrictable( structure, phoneProperty );
		personClass.getDirectProperties().add( givenNameProperty );
		
		MetaModelClass userClass =
			namespace.getMetaClass( "http://test#User", true );
		personClass.getDirectSubs().add( userClass );
		assertCollection( userClass.getDirectProperties() );
		assertCollection( userClass.getAllProperties(), givenNameProperty );
		
		assertTrue( personClass.isSubOf( thingClass ) );
		assertTrue( userClass.isSubOf( personClass ) );
		assertTrue( userClass.isSubOf( thingClass ) );
		assertFalse( userClass.isSubOf( phoneClass ) );
		
		// Direct instances
		Node person1 = graphDb().createNode();
		Node person2 = graphDb().createNode();
		assertCollection( personClass.getDirectInstances() );
		personClass.getDirectInstances().add( person1 );
		assertCollection( personClass.getDirectInstances(), person1 );
		personClass.getDirectInstances().add( person2 );
		assertCollection( personClass.getDirectInstances(), person1, person2 );
		
		personClass.getDirectInstances().remove( person2 );
		assertCollection( personClass.getDirectInstances(), person1 );
		graphDb().getNodeById( person2.getId() );
		personClass.getDirectInstances().add( person2 );
		
		// All instances (recursive)
		assertCollection( personClass.getAllInstances(), person1, person2 );
		Node user1 = graphDb().createNode();
		userClass.getDirectInstances().add( user1 );
		assertCollection( personClass.getAllInstances(), person1, person2, user1 );
		assertCollection( userClass.getDirectInstances(), user1 );
        assertCollection( userClass.getAllInstances(), user1 );

        //Instance range
		MetaModelClass adminClass =
			namespace.getMetaClass( "http://test#Admin", true );
		userClass.getDirectSubs().add( adminClass );
		Node admin1 = graphDb().createNode();
		Node admin2 = graphDb().createNode();
		InstanceRange iRange = new InstanceEnumerationRange(admin1, admin2); 
        adminClass.setRange(iRange);
        assertCollection( adminClass.getAllInstances(), admin1, admin2);		
        
		deleteMetaModel();
	}
	
	/**
	 * Tests extended functionality, like OWL constructs.
	 */
    @Test
	public void testExtended()
	{
		MetaModel structure = new MetaModelImpl( graphDb(), indexService() );
		MetaModelNamespace namespace = structure.getGlobalNamespace();
		MetaModelProperty maker = namespace.getMetaProperty(
			"http://test.org/test#maker", true );
		MetaModelProperty madeBy = namespace.getMetaProperty(
			"http://test.org/test#madeBy", true );
		assertNull( maker.getInverseOf() );
		assertNull( madeBy.getInverseOf() );
		maker.setInverseOf( madeBy );
		assertEquals( maker, madeBy.getInverseOf() );
		assertEquals( madeBy, maker.getInverseOf() );
		madeBy.setInverseOf( maker );
		assertEquals( maker, madeBy.getInverseOf() );
		assertEquals( madeBy, maker.getInverseOf() );
		maker.setInverseOf( null );
		assertNull( maker.getInverseOf() );
		assertNull( madeBy.getInverseOf() );
		deleteMetaModel();
	}

	/**
	 * Tests restrictions.
	 */
    @Test
    public void testRestrictions()
	{
		MetaModel structure = new MetaModelImpl( graphDb(), indexService() );
		MetaModelNamespace namespace = structure.getGlobalNamespace();
		MetaModelClass thing = namespace.getMetaClass( "thing", true );
		MetaModelClass person = namespace.getMetaClass( "person", true );
		MetaModelClass user = namespace.getMetaClass( "user", true );
		thing.getDirectSubs().add( person );
		person.getDirectSubs().add( user );
		
		MetaModelProperty name = namespace.getMetaProperty( "name", true );
		MetaModelProperty nickName =
			namespace.getMetaProperty( "nickname", true );
		nickName.getDirectSupers().add( name );
		MetaModelPropertyRestriction personNickNameRestriction =
		    person.getRestriction( nickName, true );
		assertCollection( thing.getDirectPropertyRestrictions() );
		assertCollection( thing.getAllPropertyRestrictions() );
		assertCollection( person.getDirectPropertyRestrictions(),
		    personNickNameRestriction );
		assertCollection( person.getAllPropertyRestrictions(),
		    personNickNameRestriction );
		assertCollection( user.getDirectPropertyRestrictions() );
		assertCollection( user.getAllPropertyRestrictions(),
		    personNickNameRestriction );
		person.getDirectProperties().add( name );
		MetaModelPropertyRestriction personNameRestriction =
			person.getRestriction( name, true );
		assertNotNull( personNameRestriction );
		assertCollection( person.getDirectPropertyRestrictions(),
			personNameRestriction, personNickNameRestriction );
		assertCollection( person.getAllPropertyRestrictions(),
			personNameRestriction, personNickNameRestriction );
		assertCollection( user.getDirectPropertyRestrictions() );
		assertCollection( user.getAllPropertyRestrictions(), personNameRestriction,
		    personNickNameRestriction );
		MetaModelPropertyRestriction userNameRestriction =
			user.getRestriction( name, true );
		assertNotNull( userNameRestriction );
		assertCollection( user.getDirectPropertyRestrictions(), userNameRestriction );
		assertCollection( user.getAllPropertyRestrictions(), personNameRestriction,
			userNameRestriction, personNickNameRestriction );
		MetaModelPropertyRestriction userNickNameRestriction =
			user.getRestriction( nickName, true );
		assertNotNull( userNickNameRestriction );
		assertCollection( user.getDirectPropertyRestrictions(), userNameRestriction,
			userNickNameRestriction );
		assertCollection( user.getAllPropertyRestrictions(), personNameRestriction,
			userNameRestriction, userNickNameRestriction,
			personNickNameRestriction );
		doTestPropertyTypeRestrictable( structure, userNameRestriction );
		deleteMetaModel();
	}
	
	private void doTestRelationshipTypeRestrictable( MetaModel structure,
		MetaModelRestrictable<RelationshipRange> restrictable )
	{
		assertNull( restrictable.getMaxCardinality() );
		assertNull( restrictable.getMinCardinality() );
		restrictable.setMinCardinality( null );
		restrictable.setMaxCardinality( 10 );
		assertNull( restrictable.getMinCardinality() );
		assertEquals( 10, ( int ) restrictable.getMaxCardinality() );
		restrictable.setMinCardinality( 5 );
		assertEquals( 5, ( int ) restrictable.getMinCardinality() );
		assertNull( restrictable.getCollectionBehaviourClass() );
		restrictable.setCollectionBehaviourClass( List.class );
		assertEquals( List.class,
			restrictable.getCollectionBehaviourClass() );
		restrictable.setCollectionBehaviourClass( null );
		assertNull( restrictable.getCollectionBehaviourClass() );
		restrictable.setMinCardinality( null );
		restrictable.setMaxCardinality( null );
		assertNull( restrictable.getMaxCardinality() );
		assertNull( restrictable.getMinCardinality() );
		
		MetaModelClass testClass = structure.getGlobalNamespace().
			getMetaClass( "rangeTestClass", true );
		restrictable.setRange( new ClassRange( testClass ) );
		assertCollection( Arrays.asList( ( ( ClassRange )
			restrictable.getRange() ).getRangeClasses() ), testClass );
	}

	
	private void doTestPropertyTypeRestrictable( MetaModel structure,
			MetaModelRestrictable<PropertyRange> restrictable )
		{
			assertNull( restrictable.getMaxCardinality() );
			assertNull( restrictable.getMinCardinality() );
			restrictable.setMinCardinality( null );
			restrictable.setMaxCardinality( 10 );
			assertNull( restrictable.getMinCardinality() );
			assertEquals( 10, ( int ) restrictable.getMaxCardinality() );
			restrictable.setMinCardinality( 5 );
			assertEquals( 5, ( int ) restrictable.getMinCardinality() );
			assertNull( restrictable.getCollectionBehaviourClass() );
			restrictable.setCollectionBehaviourClass( List.class );
			assertEquals( List.class,
				restrictable.getCollectionBehaviourClass() );
			restrictable.setCollectionBehaviourClass( null );
			assertNull( restrictable.getCollectionBehaviourClass() );
			restrictable.setMinCardinality( null );
			restrictable.setMaxCardinality( null );
			assertNull( restrictable.getMaxCardinality() );
			assertNull( restrictable.getMinCardinality() );
			
			restrictable.setRange(
				new DatatypeClassRange( Float.class ) );
			assertEquals( Float.class, ( ( DatatypeClassRange )
				restrictable.getRange() ).getRangeClass() );
		}
	
	
	/**
	 * Tests the "lookup" functionality.
	 */
	@Test
    public void testLookup()
	{
		MetaModel meta = new MetaModelImpl( graphDb(), indexService() );
		MetaModelNamespace namespace = meta.getGlobalNamespace();
		
		// The classes
		MetaModelClass thing = namespace.getMetaClass( "thing", true );
		MetaModelClass organism =
			namespace.getMetaClass( "organism", true );
		MetaModelClass person = namespace.getMetaClass( "person", true );
		MetaModelClass user = namespace.getMetaClass( "user", true );
		MetaModelClass musicListener =
			namespace.getMetaClass( "musicListener", true );
		MetaModelClass song = namespace.getMetaClass( "song", true );
		
		// Class hierarchy
		organism.getDirectSupers().add( thing );
		person.getDirectSupers().add( organism );
		user.getDirectSupers().add( person );
		musicListener.getDirectSupers().add( person );
		
		// Properties
		MetaModelProperty size = namespace.getMetaProperty( "size", true );
		size.setRange( new DatatypeClassRange( Integer.class ) );
		MetaModelProperty age = namespace.getMetaProperty( "age", true );
		age.setRange( new DatatypeClassRange( Integer.class ) );
		MetaModelProperty name = namespace.getMetaProperty( "name", true );
		name.setRange( new DatatypeClassRange( String.class ) );
		MetaModelProperty nickName =
			namespace.getMetaProperty( "nickname", true );
		MetaModelProperty login =
			namespace.getMetaProperty( "login", true );
		MetaModelRelationship likes =
			namespace.getMetaRelationship( "likes", true );
		likes.setRange( new ClassRange( thing ) );
		
		// Property hierarchy
		login.getDirectSupers().add( name );
		nickName.getDirectSupers().add( name );
		
		// Domains and properties
		thing.getDirectProperties().add( size );
		organism.getDirectProperties().add( age );
		person.getDirectProperties().add( nickName );
		person.getDirectRelationships().add( likes );
		user.getDirectProperties().add( login );
		age.setCardinality( 1 );
		
		// Restrictions
		organism.getRestriction( size, true ).setCardinality( 1 );
		person.getRestriction( nickName, true ).setMinCardinality( 0 );
		person.getRestriction( nickName, false ).setMaxCardinality( 5 );
		user.getRestriction( login, true ).setCardinality( 1 );
		musicListener.getRestriction( likes, true ).setRange(
			new ClassRange( song ) );
		
		// Verify
		assertLookup( meta, size, MetaModel.LOOKUP_PROPERTY_MIN_CARDINALITY, null,
			thing );
		assertLookup( meta, size, MetaModel.LOOKUP_PROPERTY_MIN_CARDINALITY, 1,
			organism );
		assertLookup( meta, age, MetaModel.LOOKUP_PROPERTY_MAX_CARDINALITY, 1,
			thing, organism, person, user, musicListener, song );
		assertEquals( String.class, ( ( DatatypeClassRange ) meta.lookup(
			nickName, MetaModel.LOOKUP_PROPERTY_RANGE,
			user ) ).getRangeClass() );
		assertCollection( Arrays.asList( ( ( ClassRange )			
			meta.lookup( likes, MetaModel.LOOKUP_RELATIONSHIPTYPE_RANGE,
			musicListener ) ).getRangeClasses() ), song );
		assertCollection( Arrays.asList( ( ( ClassRange )
			meta.lookup( likes, MetaModel.LOOKUP_RELATIONSHIPTYPE_RANGE,
			person ) ).getRangeClasses() ), thing );
		
		deleteMetaModel();
	}
	
	private <T> void assertLookup( MetaModel meta,
		MetaModelProperty property, PropertyLookerUpper<T> finder, T expectedValue,
		MetaModelClass... classes )
	{
		T value = meta.lookup( property, finder, classes );
		if ( expectedValue == null )
		{
			assertNull( value );
		}
		else
		{
			assertEquals( expectedValue, value );
		}
	}

	private <T> void assertLookup( MetaModel meta,
			MetaModelRelationship relationshipType, RelationshipLookerUpper<T> finder, T expectedValue,
			MetaModelClass... classes )
		{
			T value = meta.lookup( relationshipType, finder, classes );
			if ( expectedValue == null )
			{
				assertNull( value );
			}
			else
			{
				assertEquals( expectedValue, value );
			}
		}
	
}
