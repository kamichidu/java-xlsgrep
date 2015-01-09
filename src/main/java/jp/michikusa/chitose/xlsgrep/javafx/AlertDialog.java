package jp.michikusa.chitose.xlsgrep.javafx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

class AlertDialog
    extends AnchorPane
    implements Initializable
{
    public static AlertDialog newDialog()
    {
    	final AlertDialog dialog= new AlertDialog();

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
	}
}
