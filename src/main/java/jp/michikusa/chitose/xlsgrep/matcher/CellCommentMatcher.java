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
    public CellCommentMatcher(@NonNull Workbook workbook)
    {
        this.workbook= workbook;
    }

    @Override
    public Stream<MatchResult> matches(@NonNull Pattern pattern) {
         Spliterator<MatchResult> spliterator= cells(this.workbook)
            .filter((CellComment c) -> this.match(c, pattern))
            .map(this::makeResult)
            .spliterator()
        ;
         return StreamSupport.stream(spliterator, true);
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

    private boolean match(@NonNull CellComment comment, @NonNull Pattern pattern)
    {
        return pattern.matcher(comment.getComment().getString().getString()).find();
    }

    private MatchResult makeResult(@NonNull CellComment comment)
    {
        final CellReference ref= new CellReference(comment.getSheet(), comment.getComment().getRow(), comment.getComment().getColumn());

        return new MatchResult(ref);
    }

    private final Workbook workbook;
}
