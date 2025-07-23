package org.example.wheel;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.example.Main;
import org.example.OptionRoue;
import org.example.Resultat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BaseWheel<T> {

    private static final double GOLDEN_ANGLE = 137.50776405003785;
    protected static final String BASE_FILL_KEY = "baseFill";

    private final StackPane root;
    private final Group wheelGroup;
    private final Resultat resultat;
    private final ImageView wheelImg = new ImageView();
    private final List<Arc> arcs = new ArrayList<>();

    private String[] seatNames = new String[0];
    private int listHash;

    private final SVGPath spear;
    private ParallelTransition winFx;
    private RotateTransition spinRT;

    private double dragX;
    private double dragY;

    protected BaseWheel(Resultat res) {
        resultat = res;

        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(Main.WHEEL_RADIUS * 2, Main.WHEEL_RADIUS * 2);

        wheelGroup = new Group();
        wheelGroup.setCache(true);
        wheelGroup.setCacheHint(CacheHint.ROTATE);
        root.getChildren().add(wheelGroup);

        wheelImg.setSmooth(true);
        wheelImg.setCache(true);
        wheelImg.setCacheHint(CacheHint.ROTATE);
        wheelImg.setVisible(false);
        root.getChildren().add(wheelImg);

        spear = new SVGPath();
        spear.setContent("M0,-" + (Main.WHEEL_RADIUS + 18) +
                " L-8,-" + (Main.WHEEL_RADIUS - 4) +
                " L0,-" + (Main.WHEEL_RADIUS - 14) +
                " L8,-" + (Main.WHEEL_RADIUS - 4) + " Z");
        spear.setFill(getHighlightColor());
        spear.setStroke(Color.BLACK);
        spear.setStrokeWidth(1.2);
        root.getChildren().add(spear);

        root.setOnMousePressed(e -> {
            dragX = e.getSceneX() - root.getTranslateX();
            dragY = e.getSceneY() - root.getTranslateY();
            root.setCursor(Cursor.CLOSED_HAND);
        });
        root.setOnMouseDragged(e -> {
            root.setTranslateX(e.getSceneX() - dragX);
            root.setTranslateY(e.getSceneY() - dragY);
        });
        root.setOnMouseReleased(e -> root.setCursor(Cursor.OPEN_HAND));
        root.setCursor(Cursor.OPEN_HAND);
    }

    public Node getRootPane() { return root; }

    public void resetPosition() {
        root.setTranslateX(0);
        root.setTranslateY(0);
    }

    public void updateWheelDisplay(ObservableList<T> items) {
        if (items.isEmpty()) {
            wheelGroup.getChildren().clear();
            arcs.clear();
            seatNames = new String[0];
            return;
        }
        int newHash = items.hashCode();
        if (newHash == listHash && seatNames.length != 0) return;
        listHash = newHash;

        int n = items.size();
        seatNames = new String[n];
        for (int i = 0; i < n; i++) seatNames[i] = itemToString(items.get(i));

        wheelGroup.setRotate(0);
        wheelGroup.getChildren().clear();
        arcs.clear();

        addDecorRings();

        double step = 360.0 / n;
        double start = 0;
        for (int i = 0; i < n; i++) {
            Arc a = buildSector(start, step, colorByIndex(i));
            arcs.add(a);
            wheelGroup.getChildren().add(a);
            start += step;
        }

        wheelGroup.applyCss();
        wheelGroup.layout();
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage img = wheelGroup.snapshot(sp, null);
        wheelImg.setImage(img);
        wheelImg.setFitWidth(img.getWidth());
        wheelImg.setFitHeight(img.getHeight());
    }

    public void spinTheWheel(ObservableList<T> items) {
        updateWheelDisplay(items);
        spinTheWheel();
    }

    public void spinTheWheel() {
        if (winFx != null) {
            winFx.stop();
            clearHighlight();
        }
        if (seatNames.length == 0) {
            resultat.setMessage("Aucun " + prefix().toLowerCase() + " – impossible de lancer la roue.");
            return;
        }

        wheelGroup.setVisible(false);
        wheelImg.setRotate(0);
        wheelImg.setVisible(true);
        wheelImg.setEffect(null);

        int idx = ThreadLocalRandom.current().nextInt(seatNames.length);
        double sector = 360.0 / seatNames.length;
        double target = idx * sector + sector / 2 - 90;
        double finalAngle = 6 * 360 + target;

        if (spinRT == null) {
            spinRT = new RotateTransition(Duration.seconds(OptionRoue.getSpinDuration()), wheelImg);
            spinRT.setInterpolator(Interpolator.EASE_OUT);
            spinRT.setCycleCount(1);
        }
        spinRT.stop();
        spinRT.setFromAngle(0);
        spinRT.setToAngle(finalAngle);
        spinRT.setOnFinished(e -> {
            wheelGroup.setRotate(wheelImg.getRotate());
            wheelImg.setVisible(false);
            wheelGroup.setVisible(true);
            String label = seatNames[idx];
            resultat.setMessage(prefix() + label);
            onItemWon(label);
            highlightWinner(idx);
        });
        spinRT.playFromStart();
    }

    private void highlightWinner(int idx) {
        if (idx < 0 || idx >= arcs.size()) return;
        Arc a = arcs.get(idx);
        Paint base = (Paint) a.getProperties().get(BASE_FILL_KEY);
        a.setEffect(new Glow(1.0));
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(a.fillProperty(), getHighlightColor())),
                new KeyFrame(Duration.seconds(.6), new KeyValue(a.fillProperty(), base))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        winFx = new ParallelTransition(pulse);
        winFx.play();
    }

    private void clearHighlight() {
        for (Arc a : arcs) {
            a.setEffect(null);
            a.setStroke(getMetalLight());
            a.setStrokeWidth(1.2);
            Paint base = (Paint) a.getProperties().get(BASE_FILL_KEY);
            if (base != null) a.setFill(base);
        }
    }

    private void addDecorRings() {
        Circle outer = new Circle(Main.WHEEL_RADIUS + 6, getMetalDark());
        outer.setStroke(getMetalLight());
        outer.setStrokeWidth(4);

        Group rivets = new Group();
        int count = 32;
        double r = Main.WHEEL_RADIUS + 6;
        for (int i = 0; i < count; i++) {
            double a = 2 * Math.PI * i / count;
            rivets.getChildren().add(new Circle(r * Math.cos(a), r * Math.sin(a), 3, getMetalLight()));
        }
        wheelGroup.getChildren().addAll(outer, rivets);
    }

    private Arc buildSector(double start, double extent, Color tint) {
        Arc arc = new Arc(0, 0, Main.WHEEL_RADIUS, Main.WHEEL_RADIUS, start, extent);
        arc.setType(ArcType.ROUND);
        Paint fill = new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, getFireStart().interpolate(tint, .25)),
                new Stop(.45, tint),
                new Stop(1, getFireEnd()));
        arc.setFill(fill);
        arc.getProperties().put(BASE_FILL_KEY, fill);
        arc.setStroke(getMetalLight());
        arc.setStrokeWidth(1.2);
        return arc;
    }

    private static Color colorByIndex(int idx) {
        double h = (idx * GOLDEN_ANGLE) % 360;
        return Color.hsb(h, .65, .9);
    }

    protected abstract Color getFireStart();
    protected abstract Color getFireEnd();
    protected abstract Color getHighlightColor();
    protected Color getMetalLight() { return Color.web("#cfcfcf"); }
    protected Color getMetalDark()  { return Color.web("#777777"); }
    protected abstract String prefix();
    protected abstract String itemToString(T item);
    protected abstract void onItemWon(String label);
}
