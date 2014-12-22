package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.NonNull;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class SheetNameMatcher
    implements Matcher
{
    @Override
    public Stream<MatchResult> matches(@NonNull Workbook workbook, @NonNull Pattern pattern)
    {
        return StreamTaker.sheets(workbook)
            .map((Sheet s) -> { return this.matches(s, pattern); })
            .filter((Optional<MatchResult> r) -> { return r.isPresent(); })
            .map((Optional<MatchResult> r) -> { return r.get(); })
        ;
    }

    private Optional<MatchResult> matches(@NonNull Sheet sheet, @NonNull Pattern pattern)
    {
        final String text= sheet.getSheetName();
        final java.util.regex.Matcher rmatcher= pattern.matcher(text);
        if(rmatcher.find())
        {
            return Optional.of(new MatchResult(null, text, rmatcher.start(), rmatcher.end()));
        }
        else
        {
            return Optional.empty();
        }
    }
}
