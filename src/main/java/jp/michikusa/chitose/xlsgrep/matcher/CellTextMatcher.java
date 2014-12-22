package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.CellReference;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.NonNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;

public class CellTextMatcher
    implements Matcher
{
    @Override
    public Stream<MatchResult> matches(@NonNull Workbook workbook, @NonNull Pattern pattern)
    {
        return StreamTaker.cells(workbook)
            .map((Cell c) -> { return this.matches(c, pattern); })
            .filter((Optional<MatchResult> r) -> { return r.isPresent(); })
            .map((Optional<MatchResult> r) -> { return r.get(); })
        ;
    }

    private Optional<MatchResult> matches(@NonNull Cell cell, @NonNull Pattern pattern)
    {
        final String text= this.formatter.formatCellValue(cell);
        final java.util.regex.Matcher rmatcher= pattern.matcher(text);
        if(rmatcher.find())
        {
            final CellReference ref= new CellReference(cell);

            return Optional.of(new MatchResult(ref, text, rmatcher.start(), rmatcher.end()));
        }
        else
        {
            return Optional.empty();
        }
    }

    private final DataFormatter formatter= new DataFormatter();
}
