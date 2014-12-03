package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import lombok.NonNull;

public interface Matcher
{
    Stream<MatchResult> matches(Pattern pattern);
}
