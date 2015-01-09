package jp.michikusa.chitose.xlsgrep.javafx;

import java.util.function.Function;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.NonNull;

public final class Dialogs
{
	public static void alert(@NonNull CharSequence message, Window owner, @NonNull Function<AlertDialog, Void> initializer)
	{
		final AlertDialog dialog= AlertDialog.newDialog();

		initializer.apply(dialog);

		final Scene scene= new Scene(dialog);
        final Stage stage= new Stage(StageStyle.UNDECORATED);

        stage.setTitle("警告");
        stage.setScene(scene);
        stage.setWidth(200);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(true);

        stage.show();
	}

	public static void error(@NonNull CharSequence message, Window owner, @NonNull Function<AlertDialog, Void> initializer)
	{
		final AlertDialog dialog= AlertDialog.newDialog();

		initializer.apply(dialog);

		final Scene scene= new Scene(dialog);
        final Stage stage= new Stage(StageStyle.UNDECORATED);

        stage.setTitle("エラー");
        stage.setScene(scene);
        stage.setWidth(200);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(true);

        stage.show();
	}
}
