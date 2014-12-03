package jp.michikusa.chitose.xlsgrep.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

@EqualsAndHashCode
@ToString
public class CellReference
{
	public CellReference(@NonNull Sheet sheet, int rownum, int cellnum)
	{
		this.sheet= sheet;
		this.rownum= rownum;
		this.cellnum= cellnum;
	}

	public CellReference(@NonNull Cell cell)
	{
		this.sheet= cell.getSheet();
		this.rownum= cell.getRowIndex();
		this.cellnum= cell.getColumnIndex();
	}

	@Getter @NonNull
	private final Sheet sheet;

	@Getter
	private final int rownum;
	
	@Getter
	private final int cellnum;
}
