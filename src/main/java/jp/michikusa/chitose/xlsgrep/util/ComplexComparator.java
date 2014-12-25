package jp.michikusa.chitose.xlsgrep.util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;

public final class ComplexComparator<T>
    implements Comparator<T>
{
    @SafeVarargs
    public ComplexComparator(@NonNull Comparator<? super T>... comparators)
    {
        for(final Comparator<? super T> comparator : comparators)
        {
            this.comparators.add(comparator);
        }
    }

    @Override
    public int compare(T o1, T o2)
    {
        for(final Comparator<? super T> comp : this.comparators)
        {
            final int ret= comp.compare(o1, o2);
            if(ret != 0)
            {
                return ret;
            }
        }
        return 0;
    }

    private final List<Comparator<? super T>> comparators= new LinkedList<>();
}
