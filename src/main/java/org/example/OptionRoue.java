package org.example;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OptionRoue extends Stage {

    private static double spinDuration = 4.0;

    public OptionRoue() {
        setTitle("Options de la roue");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        Label lblDuration = new Label("Durée de rotation (secondes) :");
        TextField txtDuration = new TextField(String.valueOf(spinDuration));

        Button btnSave = new Button("Enregistrer");
        btnSave.setOnAction(e -> {
            try {
                double dur = Double.parseDouble(txtDuration.getText().trim());
                if (dur > 0) spinDuration = dur;
                close();
            } catch (NumberFormatException ignored) {
            }
        });

        Theme.styleButton(btnSave);
        root.getChildren().addAll(lblDuration, txtDuration, btnSave);
        setScene(new Scene(root, 300, 120));
    }

    public static double getSpinDuration() {
        return spinDuration;
    }
}
