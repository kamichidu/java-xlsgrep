package jp.michikusa.chitose.xlsgrep.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import lombok.NonNull;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App
{
    public static void main(String[] args)
    {
        final AppOption option= new AppOption();
        final CmdLineParser parser= new CmdLineParser(option);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e)
        {
            System.err.println("Usage: xlsgrep [options] {pattern} [{path} ...]");
            System.err.println();

            parser.printUsage(System.err);

            System.exit(1);
        }

        final App app= new App(option);
        try
        {
            app.start(System.in, System.out, System.err);
        }
        catch(Exception e)
        {
            logger.error("Something wrong.", e);
            System.exit(1);
        }
    }

    public App(@NonNull AppOption option)
    {
        this.option= option;
    }

    public void start(InputStream in, OutputStream out, OutputStream err)
    {
        final Function<Workbook, Matcher> matcherProvider= this.option.getMatcher();

        this.option.getPaths()
            .filter((Path p) -> { return p.toFile().exists(); })
            .forEachOrdered((Path p) -> {
                try(Workbook workbook= WorkbookFactory.create(p.toFile()))
                {
                    final Matcher matcher= matcherProvider.apply(workbook);
                    final Stream<MatchResult> matched= matcher.matches(this.option.getPattern());

                    matched.forEachOrdered((MatchResult r) -> {
                        try
                        {
                            final StringBuilder buffer= new StringBuilder();

                            buffer.append(p.toFile().getAbsolutePath());
                            buffer.append(":");
                            buffer.append(r.getSheetName());
                            buffer.append(":");
                            buffer.append(r.getCellAddress());
                            buffer.append("\n");

                            out.write(buffer.toString().getBytes());
                        }
                        catch(IOException e)
                        {
                            throw new IllegalStateException(e);
                        }
                        catch(Exception e)
                        {
                            logger.error("Something wrong.", e);
                        }
                    });
                }
                catch(Exception e)
                {
                    logger.error("Something wrong.", e);
                }
            })
        ;
    }

    private static final Logger logger= LoggerFactory.getLogger(App.class);

    private final AppOption option;
}
