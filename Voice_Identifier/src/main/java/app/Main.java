package app;

import audio.MicListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.SpeakerMatcher;

public class Main extends Application {

    private final MicListener mic = new MicListener();

    @Override
    public void start(Stage stage) {

        /* ================= COMMON STYLES ================= */

        String buttonStyle =
                "-fx-background-color: #6F4E37;" +   // coffee brown
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 20;";

        String blackBoxStyle =
                "-fx-background-color: #111;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-padding: 20;";

        /* ================= REGISTRATION BOX ================= */

        Label regTitle = new Label("📝 Voice Registration");
        regTitle.setFont(Font.font(16));
        regTitle.setStyle("-fx-text-fill: white;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Name");

        Button startRegBtn = new Button("🎙 Start Mic");
        Button stopRegBtn  = new Button("⏹ Stop Mic");

        startRegBtn.setStyle(buttonStyle);
        stopRegBtn.setStyle(buttonStyle);

        Label regStatus = new Label("Status: Waiting...");
        regStatus.setStyle("-fx-text-fill: lightgray;");

        startRegBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                regStatus.setText("⚠ Enter a name");
                return;
            }
            SpeakerMatcher.enableRegister(name);
            mic.startListening();
            regStatus.setText("🔴 Registering voice...");
        });

        stopRegBtn.setOnAction(e -> {
            mic.stopListening();
            regStatus.setText("✅ Voice registered");
        });

        VBox registerBox = new VBox(12,
                regTitle,
                nameField,
                startRegBtn,
                stopRegBtn,
                regStatus
        );
        registerBox.setStyle(blackBoxStyle);

        /* ================= SPEAK & DETECT BOX ================= */

        Label detectTitle = new Label("🎧 Speak & Detect");
        detectTitle.setFont(Font.font(16));
        detectTitle.setStyle("-fx-text-fill: white;");

        Label detectedLabel = new Label("Detected Speaker: ---");
        detectedLabel.setStyle("-fx-text-fill: #FFDAB9; -fx-font-size: 14px;");

        Button speakBtn = new Button("🎤 Speak");
        Button doneBtn  = new Button("✅ Done");

        speakBtn.setStyle(buttonStyle);
        doneBtn.setStyle(buttonStyle);

        speakBtn.setOnAction(e -> mic.startListening());
        doneBtn.setOnAction(e -> mic.stopListening());

        VBox detectBox = new VBox(15,
                detectTitle,
                speakBtn,
                doneBtn,
                detectedLabel
        );
        detectBox.setStyle(blackBoxStyle);

        /* ================= AUTO UPDATE DETECTED NAME ================= */

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    detectedLabel.setText(
                            "🎙 Detected: " +
                            SpeakerMatcher.getLastDetectedSpeaker() +
                            " (" + SpeakerMatcher.getConfidenceText() + ")"
                    );
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        /* ================= ROOT LAYOUT ================= */

        HBox root = new HBox(30, registerBox, detectBox);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        stage.setScene(new Scene(root, 750, 350));
        stage.setTitle("🎤 Voice Identifier");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
