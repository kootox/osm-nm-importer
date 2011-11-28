package com.github.kootox;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    @Test
    public void testIntValue()
    {
        assertEquals(1422, App.intValue("1422,000000000000000"));
        assertEquals(106, App.intValue("106,000000000000000"));
        assertEquals(0, App.intValue(""));
    }

    @Test
    public void testDoubleValue()
    {
        assertEquals(-1.564270495530, App.doubleValue("-1,564270495530"));
        assertEquals(47.206835120347, App.doubleValue("47,206835120347"));
        assertEquals(0.0, App.doubleValue(""));
    }
}
