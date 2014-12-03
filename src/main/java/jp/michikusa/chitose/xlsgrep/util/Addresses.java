package jp.michikusa.chitose.xlsgrep.util;

import org.apache.poi.ss.usermodel.Cell;

public enum Addresses
{
    A1
    {
        @Override
        public String format(int rownum, int cellnum) {
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
        public String format(int rownum, int cellnum) {
            return "R" + (rownum + 1) + "C" + (cellnum + 1);
        }
    },
    ;

    public abstract String format(int rownum, int cellnum);

    public String format(Cell cell)
    {
        return this.format(cell.getRowIndex(), cell.getColumnIndex());
    }
}
