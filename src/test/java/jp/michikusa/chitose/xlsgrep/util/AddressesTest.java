package jp.michikusa.chitose.xlsgrep.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddressesTest
{
    @Test
    public void A1()
    {
        assertEquals("A1", Addresses.A1.format(0, 0));
        assertEquals("B27", Addresses.A1.format(26, 1));
        assertEquals("Z27", Addresses.A1.format(26, 25));
        assertEquals("AA28", Addresses.A1.format(27, 26));
        assertEquals("AC28", Addresses.A1.format(27, 28));
        assertEquals("JC14", Addresses.A1.format(13, 262));
    }

    @Test
    public void R1C1()
    {
        assertEquals("R1C1", Addresses.R1C1.format(0, 0));
        assertEquals("R2C27", Addresses.R1C1.format(1, 26));
        assertEquals("R26C27", Addresses.R1C1.format(25, 26));
        assertEquals("R27C28", Addresses.R1C1.format(26, 27));
    }
}
