package org.example;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class Resultat {

    private static final Stop[] GRADIENT = {
            new Stop(0, Color.web("#ff4d4d")),
            new Stop(1, Color.web("#ffae42"))
    };

    private final StackPane root = new StackPane();
    private final Text icon = new Text("🎲");
    private final Text label = new Text("Résultat : ?");
    private final Timeline gradientLoop;
    private String lastMessage = "?";

    public Resultat() {
        icon.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 26));
        icon.setFill(Color.WHITE);
        label.setFont(Font.font("Montserrat", FontWeight.BOLD, 24));
        label.setFill(Color.WHITE);

        HBox content = new HBox(12, icon, label);
        content.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(content);
        root.setPadding(new Insets(14, 34, 14, 34));
        root.setMaxWidth(Region.USE_PREF_SIZE);
        root.setEffect(new DropShadow(16, Color.rgb(120, 0, 0, .6)));

        DoubleProperty offset = new SimpleDoubleProperty();
        offset.addListener((o, ov, nv) -> root.setBackground(new Background(
                new BackgroundFill(makeGradient(nv.doubleValue()), new CornerRadii(22), Insets.EMPTY))));

        root.setBackground(new Background(
                new BackgroundFill(makeGradient(0), new CornerRadii(22), Insets.EMPTY)));

        gradientLoop = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(offset, 0)),
                new KeyFrame(Duration.seconds(6), new KeyValue(offset, 1)));
        gradientLoop.setCycleCount(Animation.INDEFINITE);
        gradientLoop.setAutoReverse(true);
        gradientLoop.play();
    }

    public Pane getNode()               { return root; }
    public String getLastMessage()      { return lastMessage; }

    public void setMessage(String msg) {
        lastMessage = msg;
        label.setText("Résultat : " + msg);
        icon.setText("☠");
        root.setScaleX(.88);
        root.setScaleY(.88);
        ScaleTransition pop = new ScaleTransition(Duration.millis(260), root);
        pop.setToX(1);
        pop.setToY(1);
        pop.play();
    }

    private static LinearGradient makeGradient(double offset) {
        return new LinearGradient(offset, 0, 1 + offset, 0, true, CycleMethod.NO_CYCLE, GRADIENT);
    }
}
