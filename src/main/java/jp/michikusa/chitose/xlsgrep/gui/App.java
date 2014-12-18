package jp.michikusa.chitose.xlsgrep.gui;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class App
	extends Application
	implements Initializable
{
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage stage)
		throws Exception
	{
		final FXMLLoader loader= new FXMLLoader();

		loader.setLocation(this.getClass().getResource(this.getClass().getSimpleName() + ".fxml"));
		loader.setController(this);

		final Parent root= loader.load();

		stage.setScene(new Scene(root));

		stage.setTitle("xlsgrep - v0.0.4");
		stage.show();
	}

	@Override
	public void initialize(URL url, ResourceBundle bundle)
	{
	}

	@FXML
	private void addFile(ActionEvent event)
	{
		final FileChooser chooser= new FileChooser();

		chooser.setTitle("ファイルを選択してください");
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.getExtensionFilters().add(new ExtensionFilter("Excel ファイル", "*.xls", "*.xlsx"));

		final List<File> files= chooser.showOpenMultipleDialog(null);
		files.forEach((File f) -> {
			this.files.getItems().add(f.toPath().toAbsolutePath());
		});
	}

	@FXML
	private void addDirectory(ActionEvent event)
	{
		final DirectoryChooser chooser= new DirectoryChooser();

		chooser.setTitle("ディレクトリを選択してください");
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));

		final File file= chooser.showDialog(null);
		if(file != null)
		{
			this.files.getItems().add(file.toPath().toAbsolutePath());
		}
	}

	@FXML
	private void removeFile(ActionEvent event)
	{
		final List<Path> paths= this.files.getSelectionModel().getSelectedItems();

		this.files.getItems().removeAll(paths);
	}

	@FXML
	private void doGrep(ActionEvent event)
	{
	}

	@FXML
	private TextField pattern;

	@FXML
	private CheckBox matcherText;

	@FXML
	private CheckBox matcherFormula;

	@FXML
	private CheckBox matcherComment;

	@FXML
	private CheckBox matcherShape;

	@FXML
	private CheckBox matcherSheetName;

	@FXML
	private ListView<Path> files;
}
