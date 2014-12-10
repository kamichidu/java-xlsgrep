package jp.michikusa.chitose.xlsgrep.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import jp.michikusa.chitose.xlsgrep.util.StringTemplate;
import lombok.NonNull;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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
        try
        {
            parser.parseArgument(args);
        }
        catch(CmdLineException e)
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
        try
        {
            this.reportFormat= new StringTemplate(this.option.getReportFormat() + "\n");
        }
        catch(ParseException e)
        {
            this.err.format("`%s' は不正です。", this.option.getReportFormat());
            return;
        }

        final Stream<Path> paths= this.option.getPaths()
            .map(this::files)
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;

        final Stream<MatchResult> matches= paths
            .map(this::matches)
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;

        matches
            .map(this::format)
            .forEach((CharSequence msg) -> {
                this.out.write(msg.toString());
                this.out.flush();
            })
        ;
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

    private Stream<MatchResult> matches(@NonNull Path path)
    {
        try(final Workbook workbook= WorkbookFactory.create(path.toFile()))
        {
            final Matcher matcher= this.option.getMatcher().apply(workbook);
            final Stream<MatchResult> matches= matcher.matches(this.option.getPattern());
            return matches.map((MatchResult r) -> {
                r.setFilepath(path);
                return r;
            });
        }
        catch(InvalidFormatException | IllegalArgumentException | IOException e)
        {
            this.err.format("`%s' is not an Excel file.%n", path.toFile().getAbsolutePath());
            this.err.flush();
            return Stream.empty();
        }
        catch(Exception e)
        {
            logger.error("Something wrong.", e);
            return Stream.empty();
        }
    }

    private CharSequence format(@NonNull MatchResult data)
    {
        final Map<String, CharSequence> tplData= new HashMap<>();

        tplData.put("filename", data.getFilepath().isPresent()
            ? data.getFilepath().get().toFile().getAbsolutePath()
            : "<<<Unknown>>>"
        );
        tplData.put("sheet", data.getSheetName());
        tplData.put("cell", data.getCellAddress());

        return this.reportFormat.apply(tplData);
    }

    private static final Logger logger= LoggerFactory.getLogger(App.class);

    private final AppOption option;

    private final Reader in;

    private final PrintWriter out;

    private final PrintWriter err;

    private StringTemplate reportFormat;
}
