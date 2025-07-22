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

    public static final Color DARK_BG = Color.web("#121212");
    public static final Color DARK_ELEVATION = Color.web("#1e1e1e");
    public static final Color ACCENT = Color.web("#2196F3");
    public static final Color ACCENT_LIGHT = Color.web("#42A5F5");
    public static final Color TEXT_DEFAULT = Color.WHITE;
    public static final Font MAIN_FONT = Font.font("Arial", 14);

    private static final String BASE_BTN =
            "-fx-background-radius:8;-fx-text-fill:white;-fx-font-weight:bold;";
    private static final String BTN_GRAD = "-fx-background-color:linear-gradient(#2196F3 0%, #1e88e5 100%);";
    private static final String BTN_HOVER = "-fx-background-color:linear-gradient(#42a5f5 0%, #2196F3 100%);";
    private static final String BTN_PRESSED = "-fx-background-color:#1565c0;";

    private static final String BASE_CTRL =
            "-fx-control-inner-background:#1e1e1e;" +
                    "-fx-background-insets:0;" +
                    "-fx-selection-bar:#2196F3;" +
                    "-fx-selection-bar-non-focused:#1565c0;";

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
    }

    public static void styleListView(ListView<?> lv) {
        lv.setStyle(BASE_CTRL);
    }

    public static void styleTableView(TableView<?> tv) {
        styleControl(tv);
        tv.setStyle(tv.getStyle() +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-table-header-border-color:transparent;" +
                "-fx-border-color:transparent;");
    }

    public static void styleControl(Control c) {
        c.setStyle(BASE_CTRL);
    }

    public static String toWebColor(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    private static void stylizeTextInput(Region n) {
        String base =
                "-fx-background-radius:6;" +
                        "-fx-background-color:#1e1e1e;" +
                        "-fx-text-fill:white;" +
                        "-fx-prompt-text-fill:#bbbbbb;" +
                        "-fx-border-color:#2196F3;" +
                        "-fx-border-radius:6;" +
                        "-fx-border-width:1;";
        String focused = "-fx-border-color:#42A5F5;";
        n.setStyle(base);
        n.focusedProperty().addListener((o, ov, nv) -> n.setStyle(nv ? base + focused : base));
    }

    public static void styleTextField(TextField tf) {
        stylizeTextInput(tf);
    }

    public static void styleTextArea(TextArea ta) {
        stylizeTextInput(ta);
    }

    public static void styleCapsuleLabel(Label label, String start, String end) {
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(8, 16, 8, 16));
        label.setAlignment(Pos.CENTER);
        LinearGradient grad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(start)), new Stop(1, Color.web(end)));
        label.setBackground(new Background(new BackgroundFill(grad, new CornerRadii(15), Insets.EMPTY)));
        label.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.35)));
    }

    public static void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
