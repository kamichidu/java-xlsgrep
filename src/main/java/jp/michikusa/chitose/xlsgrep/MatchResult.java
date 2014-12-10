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
    public MatchResult(@NonNull CellReference ref)
    {
        this.cellRef= ref;
    }

    public CharSequence getSheetName()
    {
        return this.cellRef.getSheet().getSheetName();
    }

    public CharSequence getCellAddress()
    {
        return Addresses.A1.format(this.cellRef.getRownum(), this.cellRef.getCellnum());
    }

    public void setFilepath(Path filepath)
    {
        this.filepath= Optional.ofNullable(filepath);
    }

    @Getter
    private Optional<Path> filepath;

    @Getter
    private final CellReference cellRef;
}
