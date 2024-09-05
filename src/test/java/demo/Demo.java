package demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.markdowneditorfx.editor.MarkdownEditorPane;

public class Demo extends Application {

	@Override
	public void start(Stage primaryStage) {
		var root = new BorderPane();
		MarkdownEditorPane markdownEditorPane = new MarkdownEditorPane();
		root.setCenter(markdownEditorPane.getNode());

		var scene = new Scene(root, 1280.0, 800.0);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(Demo.class, args);
	}
}
