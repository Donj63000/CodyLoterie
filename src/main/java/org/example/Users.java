package org.example;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.IntegerStringConverter;

/**
 * Table des participants : chaque ligne reçoit une couleur unique,
 * indépendamment du pseudo (→ palettes vraiment distinctes).
 */
public class Users {

    private final ObservableList<Participant> participants = FXCollections.observableArrayList();
    private final TableView<Participant>      table        = new TableView<>(participants);
    private final VBox                        root         = new VBox(10);

    private static final double GOLDEN_ANGLE = 137.50776405003785;

    public Users(){

        /* === Colonnes ================================================= */
        TableColumn<Participant,String>  colNom   = new TableColumn<>("Nom");
        TableColumn<Participant,Integer> colLevel = new TableColumn<>("Level");
        TableColumn<Participant,String>  colClasse= new TableColumn<>("Classe");

        colNom   .setCellValueFactory(p -> new SimpleStringProperty (p.getValue().getName()));
        colLevel .setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getLevel()).asObject());
        colClasse.setCellValueFactory(p -> new SimpleStringProperty (p.getValue().getClasse()));

        /* === Cellule colorée par INDEX de ligne ======================= */
        colNom.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty || item==null){ setText(null); setStyle(""); return; }

                setText(item); setFont(Font.font("Arial", FontWeight.BOLD, 15));

                int idx = getIndex();
                double hue = (idx * GOLDEN_ANGLE) % 360;
                javafx.scene.paint.Color c = javafx.scene.paint.Color.hsb(hue,.85,.9).brighter();
                setStyle("-fx-text-fill:" + Theme.toWebColor(c) + ";");
            }
        });
        colLevel .setCellFactory(c -> new TextFieldTableCell<>(new IntegerStringConverter()));
        colClasse.setCellFactory(TextFieldTableCell.forTableColumn());

        colNom.setOnEditCommit(e -> {
            Participant p = e.getRowValue();
            p.setName(e.getNewValue());
        });
        colLevel.setOnEditCommit(e -> {
            Participant p = e.getRowValue();
            p.setLevel(e.getNewValue());
        });
        colClasse.setOnEditCommit(e -> {
            Participant p = e.getRowValue();
            p.setClasse(e.getNewValue());
        });

        table.getColumns().addAll(colNom, colLevel, colClasse);
        table.setEditable(true);
        table.setPrefHeight(600);
        Theme.styleTableView(table);

        /* === Formulaire =============================================== */
        TextField tNom   = new TextField(); tNom.setPromptText("Pseudo");  Theme.styleTextField(tNom);
        TextField tLevel  = new TextField(); tLevel.setPromptText("Level");  Theme.styleTextField(tLevel);
        TextField tClasse = new TextField(); tClasse.setPromptText("Classe"); Theme.styleTextField(tClasse);

        Button add = new Button("Ajouter");   Theme.styleButton(add);
        Button del = new Button("Supprimer"); Theme.styleButton(del);

        add.setOnAction(e -> {
            String n = tNom.getText().trim();
            if(!n.isEmpty()){
                int lv = tLevel.getText().isBlank()?0:Integer.parseInt(tLevel.getText());
                participants.add(new Participant(n,lv,tClasse.getText().trim()));
                tNom.clear(); tLevel.clear(); tClasse.clear();
            }
        });
        del.setOnAction(e -> {
            Participant sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null) participants.remove(sel);
        });

        /* === Layout ==================================================== */
        Label lbl = new Label("Participants :");
        Theme.styleCapsuleLabel(lbl, "#4facfe", "#00f2fe");

        root.getChildren().addAll(lbl, table, tNom, tLevel, tClasse, add, del);

        /* === Sync roue ↔ table ======================================== */
        participants.addListener((ListChangeListener<Participant>) change -> {
            // Reconstruit la roue dès que la liste change via Main
        });
    }

    /* === API ========================================================== */
    public ObservableList<Participant> getParticipants(){ return participants; }
    public ObservableList<String> getParticipantNames(){
        return FXCollections.observableArrayList(participants.stream().map(Participant::getName).toList());
    }
    public Node getRootPane(){ return root; }
}
