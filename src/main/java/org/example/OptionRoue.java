package org.example;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class OptionRoue extends Stage {

    private static double spinDuration = 4.0;

    public OptionRoue() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Options de la roue");

        Label lbl = new Label("Durée de rotation (s) :");

        TextField txt = new TextField(String.valueOf(spinDuration));
        Theme.styleTextField(txt);

        Button save = new Button("Enregistrer");
        Theme.styleButton(save);
        save.setOnAction(e -> {
            try {
                double v = Double.parseDouble(txt.getText().trim().replace(',', '.'));
                if (v > 0) { spinDuration = v; close(); }
            } catch (NumberFormatException ignored) { }
        });

        VBox root = new VBox(10, lbl, txt, save);
        root.setPadding(new Insets(10));

        setScene(new Scene(root, 300, 120));
    }

    public static double getSpinDuration() { return spinDuration; }
}
