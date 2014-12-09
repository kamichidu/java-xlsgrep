package jp.michikusa.chitose.xlsgrep.cli;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import lombok.NonNull;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
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

        final App app= new App(option, System.in, System.out, System.err);
        try
        {
            app.start();
        }
        catch(Exception e)
        {
            logger.error("Something wrong.", e);
            System.exit(1);
        }
    }

    public App(@NonNull AppOption option, @NonNull InputStream in, @NonNull OutputStream out, @NonNull OutputStream err)
    {
        this.option= option;
        this.in= new InputStreamReader(in, Charset.defaultCharset());
        this.out= new PrintWriter(out);
        this.err= new PrintWriter(err);
    }

    public void start()
    {
        final Function<Workbook, Matcher> matcherProvider= this.option.getMatcher();

        final Stream<Path> paths= this.option.getPaths()
            .map(this::files)
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;

        paths.forEachOrdered((Path p) -> {
            try(
                final InputStream origIn= new FileInputStream(p.toFile());
                final InputStream fileIn= new PushbackInputStream(origIn, 1024);
            )
            {
                if(!NPOIFSFileSystem.hasPOIFSHeader(fileIn))
                {
                    this.err.format("`%s' is not an Excel file.%n", p.toFile().getAbsolutePath());
                    this.err.flush();
                    return;
                }

                final Workbook workbook;
                try
                {
                    workbook= WorkbookFactory.create(fileIn);
                }
                catch(InvalidFormatException | IllegalArgumentException e)
                {
                    this.err.format("`%s' is not an Excel file.%n", p.toFile().getAbsolutePath());
                    this.err.flush();
                    return;
                }

                final Matcher matcher= matcherProvider.apply(workbook);
                final Stream<MatchResult> matched= matcher.matches(this.option.getPattern());

                matched.forEachOrdered((MatchResult r) -> {
                    try
                    {
                        this.out.format("%s:%s:%s%n", p.toFile().getAbsolutePath(), r.getSheetName(), r.getCellAddress());
                        this.out.flush();
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
        });
    }

    private Stream<Path> files(@NonNull Path path)
    {
        if(!path.toFile().exists())
        {
            this.err.format("`%s' couldn't be found.%n", path.toFile().getAbsolutePath());
            this.err.flush();
            return Stream.empty();
        }
        if(path.toFile().isDirectory())
        {
            if(!this.option.isRecurse())
            {
                this.err.format("`%s' is a directory.%n", path.toFile().getAbsolutePath());
                this.err.flush();
                return Stream.empty();
            }

            final File[] children= path.toFile().listFiles();
            if(children == null)
            {
                this.err.format("`%s' couldn't be opened.%n", path.toFile().getAbsolutePath());
                this.err.flush();
                return Stream.empty();
            }

            return Stream.of(children)
                .map((File f) -> { return f.toPath(); })
                .map(this::files)
                .reduce(Stream::concat)
                .orElse(Stream.empty())
            ;
        }
        else
        {
            return Stream.of(path);
        }
    }

    private static final Logger logger= LoggerFactory.getLogger(App.class);

    private final AppOption option;

    private final Reader in;

    private final PrintWriter out;

    private final PrintWriter err;
}
