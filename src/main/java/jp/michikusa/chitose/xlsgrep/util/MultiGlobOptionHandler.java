package jp.michikusa.chitose.xlsgrep.util;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class MultiGlobOptionHandler
    extends OptionHandler<String>
{
    public MultiGlobOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter)
    {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params)
        throws CmdLineException
    {
        int counter=0;

        while(counter < params.size())
        {
            final String param= params.getParameter(counter);

            if(param.startsWith("-"))
            {
                break;
            }

            this.setter.addValue(param);

            ++counter;
        }

        return counter;
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return "PATH[]";
    }
}
