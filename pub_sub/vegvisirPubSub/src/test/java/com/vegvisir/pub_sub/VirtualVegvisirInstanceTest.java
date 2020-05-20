package com.vegvisir.pub_sub;



import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class VirtualVegvisirInstanceTest {
    @Test
     void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void test_virtual_instance(){
        VirtualVegvisirInstance virtual = VirtualVegvisirInstance.getInstance(  );
        assertTrue( virtual != null );
    }

    @Test
    void getInstance(){
        VirtualVegvisirInstance a = VirtualVegvisirInstance.getInstance();
        assertEquals( "DeviceA", a.getDeviceId() );
        VirtualVegvisirInstance b = VirtualVegvisirInstance.getInstance("Danny");
        assertEquals("Danny", b.getDeviceId() );
        assertNotEquals( a, b );
    }

    @Test
    void getSubscriptionList(){
        VirtualVegvisirInstance a = VirtualVegvisirInstance.getInstance();
        Map<String, Set<String>> result = a.getSubscriptionList();
        assertEquals(0, result.size());
    }

    @Test
    void updateSubscriptionList(){
        VegvisirApplicationContext context = new VegvisirApplicationContext( "Shop",
                "We all should shop more",
                Stream.of("Pepsi", "Coke").collect(Collectors.toSet()));
        VirtualVegvisirInstance virtual = VirtualVegvisirInstance.getInstance( "DeviceA" );
        VirtualVegvisirApplicationDelegator delegator = new VirtualVegvisirApplicationDelegator();

        virtual.registerApplicationDelegator( context, delegator );
        virtual.updateSubscriptionList(virtual.getDeviceId(), context.getChannels());
        assertEquals(2, virtual.getSubscriptionList().size() );
        virtual.updateSubscriptionList("Bob", Stream.of("Pepsi", "Coke").collect(Collectors.toSet()));
        assertEquals(2, virtual.getSubscriptionList().size() );
        virtual.updateSubscriptionList("Bob", Stream.of("Mountain Dew").collect(Collectors.toSet()));
        assertEquals(3, virtual.getSubscriptionList().size() );
        assertEquals(2, virtual.getSubscriptionList().get("Coke").size() );
        assertEquals(2, virtual.getSubscriptionList().get("Pepsi").size() );
        assertEquals(1, virtual.getSubscriptionList().get("Mountain Dew").size() );
        virtual.updateSubscriptionList("Bob", Stream.of("Mountain Dew").collect(Collectors.toSet()));
        assertEquals(3, virtual.getSubscriptionList().size() );
        assertEquals(1, virtual.getSubscriptionList().get("Mountain Dew").size() );
    }


}