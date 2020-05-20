package com.vegvisir.app.tasklist.ui.login;


import com.vegvisir.app.tasklist.R;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginActivityTest {
    @Test
    void testPriorityEnumConstruction() {
        assert (LoginActivity.Priority.High == LoginActivity.Priority.High);
        assert (LoginActivity.Priority.Medium != LoginActivity.Priority.Low);
        List< LoginActivity.Priority> priorities = Arrays.asList(
                LoginActivity.Priority.Medium, LoginActivity.Priority.Low,
                LoginActivity.Priority.High
        );
        priorities.sort(LoginActivity.Priority :: priorityComparator);
        System.out.println(priorities);

        assert( priorities.get(0) == LoginActivity.Priority.High);
        assert( priorities.get(1) == LoginActivity.Priority.Medium);
        assert( priorities.get(2) == LoginActivity.Priority.Low);
    }


    // Needs to have Context created in s
    @Test
    void testPriorityEnumGetAssociated() {
        //assert( R.color.Green == LoginActivity.Priority.Low.getAssociatedColor()) ;
        //assert( R.color.Blue  == LoginActivity.Priority.Medium.getAssociatedColor());
        //assert( R.color.Red   == LoginActivity.Priority.High.getAssociatedColor() );

    }
}