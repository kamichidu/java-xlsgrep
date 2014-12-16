package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.CellReference;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.NonNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

public class CellFormulaMatcher
    implements Matcher
{
    public CellFormulaMatcher(@NonNull Workbook workbook)
    {
        this.workbook= workbook;
    }

    @Override
    public Stream<MatchResult> matches(@NonNull Pattern pattern)
    {
        final Stream<Cell> cells= StreamTaker.cells(this.workbook);

        return cells
            .filter((Cell c) -> { return c.getCellType() == Cell.CELL_TYPE_FORMULA; })
            .map((Cell c) -> { return this.matches(c, pattern); })
            .filter((Optional<MatchResult> r) -> { return r.isPresent(); })
            .map((Optional<MatchResult> r) -> {return r.get(); })
        ;
    }

    private Optional<MatchResult> matches(@NonNull Cell cell, @NonNull Pattern pattern)
    {
        final java.util.regex.Matcher rmatcher= pattern.matcher(cell.getCellFormula());
        if(rmatcher.find())
        {
            final CellReference cellref= new CellReference(cell);

            return Optional.of(new MatchResult(cellref, cell.getCellFormula(), rmatcher.start(), rmatcher.end()));
        }
        else
        {
            return Optional.empty();
        }
    }

    private final Workbook workbook;
}
