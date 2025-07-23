package org.example;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.Random;
import java.util.stream.Stream;

public final class Titre {

    private static final Duration COLOR_CYCLE   = Duration.seconds(20);
    private static final Duration SHIMMER_CYCLE = Duration.seconds(12);
    private static final Duration HALO_PULSE    = Duration.seconds(4);
    private static final Color[]  WARM = {
            Color.web("#e57373"),
            Color.web("#ff8a65"),
            Color.web("#ffb74d"),
            Color.web("#ffd180")
    };

    private final StackPane root;
    private final Text title;
    private final Rectangle shimmer;
    private final Random rnd = new Random();

    private Color a1, a2, a3, b1, b2, b3;

    public Titre() {
        title = new Text("L'EVENT PVP  ·  GUILDE EVOLUTION");
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 36));
        title.setStroke(Color.web("#cfd8dc"));
        title.setStrokeWidth(1.6);
        title.setStrokeType(StrokeType.OUTSIDE);
        title.setCache(true);
        title.setCacheHint(CacheHint.SCALE_AND_ROTATE);

        DropShadow halo = new DropShadow(12, Color.web("#ffab91"));
        halo.setSpread(0.25);
        title.setEffect(halo);

        Timeline haloPulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(halo.radiusProperty(), 8)),
                new KeyFrame(HALO_PULSE,    new KeyValue(halo.radiusProperty(), 12))
        );
        haloPulse.setAutoReverse(true);
        haloPulse.setCycleCount(Animation.INDEFINITE);
        haloPulse.play();

        Text leftSword  = new Text("⚔");
        Text rightSword = new Text("⚔");
        Stream.of(leftSword, rightSword).forEach(t -> {
            t.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 28));
            t.setFill(Color.web("#fff9c4"));
            t.setEffect(new DropShadow(4, Color.web("#ffcc80")));
        });

        TranslateTransition swordSwing = new TranslateTransition(Duration.seconds(1.5), rightSword);
        swordSwing.setFromX(0);
        swordSwing.setToX(1.5);
        swordSwing.setCycleCount(Animation.INDEFINITE);
        swordSwing.setAutoReverse(true);
        swordSwing.play();

        HBox content = new HBox(6, leftSword, title, rightSword);
        content.setAlignment(Pos.CENTER_LEFT);

        root = new StackPane(content);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(4, 0, 2, 24));
        root.setMaxWidth(StackPane.USE_PREF_SIZE);

        shimmer = new Rectangle();
        shimmer.widthProperty().bind(Bindings.createDoubleBinding(
                () -> title.getLayoutBounds().getWidth() * 1.15, title.layoutBoundsProperty()));
        shimmer.heightProperty().bind(Bindings.createDoubleBinding(
                () -> title.getLayoutBounds().getHeight() * 1.6, title.layoutBoundsProperty()));
        shimmer.setRotate(22);
        shimmer.setBlendMode(BlendMode.SOFT_LIGHT);
        shimmer.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.48, Color.WHITE.deriveColor(0, 1, 1, 0.35)),
                new Stop(0.52, Color.TRANSPARENT)));
        shimmer.setMouseTransparent(true);
        root.getChildren().add(shimmer);

        TranslateTransition shimmerMove = new TranslateTransition(SHIMMER_CYCLE, shimmer);
        shimmerMove.setCycleCount(Animation.INDEFINITE);
        shimmerMove.fromXProperty().bind(Bindings.createDoubleBinding(
                () -> -title.getLayoutBounds().getWidth(), title.layoutBoundsProperty()));
        shimmerMove.toXProperty().bind(Bindings.createDoubleBinding(
                () ->  title.getLayoutBounds().getWidth(), title.layoutBoundsProperty()));
        shimmerMove.play();

        cycleColors();
    }

    public StackPane getNode() { return root; }

    private void cycleColors() {
        a1 = b1 == null ? randomWarm() : b1;
        a2 = b2 == null ? randomWarm() : b2;
        a3 = b3 == null ? randomWarm() : b3;
        b1 = randomWarm();
        b2 = randomWarm();
        b3 = randomWarm();

        DoubleProperty t = new SimpleDoubleProperty(0);
        t.addListener((o, ov, nv) -> {
            double f = nv.doubleValue();
            title.setFill(makeGradient(
                    interp(a1, b1, f),
                    interp(a2, b2, f),
                    interp(a3, b3, f)));
        });

        Timeline flow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(t, 0)),
                new KeyFrame(COLOR_CYCLE,    new KeyValue(t, 1))
        );
        flow.setOnFinished(e -> cycleColors());
        flow.play();
    }

    private Color randomWarm() { return WARM[rnd.nextInt(WARM.length)]; }

    private static Color interp(Color c1, Color c2, double f) {
        return new Color(
                c1.getRed()   + (c2.getRed()   - c1.getRed())   * f,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * f,
                c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * f,
                c1.getOpacity()+ (c2.getOpacity()-c1.getOpacity())* f);
    }

    private static Paint makeGradient(Color c1, Color c2, Color c3) {
        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, c1), new Stop(0.5, c2), new Stop(1, c3));
    }
}
