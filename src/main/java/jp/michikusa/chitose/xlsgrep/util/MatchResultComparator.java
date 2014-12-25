package jp.michikusa.chitose.xlsgrep.util;

import java.util.Comparator;

import jp.michikusa.chitose.xlsgrep.MatchResult;

public class MatchResultComparator
    implements Comparator<MatchResult>
{
    public MatchResultComparator()
    {
        this.delegate= new ComplexComparator<>(
            new FilepathComparator(),
            new SheetNameComparator(),
            new CellAddrComparator()
        );
    }

    @Override
    public int compare(MatchResult o1, MatchResult o2) {
        return this.delegate.compare(o1, o2);
    }

    private static final class FilepathComparator
        implements Comparator<MatchResult>
    {
        @Override
        public int compare(MatchResult o1, MatchResult o2)
        {
            if(o1.getFilepath().isPresent() && o2.getFilepath().isPresent())
            {
                return o1.getFilepath().get().compareTo(o2.getFilepath().get());
            }
            if(!o1.getFilepath().isPresent() && o2.getFilepath().isPresent())
            {
                return -1;
            }
            if(o1.getFilepath().isPresent() && !o2.getFilepath().isPresent())
            {
                return 1;
            }
            return 0;
        }
    }

    private static final class SheetNameComparator
        implements Comparator<MatchResult>
    {
        @Override
        public int compare(MatchResult o1, MatchResult o2)
        {
            return o1.getSheetName().toString().compareTo(o2.getSheetName().toString());
        }
    }

    private static final class CellAddrComparator
        implements Comparator<MatchResult>
    {
        @Override
        public int compare(MatchResult o1, MatchResult o2)
        {
            return o1.getCellAddress().toString().compareTo(o2.getCellAddress().toString());
        }
    }

    private final ComplexComparator<MatchResult> delegate;
}
