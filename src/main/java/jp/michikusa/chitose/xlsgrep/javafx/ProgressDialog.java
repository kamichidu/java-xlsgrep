package jp.michikusa.chitose.xlsgrep.javafx;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;

public class ProgressDialog
    extends AnchorPane
    implements Initializable
{
    public static ProgressDialog newProgressDialog()
    {
        final ProgressDialog dialog= new ProgressDialog();

        final FXMLLoader loader= new FXMLLoader();

        loader.setLocation(dialog.getClass().getResource(dialog.getClass().getSimpleName() + ".fxml"));
        loader.setController(dialog);
        loader.setRoot(dialog);

        try
        {
            return loader.load();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle)
    {
        final ProgressDialog that= this;
        this.progress.progressProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue)
            {
                final NumberFormat fmt= NumberFormat.getPercentInstance();

                that.indicator.setText(fmt.format(newValue.doubleValue()));
            }
        });
    }

    public DoubleProperty progressProperty()
    {
        return this.progress.progressProperty();
    }

    public StringProperty textProperty()
    {
        return this.message.textProperty();
    }

    private ProgressDialog()
    {
    }

    @FXML
    private ProgressBar progress;

    @FXML
    private Label message;

    @FXML
    private Label indicator;
}
