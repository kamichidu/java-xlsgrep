package jp.michikusa.chitose.xlsgrep;

import lombok.Getter;

@SuppressWarnings("serial")
public class NoSuchMatcherException
    extends IllegalArgumentException
{
    public NoSuchMatcherException(CharSequence name)
    {
        super(String.format("No such matcher: `%s'", name));

        this.matcherName= name;
    }

    @Getter
    private final CharSequence matcherName;
}
