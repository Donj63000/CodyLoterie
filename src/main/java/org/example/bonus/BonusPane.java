package org.example.bonus;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.Theme;

public final class BonusPane {

    private final VBox root = new VBox(8);

    public BonusPane(ObservableList<Bonus> bonus) {

        ListView<Bonus> list = new ListView<>(bonus);
        Theme.styleListView(list);
        list.setPrefHeight(280);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Bonus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
                setStyle(empty ? "" : "-fx-font-size:18px;-fx-text-fill:#00e3ae;");
            }
        });

        TextField txt = new TextField();
        txt.setPromptText("Nouveau bonus…");
        Theme.styleTextField(txt);

        Button add  = new Button("Ajouter");
        Button edit = new Button("Modifier");
        Button del  = new Button("Supprimer");
        Theme.styleButton(add);
        Theme.styleButton(edit);
        Theme.styleButton(del);

        add.disableProperty().bind(txt.textProperty().isEmpty());
        edit.disableProperty().bind(Bindings.or(list.getSelectionModel().selectedItemProperty().isNull(),
                txt.textProperty().isEmpty()));
        del.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());

        Runnable addAction = () -> {
            String v = txt.getText().trim();
            if (!v.isEmpty()) { bonus.add(new Bonus(v)); txt.clear(); }
        };
        add.setOnAction(e -> addAction.run());
        txt.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) addAction.run(); });

        del.setOnAction(e -> bonus.remove(list.getSelectionModel().getSelectedIndex()));

        edit.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) { bonus.set(idx, new Bonus(txt.getText().trim())); txt.clear(); }
        });

        Label lbl = new Label("Bonus :");
        Theme.styleCapsuleLabel(lbl, "#9be15d", "#00e3ae");

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, list, txt, new HBox(10, add, edit, del));
    }

    public Node getRootPane() { return root; }
}
