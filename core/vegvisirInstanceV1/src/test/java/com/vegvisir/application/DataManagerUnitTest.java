package com.vegvisir.application;


//import org.junit.jupiter.api.Test;

import org.junit.Assert;
import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DataManagerUnitTest {

    @Test
    public void refConvert() {
        String ori = "hello world/sfenbv123n12vpdv_O";
        Assert.assertEquals(ori, Utils.hex2str(Utils.str2Hex(ori)));
        System.out.println("Passed");
    }
}