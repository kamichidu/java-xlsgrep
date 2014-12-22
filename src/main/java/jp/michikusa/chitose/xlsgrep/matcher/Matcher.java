package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import lombok.NonNull;

public interface Matcher
{
    Stream<MatchResult> matches(Workbook workbook, Pattern pattern);
}
