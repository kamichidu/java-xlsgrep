package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.CellReference;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.NonNull;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class SheetNameMatcher
    implements Matcher
{
    public SheetNameMatcher(@NonNull Workbook workbook)
    {
        this.workbook= workbook;
    }

    @Override
    public Stream<MatchResult> matches(Pattern pattern)
    {
        return StreamTaker.sheets(this.workbook)
            .filter((Sheet s) -> { return pattern.matcher(s.getSheetName()).find(); })
            .map((Sheet s) -> {
                final CellReference cellref= new CellReference(s, -1, -1);

                return new MatchResult(cellref);
            })
        ;
    }

    private final Workbook workbook;
}
