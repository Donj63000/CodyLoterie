package org.example;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Fenêtre optionnelle pour régler la configuration
 * de la roue (durée de rotation).
 */
public class OptionRoue extends Stage {

    // Durée de rotation (3.0 s par défaut)
    private static double spinDuration = 3.0;

    public OptionRoue() {
        setTitle("Options de la roue");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        // Champ pour la durée de rotation
        Label lblDuration = new Label("Durée de rotation (secondes) :");
        TextField txtDuration = new TextField(String.valueOf(spinDuration));

        // Bouton pour enregistrer la valeur
        Button btnSave = new Button("Enregistrer");
        btnSave.setOnAction(e -> {
            try {
                // Lecture de la durée de rotation
                double dur = Double.parseDouble(txtDuration.getText().trim());
                if (dur > 0) {
                    spinDuration = dur;
                }

                // On ferme la fenêtre après sauvegarde
                close();

            } catch (NumberFormatException ex) {
                // Gère l'erreur éventuelle, on peut ignorer ou afficher un message
            }
        });

        // Style Material sur le bouton
        Theme.styleButton(btnSave);

        root.getChildren().addAll(lblDuration, txtDuration, btnSave);

        Scene scene = new Scene(root, 300, 120);
        setScene(scene);
    }

    // Méthode statique pour récupérer la config de la durée de rotation (en secondes)
    public static double getSpinDuration() {
        return spinDuration;
    }
}
