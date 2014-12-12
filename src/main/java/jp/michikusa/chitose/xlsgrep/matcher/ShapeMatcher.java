package jp.michikusa.chitose.xlsgrep.matcher;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.util.CellReference;
import jp.michikusa.chitose.xlsgrep.util.StreamTaker;

import lombok.NonNull;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeMatcher
    implements Matcher
{
    public ShapeMatcher(@NonNull Workbook workbook)
    {
        this.workbook= workbook;
    }

    @Override
    public Stream<MatchResult> matches(@NonNull Pattern pattern)
    {
        if(this.workbook instanceof HSSFWorkbook)
        {
            return this.matches((HSSFWorkbook)this.workbook, pattern);
        }
        else if(this.workbook instanceof XSSFWorkbook)
        {
            return this.matches((XSSFWorkbook)this.workbook, pattern);
        }
        else
        {
            logger.warn("Unsupported workbook type `{}'.", this.workbook.getClass());
            return Stream.empty();
        }
    }

    // XXX: work around, getting a NPE when a shape has no text
    private static Optional<HSSFRichTextString> getString(HSSFSimpleShape shape)
    {
        try
        {
            return Optional.ofNullable(shape.getString());
        }
        catch(NullPointerException e)
        {
            return Optional.empty();
        }
    }

    private Stream<MatchResult> matches(@NonNull HSSFWorkbook book, @NonNull Pattern pattern)
    {
        return StreamTaker.sheets(book)
            .map((Sheet s) -> { return this.matches((HSSFSheet)s, pattern); })
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    private Stream<MatchResult> matches(@NonNull HSSFSheet sheet, @NonNull Pattern pattern)
    {
        if(sheet.getDrawingPatriarch() == null)
        {
            return Stream.empty();
        }

        final Stream<HSSFPatriarch> patriarches= Stream.of(sheet.getDrawingPatriarch());

        final Stream<HSSFShape> shapes= patriarches
            .map((HSSFPatriarch p) -> { return p.getChildren().stream(); })
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;

        final Stream<HSSFSimpleShape> sshapes= shapes
            .filter((HSSFShape s) -> { return s instanceof HSSFSimpleShape; })
            .map((HSSFShape s) -> { return (HSSFSimpleShape)s; })
        ;

        return sshapes
            .filter((HSSFSimpleShape s) -> { return getString(s).isPresent(); })
            .filter((HSSFSimpleShape s) -> { return pattern.matcher(s.getString().getString()).find(); })
            .map((HSSFSimpleShape s) -> {
                final CellReference cellref= new CellReference(sheet, s);

                return new MatchResult(cellref);
            })
        ;
    }

    private Stream<MatchResult> matches(@NonNull XSSFWorkbook book, @NonNull Pattern pattern)
    {
        return StreamTaker.sheets(book)
            .map((Sheet s) -> { return this.matches((XSSFSheet)s, pattern); })
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;
    }

    private Stream<MatchResult> matches(@NonNull XSSFSheet sheet, @NonNull Pattern pattern)
    {
        final Stream<POIXMLDocumentPart> parts= sheet.getRelations().stream();

        final Stream<XSSFDrawing> drawings= parts
            .filter((POIXMLDocumentPart p) -> { return p instanceof XSSFDrawing; })
            .map((POIXMLDocumentPart p) -> { return (XSSFDrawing)p; })
        ;

        final Stream<XSSFShape> shapes= drawings
            .map((XSSFDrawing d) -> { return d.getShapes().stream(); })
            .reduce(Stream::concat)
            .orElse(Stream.empty())
        ;

        final Stream<XSSFSimpleShape> sshapes= shapes
            .filter((XSSFShape s) -> { return s instanceof XSSFSimpleShape; })
            .map((XSSFShape s) -> { return (XSSFSimpleShape)s; })
        ;

        return sshapes
            .filter((XSSFSimpleShape s) -> { return pattern.matcher(s.getText()).find(); })
            .map((XSSFSimpleShape s) -> {
                final CellReference cellref= new CellReference(sheet, s);

                return new MatchResult(cellref);
            })
        ;
    }

    private static final Logger logger= LoggerFactory.getLogger(ShapeMatcher.class);

    private final Workbook workbook;
}
