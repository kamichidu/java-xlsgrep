package jp.michikusa.chitose.xlsgrep;

import java.nio.file.Path;
import java.util.Optional;

import jp.michikusa.chitose.xlsgrep.util.Addresses;
import jp.michikusa.chitose.xlsgrep.util.CellReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MatchResult
{
    public MatchResult(CellReference ref, @NonNull CharSequence text, int start, int end)
    {
        this.cellRef= ref;
        this.text= text.toString();
        this.start= start;
        this.end= end;
    }

    public CharSequence getSheetName()
    {
        return this.cellRef.getSheet().getSheetName();
    }

    public CharSequence getCellAddress()
    {
        return this.cellRef.getAddress();
    }

    public void setFilepath(Path filepath)
    {
        this.filepath= Optional.ofNullable(filepath);
    }

    public CharSequence getMatched()
    {
        // extract matched part in line
        final int startOfLine;
        {
            int i= this.start;
            while((i - 1) >= 0)
            {
                final char ch= this.text.charAt(i - 1);
                if(ch == '\r' || ch == '\n')
                {
                    break;
                }

                --i;
            }

            startOfLine= i;
        }

        final int endOfLine;
        {
            // this.end is exclusive index
            int i= this.end;
            while(i < this.text.length())
            {
                final char ch= this.text.charAt(i);
                if(ch == '\r' || ch == '\n')
                {
                    break;
                }

                ++i;
            }

            endOfLine= i;
        }

        return this.text.substring(startOfLine, endOfLine);
    }

    @Getter
    private Optional<Path> filepath;

    @Getter
    private final CellReference cellRef;

    private final String text;

    private final int start;

    private final int end;
}
