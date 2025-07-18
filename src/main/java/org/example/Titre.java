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
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.Random;
import java.util.stream.Stream;

public class Titre {

    private final StackPane root;
    private final Text title;
    private final Rectangle shimmer;

    // Couleurs (texte) : on fait une transition progressive de old → new
    private Color oldC1, oldC2, oldC3, newC1, newC2, newC3;


    private final Random rand = new Random();

    // Palette "warm combat"
    private static final Color[] WARM = {
            Color.web("#ff4e50"),
            Color.web("#ff6e40"),
            Color.web("#ff9e2c"),
            Color.web("#ffd452")
    };

    public Titre() {
        // ====== TEXTE ======
        title = new Text("L'EVENT PVP de la guilde EVOLUTION");
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 34));
        title.setBoundsType(TextBoundsType.VISUAL);
        title.setCache(true);
        title.setCacheHint(CacheHint.SCALE_AND_ROTATE);

        // Contour métallique
        title.setStroke(Color.web("#cfd8dc"));
        title.setStrokeWidth(2);
        title.setStrokeType(StrokeType.OUTSIDE);

        // Halo pulsant
        DropShadow fire = new DropShadow(14, Color.web("#ff6e40"));
        fire.setSpread(0.35);
        title.setEffect(fire);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(fire.radiusProperty(), 14)),
                new KeyFrame(Duration.seconds(1), new KeyValue(fire.radiusProperty(), 20))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        Text swordLeft  = new Text("⚔");
        Text swordRight = new Text("⚔");
        Stream.of(swordLeft, swordRight).forEach(t -> {
            t.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 28));
            t.setFill(Color.web("#ffe082"));
            t.setEffect(new DropShadow(5, Color.web("#ff6e40")));
        });
        HBox content = new HBox(8, swordLeft, title, swordRight);
        content.setAlignment(Pos.CENTER_LEFT);

        TranslateTransition vib = new TranslateTransition(Duration.seconds(.12), swordRight);
        vib.setFromX(0); vib.setToX(3);
        vib.setCycleCount(Animation.INDEFINITE); vib.setAutoReverse(true);
        vib.play();

        // Conteneur (réduit)
        root = new StackPane(content);
        root.setAlignment(Pos.TOP_LEFT);
        // Moins de padding => l'élément en dessous remonte
        root.setPadding(new Insets(2, 0, 1, 20));
        root.setMaxWidth(StackPane.USE_PREF_SIZE);

        // ====== SHIMMER (rectangle mobile) ======
        shimmer = new Rectangle();
        shimmer.widthProperty().bind(Bindings.createDoubleBinding(
                () -> title.getLayoutBounds().getWidth() * 1.2,
                title.layoutBoundsProperty()
        ));
        shimmer.heightProperty().bind(Bindings.createDoubleBinding(
                () -> title.getLayoutBounds().getHeight() * 2,
                title.layoutBoundsProperty()
        ));
        shimmer.setRotate(25);
        shimmer.setBlendMode(BlendMode.OVERLAY);
        shimmer.setFill(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(.4, Color.WHITE.deriveColor(0,1,1,0.75)),
                new Stop(.6, Color.TRANSPARENT)));
        root.getChildren().add(shimmer);

        var tt = new TranslateTransition(Duration.seconds(4), shimmer);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.fromXProperty().bind(Bindings.createDoubleBinding(
                () -> -title.getLayoutBounds().getWidth(),
                title.layoutBoundsProperty()));
        tt.toXProperty().bind(Bindings.createDoubleBinding(
                () -> title.getLayoutBounds().getWidth(),
                title.layoutBoundsProperty()));
        tt.play();

        // On lance l’animation continue (texte + shimmer)
        startColorCycle();
    }

    public StackPane getNode() {
        return root;
    }

    // ====================== Animation continue ======================
    private void startColorCycle() {
        // Au début de chaque cycle, old = new (ou random init)
        if (oldC1 == null) {
            oldC1 = randomWarm();
            oldC2 = randomWarm();
            oldC3 = randomWarm();
        } else {
            oldC1 = newC1;
            oldC2 = newC2;
            oldC3 = newC3;
        }
        // On pioche un nouveau set
        newC1 = randomWarm();
        newC2 = randomWarm();
        newC3 = randomWarm();

        DoubleProperty t = new SimpleDoubleProperty(0);
        t.addListener((o, ov, nv) -> {
            double frac = nv.doubleValue();
            // Interpolation linéaire pour chaque stop
            Color c1 = lerpColor(oldC1, newC1, frac);
            Color c2 = lerpColor(oldC2, newC2, frac);
            Color c3 = lerpColor(oldC3, newC3, frac);
            // On met à jour le dégradé du texte
            title.setFill(makeGradient(c1, c2, c3));
        });

        // On anime t de 0 → 1 sur 8 s
        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO,  new KeyValue(t, 0)),
                new KeyFrame(Duration.seconds(8), new KeyValue(t, 1))
        );
        // À la fin, on relance un nouveau cycle
        anim.setOnFinished(e -> startColorCycle());
        anim.play();
    }

    // ====================== Méthodes d’aide ======================
    private Color randomColor() {
        return Color.hsb(rand.nextDouble()*360, 0.9, 1.0);
    }
    private Color randomWarm() {
        return WARM[rand.nextInt(WARM.length)];
    }
    private Color lerpColor(Color a, Color b, double f) {
        return new Color(
                a.getRed()   + (b.getRed()   - a.getRed())   * f,
                a.getGreen() + (b.getGreen() - a.getGreen()) * f,
                a.getBlue()  + (b.getBlue()  - a.getBlue())  * f,
                a.getOpacity()+ (b.getOpacity()-a.getOpacity())* f
        );
    }
    private Paint makeGradient(Color c1, Color c2, Color c3) {
        return new LinearGradient(0,0,1,0,true, CycleMethod.NO_CYCLE,
                new Stop(0,c1),
                new Stop(.5,c2),
                new Stop(1,c3));
    }
}
