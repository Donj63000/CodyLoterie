package org.example;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class Gains {

    private static final NumberFormat NF = NumberFormat.getIntegerInstance(Locale.FRANCE);

    private final ObservableList<Participant> participants;
    private final ObservableList<String>      objets = FXCollections.observableArrayList();
    private final IntegerProperty             extraKamas = new SimpleIntegerProperty();

    private final TextField txtExtra = new TextField("0");
    private final Label     lblTotal = new Label();
    private final ListView<String> listView = new ListView<>(objets);

    private final VBox root;

    public Gains(ObservableList<Participant> participants) {
        this.participants = participants;

        Theme.styleTextField(txtExtra);
        txtExtra.setPrefWidth(70);
        txtExtra.setOnAction(e -> commitExtra());
        txtExtra.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitExtra(); });

        Theme.styleCapsuleLabel(lblTotal, "#4facfe", "#00f2fe");
        lblTotal.textProperty().bind(Bindings.createStringBinding(
                () -> "Cagnotte : " +
                        NF.format(participants.stream().mapToInt(Participant::getLevel).sum() + extraKamas.get()) +
                        " 𝚔",
                participants, extraKamas));

        participants.addListener((ListChangeListener<Participant>) c -> {
            while (c.next())
                if (c.wasAdded())
                    c.getAddedSubList().forEach(p ->
                            p.levelProperty().addListener((o, ov, nv) -> lblTotal.requestLayout()));
            refreshObjets();
        });

        Button btnAddKamas = makeButton("Ajouter", () -> commitExtra());
        Button btnDelKamas = makeButton("Supprimer", () -> { extraKamas.set(0); txtExtra.setText("0"); });

        Theme.styleListView(listView);
        listView.setPrefSize(160, 300);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-font-size:18px;-fx-text-fill:green;");
            }
        });

        TextField txtNew = new TextField();
        Theme.styleTextField(txtNew);
        txtNew.setPromptText("Nouvel objet…");

        Button btnAddObj = makeButton("Ajouter", () -> {
            String v = txtNew.getText().trim();
            if (!v.isEmpty()) { objets.add(v); txtNew.clear(); }
        });
        Button btnDelObj = makeButton("Supprimer", () -> {
            String sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null) objets.remove(sel);
        });
        txtNew.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) btnAddObj.fire(); });

        Label lblObjets = new Label("Objets :");
        Theme.styleCapsuleLabel(lblObjets, "#ff9a9e", "#fad0c4");

        VBox boxObjets = new VBox(6, lblObjets, listView, txtNew, btnAddObj, btnDelObj);
        boxObjets.setPadding(new Insets(8, 0, 0, 0));

        VBox boxKamas = new VBox(6, txtExtra, new HBox(10, btnAddKamas, btnDelKamas));

        root = new VBox(10, lblTotal, boxKamas, new Separator(), boxObjets);
        root.setPadding(new Insets(8));
        refreshObjets();
    }

    private Button makeButton(String text, Runnable action) {
        Button b = new Button(text);
        Theme.styleButton(b);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void commitExtra() {
        try {
            extraKamas.set(Math.max(0, Integer.parseInt(txtExtra.getText().trim())));
        } catch (NumberFormatException ignored) {
            txtExtra.setText(String.valueOf(extraKamas.get()));
        }
    }

    private void refreshObjets() {
        objets.setAll(participants.stream()
                .map(Participant::getClasse)
                .filter(s -> s != null && !s.isBlank() && !"-".equals(s))
                .collect(Collectors.toSet()));
    }

    public Node getRootPane()             { return root; }
    public int  getExtraKamas()            { return extraKamas.get(); }
    public void setExtraKamas(int value)   { extraKamas.set(value); txtExtra.setText(String.valueOf(value)); }
    public int  getTotalKamas()            { return participants.stream().mapToInt(Participant::getLevel).sum() + extraKamas.get(); }
    public ObservableList<String> getObjets() { return objets; }
}
