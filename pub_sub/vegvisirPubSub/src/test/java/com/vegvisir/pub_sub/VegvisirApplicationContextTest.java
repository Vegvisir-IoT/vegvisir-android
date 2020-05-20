package com.vegvisir.pub_sub;




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class VegvisirApplicationContextTest {
    static VegvisirApplicationContext under_test = null;

    @BeforeEach
    void build(){
       under_test = new VegvisirApplicationContext("testApp",
               "description", Stream.of("Pepsi", "Coke").collect(Collectors.toSet()));
    }
    
    @Test
    void getAppID() {
        assertEquals("testApp", under_test.getAppID());
    }

    @Test
    void getDesc() {
        assertEquals("description", under_test.getDesc());
    }

    @Test
    void getChannels() {
        assertTrue(under_test.getChannels().contains("Coke"));
        assertTrue(under_test.getChannels().contains("Pepsi"));
    }

    @Test
    void setAppID() {
        assertEquals("testApp", under_test.getAppID());
        under_test.setAppID("changed");
        assertEquals("changed", under_test.getAppID());
    }

    @ParameterizedTest
    @ValueSource( strings = { "alpha", "beta", "charlie"})
    void setDesc( String argument) {
        assertEquals("description", under_test.getDesc());
        under_test.setDesc(argument);
        assertEquals( argument, under_test.getDesc());

    }

    @ParameterizedTest
    @ValueSource ( strings = {"Pepsi, Mountain Dew"})
    void updateChannels( String argument){
        boolean expected = !(under_test.getChannels().contains( argument ) );
        assertEquals( expected, under_test.updateChannels( argument ));

    }

    @Test
    void setChannels() {
        under_test.setChannels( Stream.of("S", "S", "F").collect(Collectors.toSet()));
        assertEquals( 2, under_test.getChannels().size() );
        assertTrue( under_test.getChannels().contains("S") &&
                under_test.getChannels().contains("F"));
    }

    @Test
    void updateChannels( ){
        assertEquals( 2, under_test.getChannels().size());
        Boolean result = under_test.updateChannels("Dr. Pepper" );
        assertTrue( result );
        assertEquals( 3, under_test.getChannels().size());
        result = under_test.updateChannels( "Coke");
        assertFalse( result );
        assertEquals( 3, under_test.getChannels().size());

    }
}

