package jp.michikusa.chitose.xlsgrep.cli;

import java.util.LinkedHashSet;
import java.util.Set;
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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class AppOption
{
    public Matcher getMatcher()
    {
        switch(this.matcher)
        {
            case "text":
                return new CellTextMatcher();
            case "comment":
                return new CellCommentMatcher();
            case "shape":
                return new ShapeMatcher();
            case "sheet":
                return new SheetNameMatcher();
            case "formula":
                return new CellFormulaMatcher();
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

    public boolean hasRequires()
    {
        if(this.pattern == null)
        {
            return false;
        }
        return true;
    }

    @Getter
    @Option(name= "--version", aliases= "-v")
    private boolean versionFlag= false;

    @Getter
    @Option(name= "--recurse", aliases= "-R")
    private boolean recurse;

    @Option(name= "--matcher", aliases= "-m")
    private String matcher= "text";

    @Option(name= "--regex", aliases= "-r")
    private boolean useRegex= false;

    @Option(name= "--format", aliases= "-f")
    private String format= "{filename}:{sheet}:{cell}:{matched}";

    @Argument(index= 0)
    private String pattern;

    @Argument(index= 1, multiValued= true, handler= MultiGlobOptionHandler.class)
    private String[] exprs= new String[0];
}
