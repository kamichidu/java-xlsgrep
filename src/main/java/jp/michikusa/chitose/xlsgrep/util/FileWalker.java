package jp.michikusa.chitose.xlsgrep.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWalker
{
    public FileWalker recurse(boolean recurse)
    {
        this.recurse= recurse;
        return this;
    }

    public FileWalker addExpr(@NonNull CharSequence expr)
    {
        CharSequence uexpr= this.unify(expr);
        if(this.recurse)
        {
            uexpr= this.makeRecursive(uexpr);
        }
        uexpr= this.ensureAbsolute(uexpr);

        Map<String, Object> current= this.trie;
        for(final String elm : uexpr.toString().split("/"))
        {
            if(!current.containsKey(elm))
            {
                current.put(elm, new HashMap<>());
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> tmp= (Map<String, Object>)current.get(elm);
            current= tmp;
        }
        return this;
    }

    public FileWalker addExprs(@NonNull Iterable<? extends CharSequence> exprs)
    {
        for(final CharSequence expr : exprs)
        {
            this.addExpr(expr);
        }
        return this;
    }

    public Stream<Path> walk()
        throws IOException
    {
        try
        {
            final Stream<GlobPath> globs= StreamSupport.stream(this.longestPaths().spliterator(), true);

            return globs
                .map((GlobPath gp) -> {
                    try
                    {
                        return Files.walk(gp.path)
                            .filter((Path p) -> {
                                return gp.matchers.stream()
                                    .anyMatch((PathMatcher pm) -> { return pm.matches(p); })
                                ;
                            })
                        ;
                    }
                    catch(IOException e)
                    {
                        throw new WrappedIOException(e);
                    }
                })
                .reduce(Stream::concat)
                .orElse(Stream.empty())
            ;
        }
        catch(WrappedIOException e)
        {
            throw e.origin;
        }
    }

    public void walk(final FileVisitor<? super Path> visitor)
        throws IOException
    {
        final Stream<GlobPath> globs= StreamSupport.stream(this.longestPaths().spliterator(), true);

        try
        {
            globs.forEach((GlobPath gp) -> {
                try
                {
                    Files.walkFileTree(gp.path, new FileVisitor<Path>(){
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException
                        {
                            return visitor.preVisitDirectory(dir, attrs);
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException
                        {
                            final boolean matched= gp.matchers.stream()
                                .anyMatch((PathMatcher pm) -> { return pm.matches(file); })
                            ;
                            if(matched)
                            {
                                return visitor.visitFile(file, attrs);
                            }
                            else
                            {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                            throws IOException
                        {
                            return visitor.visitFileFailed(file, exc);
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException
                        {
                            return visitor.postVisitDirectory(dir, exc);
                        }
                    });
                }
                catch(IOException e)
                {
                    throw new WrappedIOException(e);
                }
            });
        }
        catch(WrappedIOException e)
        {
            throw e.origin;
        }
    }

    @EqualsAndHashCode
    @ToString
    static final class GlobPath
        implements Comparable<GlobPath>
    {
        public GlobPath(@NonNull Path path, @NonNull Iterable<? extends PathMatcher> matchers)
        {
            this.path= path;
            for(final PathMatcher matcher : matchers)
            {
                this.matchers.add(matcher);
            }
        }

        @Override
        public int compareTo(GlobPath o)
        {
            int r= this.path.compareTo(o.path);
            if(r != 0)
            {
                return r;
            }
            return this.matchers.size() - o.matchers.size();
        }

        final Path path;

        final Set<PathMatcher> matchers= new HashSet<>();
    }

    void dump()
    {
        System.out.println("--- trie ---");
        this.dump(this.trie, "");
        System.out.println("------------");
    }

    Iterable<GlobPath> longestPaths()
    {
        final Set<GlobPath> paths= new TreeSet<>();
        for(final String key : this.trie.keySet())
        {
            @SuppressWarnings("unchecked")
            final Map<String, Object> childTrie= (Map<String, Object>)this.trie.get(key);
            // root path element looks like volume or not
            // the all keys are one of volume and empty string, because addExpr() splits by `/'
            final Path root= key.matches("^[a-zA-Z]:$")
                ? Paths.get(key + "/")
                : Paths.get("/")
            ;

            paths.addAll(this.longestPaths(root, childTrie));
        }
        return paths;
    }

    CharSequence unify(@NonNull CharSequence ununified)
    {
        final String uexpr= ununified.toString()
            .replace('\\', '/')
            .replaceFirst("^\\./", "")
        ;

        if(ignoreCase)
        {
            return uexpr.toLowerCase();
        }
        else
        {
            return uexpr;
        }
    }

    CharSequence makeRecursive(@NonNull CharSequence expr)
    {
        if(this.looksLikeAbsolutePath(expr))
        {
            return expr;
        }

        return "**/" + expr;
    }

    CharSequence ensureAbsolute(@NonNull CharSequence expr)
    {
        if(this.looksLikeAbsolutePath(expr))
        {
            return expr;
        }

        return String.format("%s/%s", this.unify(System.getProperty("user.dir")), expr);
    }

    @SuppressWarnings("serial")
    private static class WrappedIOException
        extends RuntimeException
    {
        public WrappedIOException(IOException e) {
            super(e);

            this.origin= e;
        }

        private final IOException origin;
    }

    private void dump(@NonNull Map<? extends String, ? extends Object> m, @NonNull CharSequence prefix)
    {
        for(final Map.Entry<? extends String, ? extends Object> entry : m.entrySet())
        {
            System.out.println("" + prefix + entry.getKey());

            @SuppressWarnings("unchecked")
            final Map<String, Object> nested= (Map<String, Object>)entry.getValue();
            this.dump(nested, prefix + "| ");
        }
    }

    private Collection<GlobPath> longestPaths(@NonNull Path parent, @NonNull Map<? extends String, ? extends Object> trie)
    {
        final Set<GlobPath> paths= new HashSet<>();
        for(final String child : trie.keySet())
        {
            @SuppressWarnings("unchecked")
            final Map<String, Object> childTrie= (Map<String, Object>)trie.get(child);

            if(!child.equals("**"))
            {
                final Path childPath= parent.resolve(child);
                logger.debug("parent=`{}', child=`{}', resolved=`{}'", parent, child, childPath);

                for(final GlobPath globPath : this.longestPaths(childPath, childTrie))
                {
                    paths.add(globPath);
                }
            }
            else
            {
                final Iterable<PathMatcher> matchers= this.makePathMatchers(
                    this.unify(parent.toAbsolutePath().toString()),
                    trie
                );
                return Arrays.asList(new GlobPath(parent, matchers));
            }
        }

        if(paths.isEmpty())
        {
            paths.add(new GlobPath(parent, Collections.emptyList()));
        }

        return paths;
    }

    private Iterable<PathMatcher> makePathMatchers(@NonNull CharSequence parent, @NonNull Map<? extends String, ? extends Object> trie)
    {
        final CharSequence parentExpr= (parent != null && trie.isEmpty())
            ? parent
            : parent + "/"
        ;
        logger.debug("parentExpr=`{}'", parentExpr);

        if(trie.isEmpty())
        {
            final String expr= "glob:" + parentExpr;
            logger.debug("result=[`{}']", expr);
            return Arrays.asList(FileSystems.getDefault().getPathMatcher(expr));
        }

        final Set<PathMatcher> pathMatchers= new HashSet<>();
        for(final String child : trie.keySet())
        {
            final CharSequence childExpr= parentExpr + child;
            @SuppressWarnings("unchecked")
            final Map<String, Object> childTrie= (Map<String, Object>)trie.get(child);

            for(final PathMatcher matcher : this.makePathMatchers(childExpr, childTrie))
            {
                pathMatchers.add(matcher);
            }
        }
        return pathMatchers;
    }

    private boolean looksLikeAbsolutePath(@NonNull CharSequence expr)
    {
        final boolean ret= Pattern.compile("^(?:(?:[a-zA-Z]:)?/)").matcher(expr).find();

        logger.debug("`{}' looks like absolute path? ({})", expr, ret);

        return ret;
    }

    private static final Logger logger= LoggerFactory.getLogger(FileWalker.class);

    private static final boolean ignoreCase;
    static
    {
        final String os= System.getProperty("os.name").toLowerCase();

        ignoreCase= os.startsWith("windows") || os.startsWith("mac");
    }

    private boolean recurse= false;

    private final Map<String, Object> trie= new HashMap<>();
}
