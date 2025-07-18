package org.example;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MalusPane {

    private final ObservableList<String> malus;
    private final ListView<String> list;
    private final VBox root = new VBox(8);

    public MalusPane(ObservableList<String> malus) {
        this.malus = malus;

        list = new ListView<>(malus);
        Theme.styleListView(list);
        list.setPrefHeight(280);

        TextField txt = new TextField();
        txt.setPromptText("Nouveau malus…");
        Theme.styleTextField(txt);

        Button add = new Button("Ajouter");   Theme.styleButton(add);
        Button del = new Button("Supprimer"); Theme.styleButton(del);
        Button edit= new Button("Modifier");  Theme.styleButton(edit);

        add.setOnAction(e -> {
            String v = txt.getText().trim();
            if (!v.isEmpty()) { malus.add(v); txt.clear(); }
        });

        del.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) malus.remove(idx);
        });

        edit.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                String v = txt.getText().trim();
                if (!v.isEmpty()) { malus.set(idx, v); txt.clear(); }
            }
        });

        Label lbl = new Label("Malus :");
        Theme.styleCapsuleLabel(lbl, "#ff9a9e", "#fad0c4");

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, list, txt, new HBox(10, add, edit, del));
    }

    public Node getRootPane() { return root; }
}
