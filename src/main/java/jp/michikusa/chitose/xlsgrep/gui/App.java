package jp.michikusa.chitose.xlsgrep.gui;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.matcher.CellCommentMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellFormulaMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellTextMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import jp.michikusa.chitose.xlsgrep.matcher.ShapeMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.SheetNameMatcher;
import lombok.NonNull;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		final Stream<Path> paths= this.files();

		final Stream<MatchResult> results= paths
			.map((Path p) -> { return this.matches(p, this.matchers()); })
			.reduce(Stream::concat)
			.orElse(Stream.empty())
		;

		final TreeItem<CharSequence> root= new TreeItem<>("検索結果");
		this.result.setRoot(root);
		results.forEach((MatchResult r) -> {
			final TreeItem<CharSequence> parent= this.findOrCreateSheetNode(root, r);

			final TreeItem<CharSequence> item= new TreeItem<>();

			item.setValue(String.format("%s - %s", r.getCellAddress(), r.getMatched()));

			parent.getChildren().add(item);
		});
	}

	private TreeItem<CharSequence> findOrCreateSheetNode(TreeItem<CharSequence> base, MatchResult key)
	{
		final TreeItem<CharSequence> fileNode= this.findOrCreateFileNode(base, key);
		for(final TreeItem<CharSequence> sheetNode : fileNode.getChildren())
		{
			if(sheetNode.getValue().toString().equals(key.getSheetName().toString()))
			{
				return sheetNode;
			}
		}

		final TreeItem<CharSequence> sheetNode= new TreeItem<>(key.getSheetName());

		fileNode.getChildren().add(sheetNode);

		return sheetNode;
	}

	private TreeItem<CharSequence> findOrCreateFileNode(TreeItem<CharSequence> base, MatchResult key)
	{
		for(final TreeItem<CharSequence> fileNode : base.getChildren())
		{
			if(fileNode.getValue().toString().equals(key.getSheetName().toString()))
			{
				return fileNode;
			}
		}

		final TreeItem<CharSequence> fileNode= new TreeItem<>(key.getFilepath().get().toFile().getAbsolutePath());

		base.getChildren().add(fileNode);

		return fileNode;
	}

	private Stream<Path> files()
	{
		final Stream<Path> paths= this.files.getItems().stream()
			.<Stream<Path>>map((Path p) -> {
				if(p.toFile().isFile())
				{
					return Stream.of(p);
				}
				else
				{
					try
					{
						return Files.walk(p);
					}
					catch(IOException e)
					{
						logger.error("Couldn't walk directory.", e);
						return Stream.empty();
					}
				}
			})
			.reduce(Stream::concat)
			.orElse(Stream.empty())
		;

		final FilenameFilter fnameFilter= new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xls") || name.endsWith(".xlsx");
			}
		};
		return paths
			.filter((Path p) -> { return p.toFile().isFile(); })
			.filter((Path p) -> { return fnameFilter.accept(p.getParent().toFile(), p.toFile().getName()); })
		;
	}

	private Stream<Matcher> matchers()
	{
		Stream.Builder<Matcher> matchers= Stream.builder();

		if(this.matcherText.isSelected())
		{
			matchers.add(new CellTextMatcher());
		}
		if(this.matcherFormula.isSelected())
		{
			matchers.add(new CellFormulaMatcher());
		}
		if(this.matcherComment.isSelected())
		{
			matchers.add(new CellCommentMatcher());
		}
		if(this.matcherShape.isSelected())
		{
			matchers.add(new ShapeMatcher());
		}
		if(this.matcherSheetName.isSelected())
		{
			matchers.add(new SheetNameMatcher());
		}

		return matchers.build();
	}

    private Stream<MatchResult> matches(@NonNull Path path, @NonNull Stream<? extends Matcher> matchers)
    {
        try(final Workbook workbook= WorkbookFactory.create(path.toFile()))
        {
        	return matchers
        		.map((Matcher m) -> { return m.matches(workbook, Pattern.compile(this.pattern.getText())); })
        		.reduce(Stream::concat)
        		.orElse(Stream.empty())
        		.map((MatchResult r) -> {
        			r.setFilepath(path);
        			return r;
        		})
        	;
        }
        catch(InvalidFormatException | IllegalArgumentException | IOException e)
        {
        	logger.warn("`{}' is not an Excel file.", path.toFile().getAbsolutePath());
            return Stream.empty();
        }
        catch(Exception e)
        {
            logger.error("Something wrong.", e);
            return Stream.empty();
        }
    }

	private static final Logger logger= LoggerFactory.getLogger(App.class);

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

	@FXML
	private TreeView<CharSequence> result;
}
