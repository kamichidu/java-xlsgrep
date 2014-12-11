package jp.michikusa.chitose.xlsgrep.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFShape;

@EqualsAndHashCode
@ToString
public class CellReference
{
    public CellReference(@NonNull Sheet sheet, int rownum, int cellnum)
    {
        this.sheet= sheet;
        this.address= Addresses.A1.format(rownum, cellnum);
    }

    public CellReference(@NonNull Cell cell)
    {
        this.sheet= cell.getSheet();
        this.address= Addresses.A1.format(cell);
    }

    public CellReference(@NonNull Sheet sheet, @NonNull HSSFShape shape)
    {
        this.sheet= sheet;
        this.address= Addresses.A1.format(shape);
    }

    public CellReference(@NonNull Sheet sheet, @NonNull XSSFShape shape)
    {
        this.sheet= sheet;
        this.address= Addresses.A1.format(shape);
    }

    @Getter @NonNull
    private final Sheet sheet;

    @Getter
    private final String address;
}
