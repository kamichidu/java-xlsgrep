package jp.michikusa.chitose.xlsgrep.util;

import java.nio.file.Path;

import org.junit.Test;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static org.junit.Assert.assertEquals;

public class MultiGlobOptionHandlerTest
{
    public static class Opt
    {
        @Option(name= "-m")
        private String dummy;

        @Argument(handler= MultiGlobOptionHandler.class)
        private String[] paths= new String[0];
    }

    @Test
    public void noArgs()
        throws Exception
    {
        final Opt opt= new Opt();
        final CmdLineParser parser= new CmdLineParser(opt);

        parser.parseArgument("-m", "text");

        assertEquals(0, opt.paths.length);
    }

    @Test
    public void oneArgs()
        throws Exception
    {
        final Opt opt= new Opt();
        final CmdLineParser parser= new CmdLineParser(opt);

        parser.parseArgument("-m", "text", "src");

        assertEquals(1, opt.paths.length);
    }

    @Test
    public void someArgs()
        throws Exception
    {
        final Opt opt= new Opt();
        final CmdLineParser parser= new CmdLineParser(opt);

        parser.parseArgument("-m", "text", "src", "src/main/java", "src/test/java/");

        assertEquals(3, opt.paths.length);
    }

    @Test
    public void someArgsWithSpaces()
        throws Exception
    {
        final Opt opt= new Opt();
        final CmdLineParser parser= new CmdLineParser(opt);

        parser.parseArgument("-m", "text", "src test java", "src/main dayo/java", "src/test dayo/java/");

        assertEquals(3, opt.paths.length);
    }
}
