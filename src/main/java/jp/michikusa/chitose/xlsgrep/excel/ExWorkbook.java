package jp.michikusa.chitose.xlsgrep.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import lombok.NonNull;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExWorkbook
    implements Workbook
{
    public ExWorkbook(@NonNull Path filepath)
        throws InvalidFormatException, IOException
    {
        this.delegate= WorkbookFactory.create(filepath.toFile());
        this.filepath= filepath;
    }

    public Path getFilepath()
    {
        return this.filepath;
    }

    @Override
    public int getActiveSheetIndex()
    {
        return this.delegate.getActiveSheetIndex();
    }

    @Override
    public void setActiveSheet(int sheetIndex)
    {
        this.delegate.setActiveSheet(sheetIndex);
    }

    @Override
    public int getFirstVisibleTab()
    {
        return this.delegate.getFirstVisibleTab();
    }

    @Override
    public void setFirstVisibleTab(int sheetIndex)
    {
        this.delegate.setFirstVisibleTab(sheetIndex);
    }

    @Override
    public void setSheetOrder(String sheetname, int pos)
    {
        this.delegate.setSheetOrder(sheetname, pos);
    }

    @Override
    public void setSelectedTab(int index)
    {
        this.delegate.setSelectedTab(index);
    }

    @Override
    public void setSheetName(int sheet, String name)
    {
        this.delegate.setSheetName(sheet, name);
    }

    @Override
    public String getSheetName(int sheet)
    {
        return this.delegate.getSheetName(sheet);
    }

    @Override
    public int getSheetIndex(String name)
    {
        return this.delegate.getSheetIndex(name);
    }

    @Override
    public int getSheetIndex(Sheet sheet)
    {
        return this.delegate.getSheetIndex(sheet);
    }

    @Override
    public Sheet createSheet()
    {
        return this.delegate.createSheet();
    }

    @Override
    public Sheet createSheet(String sheetname)
    {
        return this.delegate.createSheet(sheetname);
    }

    @Override
    public Sheet cloneSheet(int sheetNum)
    {
        return this.delegate.cloneSheet(sheetNum);
    }

    @Override
    public int getNumberOfSheets()
    {
        return this.delegate.getNumberOfSheets();
    }

    @Override
    public Sheet getSheetAt(int index)
    {
        return this.delegate.getSheetAt(index);
    }

    @Override
    public Sheet getSheet(String name)
    {
        return this.delegate.getSheet(name);
    }

    @Override
    public void removeSheetAt(int index)
    {
        this.delegate.removeSheetAt(index);
    }

    @Override
    public void setRepeatingRowsAndColumns(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow)
    {
        this.delegate.setRepeatingRowsAndColumns(sheetIndex, startColumn, endColumn, startRow, endRow);
    }

    @Override
    public Font createFont()
    {
        return this.delegate.createFont();
    }

    @Override
    public Font findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline)
    {
        return this.delegate.findFont(boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }

    @Override
    public short getNumberOfFonts()
    {
        return this.delegate.getNumberOfFonts();
    }

    @Override
    public Font getFontAt(short idx)
    {
        return this.delegate.getFontAt(idx);
    }

    @Override
    public CellStyle createCellStyle()
    {
        return this.delegate.createCellStyle();
    }

    @Override
    public short getNumCellStyles()
    {
        return this.delegate.getNumCellStyles();
    }

    @Override
    public CellStyle getCellStyleAt(short idx)
    {
        return this.delegate.getCellStyleAt(idx);
    }

    @Override
    public void write(OutputStream stream)
        throws IOException
    {
        this.delegate.write(stream);
    }

    @Override
    public void close()
        throws IOException
    {
        this.delegate.close();
    }

    @Override
    public int getNumberOfNames()
    {
        return this.delegate.getNumberOfNames();
    }

    @Override
    public Name getName(String name)
    {
        return this.delegate.getName(name);
    }

    @Override
    public Name getNameAt(int nameIndex)
    {
        return this.delegate.getNameAt(nameIndex);
    }

    @Override
    public Name createName()
    {
        return this.delegate.createName();
    }

    @Override
    public int getNameIndex(String name)
    {
        return this.delegate.getNameIndex(name);
    }

    @Override
    public void removeName(int index)
    {
        this.delegate.removeName(index);
    }

    @Override
    public void removeName(String name)
    {
        this.delegate.removeName(name);
    }

    @Override
    public int linkExternalWorkbook(String name, Workbook workbook)
    {
        return this.delegate.linkExternalWorkbook(name, workbook);
    }

    @Override
    public void setPrintArea(int sheetIndex, String reference)
    {
        this.delegate.setPrintArea(sheetIndex, reference);
    }

    @Override
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow)
    {
        this.delegate.setPrintArea(sheetIndex, startColumn, endColumn, startRow, endRow);
    }

    @Override
    public String getPrintArea(int sheetIndex)
    {
        return this.delegate.getPrintArea(sheetIndex);
    }

    @Override
    public void removePrintArea(int sheetIndex)
    {
        this.delegate.removePrintArea(sheetIndex);
    }

    @Override
    public MissingCellPolicy getMissingCellPolicy()
    {
        return this.delegate.getMissingCellPolicy();
    }

    @Override
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy)
    {
        this.delegate.setMissingCellPolicy(missingCellPolicy);
    }

    @Override
    public DataFormat createDataFormat()
    {
        return this.delegate.createDataFormat();
    }

    @Override
    public int addPicture(byte[] pictureData, int format)
    {
        return this.delegate.addPicture(pictureData, format);
    }

    @Override
    public List<? extends PictureData> getAllPictures()
    {
        return this.delegate.getAllPictures();
    }

    @Override
    public CreationHelper getCreationHelper()
    {
        return this.delegate.getCreationHelper();
    }

    @Override
    public boolean isHidden()
    {
        return this.delegate.isHidden();
    }

    @Override
    public void setHidden(boolean hiddenFlag)
    {
        this.delegate.setHidden(hiddenFlag);
    }

    @Override
    public boolean isSheetHidden(int sheetIx)
    {
        return this.delegate.isSheetHidden(sheetIx);
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx)
    {
        return this.delegate.isSheetVeryHidden(sheetIx);
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden)
    {
        this.delegate.setSheetHidden(sheetIx, hidden);
    }

    @Override
    public void setSheetHidden(int sheetIx, int hidden)
    {
        this.delegate.setSheetHidden(sheetIx, hidden);
    }

    @Override
    public void addToolPack(UDFFinder toopack)
    {
        this.delegate.addToolPack(toopack);
    }

    @Override
    public void setForceFormulaRecalculation(boolean value)
    {
        this.delegate.setForceFormulaRecalculation(value);
    }

    @Override
    public boolean getForceFormulaRecalculation()
    {
        return this.delegate.getForceFormulaRecalculation();
    }

    private final Workbook delegate;

    private final Path filepath;
}
