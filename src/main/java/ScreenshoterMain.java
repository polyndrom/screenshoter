import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

public class ScreenshoterMain extends Application {

    public static final int MAIN_WINDOW_WIDTH = 300;
    public static final int MAIN_WINDOW_HEIGHT = 200;

    private Stage mainWindow;

    public void start(Stage stage) {
        mainWindow = stage;

        File mainDirectory = new File(System.getProperty("user.home") + "/screenshoter/");
        if (!mainDirectory.exists()) {
            mainDirectory.mkdir();
        }

        mainWindow.setOnShown(event -> {
            setupScene();
        });

        stage.setTitle("Screenshoter");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void setupScene() {
        CheckBox checkBox = new CheckBox("Hide this window?");
        checkBox.setAllowIndeterminate(false);

        Slider delaySlider = new Slider(0.0, 30.0, 0.0);
        delaySlider.setPrefWidth(MAIN_WINDOW_WIDTH - 20);
        delaySlider.setMaxWidth(MAIN_WINDOW_WIDTH - 20);
        delaySlider.setMinWidth(MAIN_WINDOW_WIDTH - 20);
        delaySlider.setMinorTickCount(4);
        delaySlider.setMajorTickUnit(5.0);
        delaySlider.setBlockIncrement(1.0);
        delaySlider.setShowTickMarks(true);
        delaySlider.setShowTickLabels(true);
        delaySlider.setSnapToTicks(true);

        Label label = new Label("Screenshot delay");
        label.setFont(new Font(14));

        Button takeScreenshotButton = new Button("Take screenshot!");

        VBox vBox = new VBox(label, delaySlider, takeScreenshotButton, checkBox);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(15);

        takeScreenshotButton.setOnAction(event -> {
            int delay = (int) delaySlider.getValue();
            startDelayTimer(delay, delaySlider::setValue, () -> {
                if (checkBox.isSelected()) {
                    mainWindow.hide();
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        takeScreenshot();
                    });
                } else {
                    takeScreenshot();
                    mainWindow.hide();
                }
            });
        });

        BorderPane root = new BorderPane(vBox);
        Scene scene = new Scene(root, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        mainWindow.setScene(scene);
    }

    private void startDelayTimer(int delay, Consumer<Integer> onUpdate, Runnable onFinish) {
        if (delay == 0) {
            onFinish.run();
            return;
        }
        final int[] k = { delay };
        Timeline delayTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            k[0]--;
            onUpdate.accept(k[0]);
        }));
        delayTimer.setCycleCount(delay);
        delayTimer.setOnFinished(event -> {
            onUpdate.accept(k[0]);
            Platform.runLater(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onFinish.run();
            });
        });
        delayTimer.play();
    }

    private void takeScreenshot() {
        try {
            Robot robot = new Robot();
            BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            BufferedImage scaledScreenshot = new BufferedImage(EditorWindow.windowWidth, EditorWindow.windowHeight - 50, BufferedImage.TYPE_INT_ARGB);
            AffineTransform transform = AffineTransform.getScaleInstance(0.8, 0.8);
            AffineTransformOp scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
            scaledScreenshot = scaleOp.filter(screenshot, scaledScreenshot);
            new EditorWindow(mainWindow, SwingFXUtils.toFXImage(scaledScreenshot, null));
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
