package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public CellTextMatcher(@NonNull Workbook workbook)
    {
        this.workbook= workbook;
    }

    @Override
    public Stream<MatchResult> matches(@NonNull Pattern pattern)
    {
        final Spliterator<MatchResult> spliterator= StreamTaker.cells(this.workbook)
            .filter((Cell cell) -> this.match(cell, pattern))
            .map(this::makeResult)
            .spliterator()
        ;
        return StreamSupport.stream(spliterator, true);
    }

    private boolean match(@NonNull Cell cell, @NonNull Pattern pattern)
    {
        return pattern.matcher(this.formatter.formatCellValue(cell)).find();
    }

    private MatchResult makeResult(@NonNull Cell cell)
    {
        final CellReference ref= new CellReference(cell);

        return new MatchResult(ref);
    }

    private final Workbook workbook;

    private final DataFormatter formatter= new DataFormatter();
}
