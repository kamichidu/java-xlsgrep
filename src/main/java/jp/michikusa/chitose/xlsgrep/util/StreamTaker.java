package jp.michikusa.chitose.xlsgrep.util;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.NonNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class StreamTaker
{
    public static Stream<Sheet> sheets(@NonNull Workbook book)
    {
        return IntStream.range(0, book.getNumberOfSheets())
            .mapToObj((int i) -> Optional.ofNullable(book.getSheetAt(i)))
            .filter((Optional<Sheet> s) -> s.isPresent())
            .map((Optional<Sheet> s) -> s.get())
        ;
    }

    public static Stream<Row> rows(@NonNull Workbook book)
    {
        return sheets(book)
            .map(StreamTaker::rows)
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    public static Stream<Row> rows(@NonNull Sheet sheet)
    {
        if(sheet.getPhysicalNumberOfRows() <= 0)
        {
            return Stream.empty();
        }

        return IntStream.range(sheet.getFirstRowNum(), sheet.getLastRowNum() + 1)
            .mapToObj((int i) -> Optional.ofNullable(sheet.getRow(i)))
            .filter((Optional<Row> r) -> r.isPresent())
            .map((Optional<Row> r) -> r.get())
        ;
    }

    public static Stream<Cell> cells(@NonNull Workbook book)
    {
        return sheets(book)
            .map((Sheet s) -> cells(s))
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    public static Stream<Cell> cells(@NonNull Sheet sheet)
    {
        return rows(sheet)
            .map((Row r) -> cells(r))
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    public static Stream<Cell> cells(@NonNull Row row)
    {
        if(row.getPhysicalNumberOfCells() <= 0)
        {
            return Stream.empty();
        }

        return IntStream.range(row.getFirstCellNum(), row.getLastCellNum())
            .mapToObj((int i) -> Optional.ofNullable(row.getCell(i)))
            .filter((Optional<Cell> c) -> c.isPresent())
            .map((Optional<Cell> c) -> c.get())
        ;
    }

    private StreamTaker()
    {
        throw new AssertionError();
    }
}
