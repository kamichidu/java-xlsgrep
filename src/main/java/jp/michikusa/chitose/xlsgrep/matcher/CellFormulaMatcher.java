package jp.michikusa.chitose.xlsgrep.matcher;

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
            .filter((Cell c) -> { return pattern.matcher(c.getCellFormula()).find(); })
            .map((Cell c) -> {
                final CellReference cellref= new CellReference(c);

                return new MatchResult(cellref);
            })
        ;
    }

    private final Workbook workbook;
}
