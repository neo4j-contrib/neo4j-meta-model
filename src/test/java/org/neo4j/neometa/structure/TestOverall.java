package org.neo4j.neometa.structure;

import java.util.Arrays;
import java.util.List;

import org.neo4j.api.core.Node;
import org.neo4j.neometa.MetaTestCase;

/**
 * Tests the meta structure.
 */
public class TestOverall extends MetaTestCase
{
	/**
	 * Some basic tests.
	 */
	public void testSome()
	{
		MetaStructure structure = new MetaStructure( neo() );
		assertEquals( 0, structure.getNamespaces().size() );
		MetaStructureNamespace namespace = structure.getGlobalNamespace();
		assertNull( namespace.getName() );
		assertEquals( 1, structure.getNamespaces().size() );
		assertEquals( namespace, structure.getGlobalNamespace() );
		
		assertNull( structure.getNamespace( "something",
			false ) );
		MetaStructureNamespace anotherNamespace = structure.getNamespace(
			"something", true );
		assertNotNull( anotherNamespace );
		
		assertEquals( 0, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		assertNull( namespace.getMetaClass( "http://test#Thing", false ) );
		MetaStructureClass thingClass =
			namespace.getMetaClass( "http://test#Thing", true );
		assertNotNull( thingClass );
		assertNotNull( namespace.getMetaClass( "http://test#Thing", false ) );
		assertEquals( 1, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		
		MetaStructureClass phoneClass =
			namespace.getMetaClass( "http://test#Phone", true );
		assertEquals( 2, namespace.getMetaClasses().size() );
		thingClass.getDirectSubs().add( phoneClass );
		MetaStructureProperty phoneTypeProperty =
			namespace.getMetaProperty( "http://test#phoneType", true );
		phoneTypeProperty.setRange( new DataRange(
			RdfUtil.NS_XML_SCHEMA + "string", "home", "work", "cell" ) );
		assertCollection( ( ( DataRange ) phoneTypeProperty.getRange() ).
			getValues(), "home", "work", "cell" );
		assertEquals( 1, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneTypeProperty );
		assertCollection( phoneTypeProperty.associatedMetaClasses(),
			phoneClass );
		
		MetaStructureProperty phoneNumberProperty =
			namespace.getMetaProperty( "http://test#phoneNumber", true );
		assertEquals( 2, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneNumberProperty );
		
		MetaStructureClass personClass =
			namespace.getMetaClass( "http://test#Person", true );
		personClass.getDirectSupers().add( thingClass );
		MetaStructureProperty nameProperty =
			namespace.getMetaProperty( "http://test#name", true );
		nameProperty.setRange( new DatatypeClassRange( String.class ) );
		assertEquals( DatatypeClassRange.class,
			nameProperty.getRange().getClass() );
		assertEquals( String.class, ( ( DatatypeClassRange )
			nameProperty.getRange() ).getRangeClass() );
		MetaStructureProperty givenNameProperty =
			namespace.getMetaProperty( "http://test#givenName", true );
		nameProperty.getDirectSubs().add( givenNameProperty );
		MetaStructureProperty phoneProperty =
			namespace.getMetaProperty( "http://test#phone", true );
		phoneProperty.setRange( new MetaStructureClassRange( phoneClass ) );
		assertEquals( phoneProperty.getName(), ( ( MetaStructureClassRange )
			phoneProperty.getRange() ).getRelationshipTypeToUse().name() );
		doTestRestrictable( structure, phoneProperty );
		personClass.getDirectProperties().add( givenNameProperty );
		
		MetaStructureClass userClass =
			namespace.getMetaClass( "http://test#User", true );
		personClass.getDirectSubs().add( userClass );
		assertCollection( userClass.getDirectProperties() );
		assertCollection( userClass.getAllProperties(), givenNameProperty );
		
		assertTrue( personClass.isSubOf( thingClass ) );
		assertTrue( userClass.isSubOf( personClass ) );
		assertTrue( userClass.isSubOf( thingClass ) );
		assertFalse( userClass.isSubOf( phoneClass ) );
		
		Node person1 = neo().createNode();
		Node person2 = neo().createNode();
		assertCollection( personClass.getInstances() );
		personClass.getInstances().add( person1 );
		assertCollection( personClass.getInstances(), person1 );
		personClass.getInstances().add( person2 );
		assertCollection( personClass.getInstances(), person1, person2 );
		
		personClass.getInstances().remove( person2 );
		assertCollection( personClass.getInstances(), person1 );
		neo().getNodeById( person2.getId() );
		deleteMetaModel();
	}
	
	/**
	 * Tests extended functionality, like OWL constructs.
	 */
	public void testExtended()
	{
		MetaStructure structure = new MetaStructure( neo() );
		MetaStructureNamespace namespace = structure.getGlobalNamespace();
		MetaStructureProperty maker = namespace.getMetaProperty(
			"http://test.org/test#maker", true );
		MetaStructureProperty madeBy = namespace.getMetaProperty(
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
	public void testRestrictions()
	{
		MetaStructure structure = new MetaStructure( neo() );
		MetaStructureNamespace namespace = structure.getGlobalNamespace();
		MetaStructureClass thing = namespace.getMetaClass( "thing", true );
		MetaStructureClass person = namespace.getMetaClass( "person", true );
		MetaStructureClass user = namespace.getMetaClass( "user", true );
		thing.getDirectSubs().add( person );
		person.getDirectSubs().add( user );
		
		MetaStructureProperty name = namespace.getMetaProperty( "name", true );
		MetaStructureProperty nickName =
			namespace.getMetaProperty( "nickname", true );
		nickName.getDirectSupers().add( name );
		try
		{
			person.getRestriction( nickName, true );
			fail( "Should've failed" );
		}
		catch ( Exception e )
		{ // Good
		}
		assertCollection( thing.getDirectRestrictions() );
		assertCollection( thing.getAllRestrictions() );
		assertCollection( person.getDirectRestrictions() );
		assertCollection( person.getAllRestrictions() );
		assertCollection( user.getDirectRestrictions() );
		assertCollection( user.getAllRestrictions() );
		person.getDirectProperties().add( name );
		MetaStructureRestriction personNameRestriction =
			person.getRestriction( name, true );
		assertNotNull( personNameRestriction );
		assertCollection( person.getDirectRestrictions(),
			personNameRestriction );
		assertCollection( person.getAllRestrictions(),
			personNameRestriction );
		assertCollection( user.getDirectRestrictions() );
		assertCollection( user.getAllRestrictions(), personNameRestriction );
		MetaStructureRestriction userNameRestriction =
			user.getRestriction( name, true );
		assertNotNull( userNameRestriction );
		assertCollection( user.getDirectRestrictions(), userNameRestriction );
		assertCollection( user.getAllRestrictions(), personNameRestriction,
			userNameRestriction );
		MetaStructureRestriction userNickNameRestriction =
			user.getRestriction( nickName, true );
		assertNotNull( userNickNameRestriction );
		assertCollection( user.getDirectRestrictions(), userNameRestriction,
			userNickNameRestriction );
		assertCollection( user.getAllRestrictions(), personNameRestriction,
			userNameRestriction, userNickNameRestriction );
		doTestRestrictable( structure, userNameRestriction );
		deleteMetaModel();
	}
	
	private void doTestRestrictable( MetaStructure structure,
		MetaStructureRestrictable restrictable )
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
		MetaStructureClass testClass = structure.getGlobalNamespace().
			getMetaClass( "rangeTestClass", true );
		restrictable.setRange( new MetaStructureClassRange( testClass ) );
		assertCollection( Arrays.asList( ( ( MetaStructureClassRange )
			restrictable.getRange() ).getRangeClasses() ), testClass );
	}
	
	/**
	 * Tests the "lookup" functionality.
	 */
	public void testLookup()
	{
		MetaStructure meta = new MetaStructure( neo() );
		MetaStructureNamespace namespace = meta.getGlobalNamespace();
		
		// The classes
		MetaStructureClass thing = namespace.getMetaClass( "thing", true );
		MetaStructureClass organism =
			namespace.getMetaClass( "organism", true );
		MetaStructureClass person = namespace.getMetaClass( "person", true );
		MetaStructureClass user = namespace.getMetaClass( "user", true );
		MetaStructureClass musicListener =
			namespace.getMetaClass( "musicListener", true );
		MetaStructureClass song = namespace.getMetaClass( "song", true );
		
		// Class hierarchy
		organism.getDirectSupers().add( thing );
		person.getDirectSupers().add( organism );
		user.getDirectSupers().add( person );
		musicListener.getDirectSupers().add( person );
		
		// Properties
		MetaStructureProperty size = namespace.getMetaProperty( "size", true );
		size.setRange( new DatatypeClassRange( Integer.class ) );
		MetaStructureProperty age = namespace.getMetaProperty( "age", true );
		age.setRange( new DatatypeClassRange( Integer.class ) );
		MetaStructureProperty name = namespace.getMetaProperty( "name", true );
		name.setRange( new DatatypeClassRange( String.class ) );
		MetaStructureProperty nickName =
			namespace.getMetaProperty( "nickname", true );
		MetaStructureProperty login =
			namespace.getMetaProperty( "login", true );
		MetaStructureProperty likes =
			namespace.getMetaProperty( "likes", true );
		likes.setRange( new MetaStructureClassRange( thing ) );
		
		// Property hierarchy
		login.getDirectSupers().add( name );
		nickName.getDirectSupers().add( name );
		
		// Domains and properties
		thing.getDirectProperties().add( size );
		organism.getDirectProperties().add( age );
		person.getDirectProperties().add( nickName );
		person.getDirectProperties().add( likes );
		user.getDirectProperties().add( login );
		age.setCardinality( 1 );
		
		// Restrictions
		organism.getRestriction( size, true ).setCardinality( 1 );
		person.getRestriction( nickName, true ).setMinCardinality( 0 );
		person.getRestriction( nickName, false ).setMaxCardinality( 5 );
		user.getRestriction( login, true ).setCardinality( 1 );
		musicListener.getRestriction( likes, true ).setRange(
			new MetaStructureClassRange( song ) );
		
		// Verify
		assertLookup( meta, size, MetaStructure.LOOKUP_MIN_CARDINALITY, null,
			thing );
		assertLookup( meta, size, MetaStructure.LOOKUP_MIN_CARDINALITY, 1,
			organism );
		assertLookup( meta, age, MetaStructure.LOOKUP_MAX_CARDINALITY, 1,
			thing, organism, person, user, musicListener, song );
		assertEquals( String.class, ( ( DatatypeClassRange ) meta.lookup(
			nickName, MetaStructure.LOOKUP_PROPERTY_RANGE,
			user ) ).getRangeClass() );
		assertCollection( Arrays.asList( ( ( MetaStructureClassRange )
			meta.lookup( likes, MetaStructure.LOOKUP_PROPERTY_RANGE,
			musicListener ) ).getRangeClasses() ), song );
		assertCollection( Arrays.asList( ( ( MetaStructureClassRange )
			meta.lookup( likes, MetaStructure.LOOKUP_PROPERTY_RANGE,
			person ) ).getRangeClasses() ), thing );
		
		deleteMetaModel();
	}
	
	private <T> void assertLookup( MetaStructure meta,
		MetaStructureProperty property, LookerUpper<T> finder, T expectedValue,
		MetaStructureClass... classes )
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
}
