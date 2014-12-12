package jp.michikusa.chitose.xlsgrep.cli;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import jp.michikusa.chitose.xlsgrep.NoSuchMatcherException;
import jp.michikusa.chitose.xlsgrep.matcher.CellCommentMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellFormulaMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellTextMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import jp.michikusa.chitose.xlsgrep.matcher.ShapeMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.SheetNameMatcher;
import jp.michikusa.chitose.xlsgrep.util.MultiGlobOptionHandler;

import lombok.Getter;

import org.apache.poi.ss.usermodel.Workbook;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class AppOption
{
    public Function<Workbook, Matcher> getMatcher()
    {
        switch(this.matcher)
        {
            case "text":
                return (Workbook workbook) -> new CellTextMatcher(workbook);
            case "comment":
                return (Workbook workbook) -> new CellCommentMatcher(workbook);
            case "shape":
                return (Workbook workbook) -> new ShapeMatcher(workbook);
            case "sheet":
                return (Workbook workbook) -> new SheetNameMatcher(workbook);
            case "formula":
                return (Workbook workbook) -> new CellFormulaMatcher(workbook);
            default:
                throw new NoSuchMatcherException(this.matcher);
        }
    }

    public Pattern getPattern()
    {
        if(this.useRegex)
        {
            return Pattern.compile(this.pattern);
        }
        else
        {
            return Pattern.compile(Pattern.quote(this.pattern));
        }
    }

    public Iterable<String> getGlobExprs()
    {
        final Set<String> exprs= new LinkedHashSet<>();
        for(final String expr : this.exprs)
        {
            // unify path separator
            exprs.add(expr.replace('\\', '/'));
        }
        return exprs;
    }

    public String getReportFormat()
    {
        return this.format;
    }

    @Getter
    @Option(name= "--recurse", aliases= "-R", usage= "Search files recursively (Default: false)")
    private boolean recurse;

    @Option(name= "--matcher", aliases= "-m", usage= "Specify matcher for cell text or formula, or else (Available: 'text', 'comment', 'shape', 'sheet')")
    private String matcher= "text";

    @Option(name= "--regex", aliases= "-r", usage= "{pattern} is a Java regular expression (Default: false)")
    private boolean useRegex= false;

    @Option(
        name= "--format",
        aliases= "-f",
        usage= "Specify reporting format\n" +
               "    {filename} - The filename\n" +
               "    {sheet}    - The sheet name\n" +
               "    {cell}     - The cell address\n"
    )
    private String format= "{filename}:{sheet}:{cell}";

    @Argument(index= 0, required= true)
    private String pattern;

    @Argument(index= 1, multiValued= true, handler= MultiGlobOptionHandler.class)
    private String[] exprs= new String[0];
}
