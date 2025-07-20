package org.example.bonus;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.Theme;

public class BonusPane {

    private final ObservableList<Bonus> bonus;
    private final ListView<Bonus> list;
    private final VBox root = new VBox(8);

    public BonusPane(ObservableList<Bonus> bonus) {
        this.bonus = bonus;

        list = new ListView<>(bonus);
        Theme.styleListView(list);
        list.setPrefHeight(280);

        TextField txt = new TextField();
        txt.setPromptText("Nouveau bonus…");
        Theme.styleTextField(txt);

        Button add = new Button("Ajouter");   Theme.styleButton(add);
        Button del = new Button("Supprimer"); Theme.styleButton(del);
        Button edit= new Button("Modifier");  Theme.styleButton(edit);

        add.setOnAction(e -> {
            String v = txt.getText().trim();
            if (!v.isEmpty()) { bonus.add(new Bonus(v)); txt.clear(); }
        });

        del.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) bonus.remove(idx);
        });

        edit.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                String v = txt.getText().trim();
                if (!v.isEmpty()) {
                    bonus.set(idx, new Bonus(v));
                    txt.clear();
                }
            }
        });

        Label lbl = new Label("Bonus :");
        Theme.styleCapsuleLabel(lbl, "#9be15d", "#00e3ae");

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, list, txt, new HBox(10, add, edit, del));
    }

    public Node getRootPane() { return root; }
}
