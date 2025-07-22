package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

public final class Users {

    private static final double GOLDEN_ANGLE = 137.50776405003785;
    private static final int TABLE_HEIGHT = 600;

    private final ObservableList<Participant> participants = FXCollections.observableArrayList();
    private final TableView<Participant> table = new TableView<>(participants);
    private final VBox root = new VBox(12);

    public Users() {
        TableColumn<Participant, String> colName = new TableColumn<>("Nom");
        TableColumn<Participant, Integer> colLevel = new TableColumn<>("Level");
        TableColumn<Participant, String> colClass = new TableColumn<>("Classe");

        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colLevel.setCellValueFactory(c -> c.getValue().levelProperty().asObject());
        colClass.setCellValueFactory(c -> c.getValue().classeProperty());

        colName.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(val);
                    setFont(Font.font("Arial", FontWeight.BOLD, 15));
                    double hue = (getIndex() * GOLDEN_ANGLE) % 360;
                    setStyle("-fx-text-fill:" + Theme.toWebColor(javafx.scene.paint.Color.hsb(hue, .85, .9).brighter()));
                }
            }
        });

        colLevel.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colClass.setCellFactory(TextFieldTableCell.forTableColumn());

        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colLevel.setOnEditCommit(e -> e.getRowValue().setLevel(e.getNewValue()));
        colClass.setOnEditCommit(e -> e.getRowValue().setClasse(e.getNewValue()));

        table.getColumns().addAll(colName, colLevel, colClass);
        table.setEditable(true);
        table.setPrefHeight(TABLE_HEIGHT);
        Theme.styleTableView(table);

        TextField tfName = buildField("Pseudo");
        TextField tfLevel = buildField("Level");
        TextField tfClass = buildField("Classe");

        Button btnAdd = buildButton("Ajouter");
        Button btnDel = buildButton("Supprimer");

        btnAdd.setOnAction(e -> {
            String name = tfName.getText().trim();
            String levelText = tfLevel.getText().trim();
            if (name.isEmpty() || levelText.isEmpty()) return;

            try {
                int level = Integer.parseInt(levelText);
                participants.add(new Participant(name, level, tfClass.getText().trim()));
                tfName.clear();
                tfLevel.clear();
                tfClass.clear();
            } catch (NumberFormatException ex) {
                Theme.showError("Le level doit être un nombre entier.");
            }
        });

        btnDel.setOnAction(e -> {
            Participant selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) participants.remove(selected);
        });

        Label title = new Label("Participants :");
        Theme.styleCapsuleLabel(title, "#4facfe", "#00f2fe");

        root.setPadding(new Insets(0, 0, 0, 0));
        root.getChildren().addAll(title, table, tfName, tfLevel, tfClass, btnAdd, btnDel);
    }

    private static TextField buildField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        Theme.styleTextField(tf);
        return tf;
    }

    private static Button buildButton(String text) {
        Button btn = new Button(text);
        Theme.styleButton(btn);
        return btn;
    }

    public ObservableList<Participant> getParticipants() {
        return participants;
    }

    public ObservableList<String> getParticipantNames() {
        return participants.stream()
                .map(Participant::getName)
                .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
    }

    public Node getRootPane() {
        return root;
    }
}
