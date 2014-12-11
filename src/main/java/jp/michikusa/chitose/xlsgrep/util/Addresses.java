package jp.michikusa.chitose.xlsgrep.util;

import lombok.NonNull;

import org.apache.poi.hssf.usermodel.HSSFAnchor;
import org.apache.poi.hssf.usermodel.HSSFChildAnchor;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFAnchor;
import org.apache.poi.xssf.usermodel.XSSFChildAnchor;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFShape;

public enum Addresses
{
    A1
    {
        @Override
        public String format(int rownum, int cellnum)
        {
            final StringBuilder buffer= new StringBuilder();

            cellnum+= 1;
            while(cellnum > 0)
            {
                buffer.append((char)('A' + (cellnum % 26 - 1)));
                cellnum/= 26;
            }

            return buffer.toString() + (rownum + 1);
        }
    },
    R1C1
    {
        @Override
        public String format(int rownum, int cellnum)
        {
            return "R" + (rownum + 1) + "C" + (cellnum + 1);
        }
    },
    ;

    public abstract String format(int rownum, int cellnum);

    public String format(@NonNull HSSFShape shape)
    {
        final HSSFAnchor anchor= shape.getAnchor();

        if(anchor instanceof HSSFClientAnchor)
        {
            final HSSFClientAnchor a= (HSSFClientAnchor)anchor;

            return this.format(a.getRow1(), a.getCol1());
        }
        else if(anchor instanceof HSSFChildAnchor)
        {
            return this.format(shape.getParent());
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    public String format(@NonNull XSSFShape shape)
    {
        final XSSFAnchor anchor= shape.getAnchor();

        if(anchor instanceof XSSFClientAnchor)
        {
            final XSSFClientAnchor a= (XSSFClientAnchor) anchor;

            return this.format(a.getRow1(), a.getCol1());
        }
        else if(anchor instanceof XSSFChildAnchor)
        {
            return this.format(shape.getParent());
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    public String format(@NonNull Cell cell)
    {
        return this.format(cell.getRowIndex(), cell.getColumnIndex());
    }
}
