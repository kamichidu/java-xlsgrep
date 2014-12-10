package jp.michikusa.chitose.xlsgrep.util;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringTemplateTest
{
    @Test
    public void emptyString()
        throws ParseException
    {
        final StringTemplate tpl= new StringTemplate("");

        assertEquals("", tpl.apply(Collections.emptyMap()));
    }

    @Test
    public void onlyCharacters()
        throws ParseException
    {
        final StringTemplate tpl= new StringTemplate("hoge");

        assertEquals("hoge", tpl.apply(Collections.emptyMap()));
    }

    @Test
    public void escapedString()
        throws ParseException
    {
        final StringTemplate tpl= new StringTemplate("hoge\\{fuga\\\\");

        assertEquals("hoge{fuga\\", tpl.apply(Collections.emptyMap()));
    }

    @Test
    public void escapedString2()
        throws ParseException
    {
        final StringTemplate tpl= new StringTemplate("hoge\\{fuga\\\\");

        assertEquals("hoge{fuga\\", tpl.apply(Collections.emptyMap()));
    }

    @Test
    public void expandProperty()
        throws ParseException
    {
        final StringTemplate tpl= new StringTemplate("hoge{fuga}{pi}");
        final Map<String, String> data= new HashMap<>();

        data.put("fuga", "FUGA");
        data.put("piyo", "PIYO");

        assertEquals("hogeFUGA", tpl.apply(data));
    }
}
