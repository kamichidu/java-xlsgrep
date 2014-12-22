package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Optional;
import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.CellReference;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class CellCommentMatcher
    implements Matcher
{
    @Override
    public Stream<MatchResult> matches(@NonNull Workbook workbook, @NonNull Pattern pattern) {
         return cells(workbook)
            .map((CellComment c) -> { return this.matches(c, pattern); })
            .filter((Optional<MatchResult> r) -> { return r.isPresent(); })
            .map((Optional<MatchResult> r) -> { return r.get(); })
        ;
    }

    @RequiredArgsConstructor
    private static final class CellComment
    {
        @Getter
        private final Sheet sheet;

        @Getter @NonNull
        private final Comment comment;
    }

    private static Stream<CellComment> cells(@NonNull Workbook workbook)
    {
        return StreamTaker.sheets(workbook)
            .map(CellCommentMatcher::gatherComments)
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    private static Stream<CellComment> gatherComments(@NonNull Sheet sheet)
    {
        if(sheet.getPhysicalNumberOfRows() <= 0)
        {
            return Stream.empty();
        }

        final int maxNumOfColumns= (sheet instanceof XSSFSheet) ? 16384 : 256;
        return StreamTaker.rows(sheet)
            .map((Row r) -> {
                return IntStream.range(0, maxNumOfColumns)
                    .mapToObj((int cellnum) -> Optional.ofNullable(sheet.getCellComment(r.getRowNum(), cellnum)))
                    .filter((Optional<Comment> c) -> c.isPresent())
                    .map((Optional<Comment> c) -> new CellComment(sheet, c.get()))
                ;
            })
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    private Optional<MatchResult> matches(@NonNull CellComment comment, @NonNull Pattern pattern)
    {
        final String commentText= comment.getComment().getString().getString();
        final java.util.regex.Matcher rmatcher= pattern.matcher(commentText);
        if(rmatcher.find())
        {
            final CellReference ref= new CellReference(comment.getSheet(), comment.getComment().getRow(), comment.getComment().getColumn());

            return Optional.of(new MatchResult(ref, commentText, rmatcher.start(), rmatcher.end()));
        }
        else
        {
            return Optional.empty();
        }
    }
}
