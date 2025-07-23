package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class Theme {

    public static final Color DARK_BG = Color.web("#101010");
    public static final Color DARK_ELEVATION = Color.web("#1b1b1b");
    public static final Color ACCENT = Color.web("#e53935");
    public static final Color ACCENT_LIGHT = Color.web("#ff7043");
    public static final Color TEXT_DEFAULT = Color.WHITE;
    public static final Font MAIN_FONT = Font.font("Arial", 14);

    private static final String BASE_BTN = "-fx-background-radius:10;-fx-text-fill:white;-fx-font-weight:bold;";
    private static final String BTN_GRAD = "-fx-background-color:linear-gradient(#d32f2f 0%, #8e0000 100%);";
    private static final String BTN_HOVER = "-fx-background-color:linear-gradient(#ff6f00 0%, #d84315 100%);";
    private static final String BTN_PRESSED = "-fx-background-color:#4e0000;";

    private static final String BASE_CTRL =
            "-fx-control-inner-background:#1b1b1b;" +
                    "-fx-background-insets:0;" +
                    "-fx-selection-bar:#d84315;" +
                    "-fx-selection-bar-non-focused:#8e0000;";

    private Theme() {}

    public static Background makeBackgroundCover(String path) {
        Image img = new Image(Theme.class.getResourceAsStream(path));
        BackgroundSize size = new BackgroundSize(1, 1, true, true, false, true);
        return new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, size));
    }

    public static void styleButton(Button b) {
        String normal = BASE_BTN + BTN_GRAD;
        b.setStyle(normal);
        b.setCursor(Cursor.HAND);
        b.setOnMouseEntered(e -> b.setStyle(normal + BTN_HOVER));
        b.setOnMouseExited(e -> b.setStyle(normal));
        b.pressedProperty().addListener((o, ov, nv) -> b.setStyle(nv ? normal + BTN_PRESSED : normal));
        b.setEffect(new DropShadow(8, Color.web("#00000080")));
    }

    public static void styleListView(ListView<?> lv) { lv.setStyle(BASE_CTRL); }

    public static void styleTableView(TableView<?> tv) {
        styleControl(tv);
        tv.setStyle(tv.getStyle() +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-table-header-border-color:transparent;" +
                "-fx-border-color:transparent;");
    }

    public static void styleControl(Control c) { c.setStyle(BASE_CTRL); }

    public static String toWebColor(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    private static void stylizeTextInput(Region n) {
        String base =
                "-fx-background-radius:6;" +
                        "-fx-background-color:#1b1b1b;" +
                        "-fx-text-fill:white;" +
                        "-fx-prompt-text-fill:#bbbbbb;" +
                        "-fx-border-color:#d84315;" +
                        "-fx-border-radius:6;" +
                        "-fx-border-width:1;";
        String focused = "-fx-border-color:#ff7043;";
        n.setStyle(base);
        n.focusedProperty().addListener((o, ov, nv) -> n.setStyle(nv ? base + focused : base));
    }

    public static void styleTextField(TextField tf) { stylizeTextInput(tf); }
    public static void styleTextArea(TextArea ta) { stylizeTextInput(ta); }

    public static void styleCapsuleLabel(Label label, String start, String end) {
        label.setFont(Font.font("Roboto", FontWeight.EXTRA_BOLD, 18));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(8, 16, 8, 16));
        label.setAlignment(Pos.CENTER);
        LinearGradient grad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(start)), new Stop(1, Color.web(end)));
        label.setBackground(new Background(new BackgroundFill(grad, new CornerRadii(18), Insets.EMPTY)));
        label.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.45)));
    }

    public static void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
