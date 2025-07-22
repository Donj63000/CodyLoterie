package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class Historique extends Stage {

    private static final Path FILE = Path.of("loterie-historique.txt");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObservableList<String> entries = FXCollections.observableArrayList();

    public Historique() {
        setTitle("Historique des tirages");

        ListView<String> view = new ListView<>(entries);
        Theme.styleListView(view);

        Button delete = new Button("Supprimer");
        Theme.styleButton(delete);
        delete.disableProperty().bind(view.getSelectionModel().selectedItemProperty().isNull());
        delete.setOnAction(e -> entries.remove(view.getSelectionModel().getSelectedIndex()));

        VBox box = new VBox(10, view, delete);
        box.setPadding(new Insets(10));
        setScene(new Scene(box, 400, 300));

        load();
        entries.addListener((ListChangeListener<String>) c -> save());
    }

    public void logResult(String malus)   { entries.add(time() + " - Malus attribué : " + malus); }
    public void logBonus(Participant p, String bonus) { entries.add(time() + " - " + p.getName() + " reçoit le bonus : " + bonus); }

    private String time() { return LocalDateTime.now().format(FMT); }

    private void load() {
        try {
            if (Files.exists(FILE)) entries.setAll(Files.readAllLines(FILE, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            System.err.println("Load history failed: " + ex.getMessage());
        }
    }

    private void save() {
        try {
            Files.writeString(FILE, String.join(System.lineSeparator(), entries),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            System.err.println("Save history failed: " + ex.getMessage());
        }
    }
}
