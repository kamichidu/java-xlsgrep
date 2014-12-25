package jp.michikusa.chitose.xlsgrep.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import jp.michikusa.chitose.xlsgrep.MatchResult;
import jp.michikusa.chitose.xlsgrep.javafx.ProgressDialog;
import jp.michikusa.chitose.xlsgrep.matcher.CellCommentMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellFormulaMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.CellTextMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.Matcher;
import jp.michikusa.chitose.xlsgrep.matcher.ShapeMatcher;
import jp.michikusa.chitose.xlsgrep.matcher.SheetNameMatcher;
import jp.michikusa.chitose.xlsgrep.util.FileWalker;
import jp.michikusa.chitose.xlsgrep.util.MatchResultComparator;

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

        stage.setTitle(String.format("%s - %s", this.bundle.getString("app.name"), this.bundle.getString("app.version")));
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
        final App that= this;
        final Task<Iterable<MatchResult>> task= new Task<Iterable<MatchResult>>() {
            @Override
            protected Iterable<MatchResult> call()
                throws Exception
            {
                this.updateProgress(0, 0);
                this.updateMessage("ファイル一覧の作成中...");

                final Collection<Path> paths= that.files();
                if(paths.isEmpty())
                {
                    return Collections.emptyList();
                }

                this.updateProgress(0, paths.size());

                final List<MatchResult> results= new LinkedList<>();
                int done= 0;
                for(final Path path : paths)
                {
                    this.updateMessage(String.format("検索しています...\n%s", path.getFileName()));
                    that.matches(path, that.matchers()).forEach(results::add);
                    this.updateProgress(++done, paths.size());
                }

                return results;
            }
        };

        try
        {
            final TreeItem<CharSequence> root= new TreeItem<>("検索結果");

            this.result.setRoot(root);

            Executors.newCachedThreadPool().execute(task);

            final Scene scene;
            {
                final ProgressDialog progress= ProgressDialog.newProgressDialog();

                progress.progressProperty().bind(task.progressProperty());
                progress.textProperty().bind(task.messageProperty());;

                scene= new Scene(progress);
            }
//          final Stage stage= new Stage(StageStyle.UTILITY);
            final Stage stage= new Stage(StageStyle.UNDECORATED);

            stage.setTitle("進捗状況");
            stage.setScene(scene);
            stage.setWidth(200);
            stage.initOwner(this.result.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent evt) -> { stage.close(); });
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, (WorkerStateEvent evt) -> { stage.close(); });
            stage.showAndWait();

            try
            {
                this.result.setVisible(false);

                final Iterable<MatchResult> results= task.get();
                final Set<MatchResult> sorted= new TreeSet<>(new MatchResultComparator());
                for(final MatchResult r : results)
                {
                    sorted.add(r);
                }
                for(final MatchResult r : sorted)
                {
                    final TreeItem<CharSequence> parent= this.findOrCreateSheetNode(root, r);
                    final TreeItem<CharSequence> item= new TreeItem<>();

                    item.setValue(String.format("%s - %s", r.getCellAddress(), r.getMatched()));

                    if(!parent.isExpanded())
                    {
                        parent.setExpanded(true);
                    }
                    parent.getChildren().add(item);
                }
            }
            finally
            {
                this.result.setVisible(true);
            }
        }
        catch(Exception e)
        {
            logger.error("Something wrong while executing grep.", e);
        }
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

        fileNode.setExpanded(true);
        fileNode.getChildren().add(sheetNode);

        return sheetNode;
    }

    private TreeItem<CharSequence> findOrCreateFileNode(TreeItem<CharSequence> base, MatchResult key)
    {
        for(final TreeItem<CharSequence> fileNode : base.getChildren())
        {
            if(fileNode.getValue().toString().equals(key.getFilepath().get().toAbsolutePath().toString()))
            {
                return fileNode;
            }
        }

        final TreeItem<CharSequence> fileNode= new TreeItem<>(key.getFilepath().get().toAbsolutePath().toString());

        base.setExpanded(true);
        base.getChildren().add(fileNode);

        return fileNode;
    }

    private Collection<Path> files()
    {
        final FileWalker walker= new FileWalker();

        this.files.getItems().forEach((Path p) -> {
            if(p.toFile().isFile())
            {
                walker.addExpr(p.toFile().getAbsolutePath());
            }
            else
            {
                walker.addExpr(p.toFile().getAbsolutePath() + "/**/*.{xls,xlsx}");
            }
        });

        final Set<Path> files= new TreeSet<>();
        try
        {
            walker.walk(new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
                {
                    return dir.toFile().canRead()
                        ? FileVisitResult.CONTINUE
                        : FileVisitResult.SKIP_SUBTREE
                    ;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
                {
                    files.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException
                {
                    if(exc != null)
                    {
                        logger.warn("It's okay but note for debug.", exc);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(IOException e)
        {
            // IOException was suppressed
            throw new AssertionError(e);
        }
        return files;
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
            final Pattern pattern= this.regexSearch.isSelected()
                ? Pattern.compile(this.pattern.getText())
                : Pattern.compile(Pattern.quote(this.pattern.getText()))
            ;
            return matchers
                .map((Matcher m) -> { return m.matches(workbook, pattern); })
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

    private final ResourceBundle bundle= ResourceBundle.getBundle(App.class.getCanonicalName());

    @FXML
    private TextField pattern;

    @FXML
    private CheckBox regexSearch;

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
