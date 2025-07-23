package org.example;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class MalusPane {

    private final VBox root = new VBox(8);

    public MalusPane(ObservableList<String> malus) {

        ListView<String> list = new ListView<>(malus);
        Theme.styleListView(list);
        list.setPrefHeight(280);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-font-size:18px;-fx-text-fill:#ff4d4d;");
            }
        });

        TextField txt = new TextField();
        txt.setPromptText("Nouveau malus…");
        Theme.styleTextField(txt);

        Button add = new Button("Ajouter");
        Button edit = new Button("Modifier");
        Button del = new Button("Supprimer");
        Theme.styleButton(add);
        Theme.styleButton(edit);
        Theme.styleButton(del);

        add.disableProperty().bind(txt.textProperty().isEmpty());
        edit.disableProperty().bind(Bindings.or(list.getSelectionModel().selectedItemProperty().isNull(),
                txt.textProperty().isEmpty()));
        del.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());

        Runnable addAction = () -> {
            String v = txt.getText().trim();
            if (!v.isEmpty()) { malus.add(v); txt.clear(); }
        };
        add.setOnAction(e -> addAction.run());
        txt.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) addAction.run(); });

        del.setOnAction(e -> malus.remove(list.getSelectionModel().getSelectedIndex()));

        edit.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                String v = txt.getText().trim();
                if (!v.isEmpty()) { malus.set(idx, v); txt.clear(); }
            }
        });

        Label lbl = new Label("Malus :");
        Theme.styleCapsuleLabel(lbl, "#ff4d4d", "#8b0000");

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, list, txt, new HBox(10, add, edit, del));
    }

    public Node getRootPane() { return root; }
}
