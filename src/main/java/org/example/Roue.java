package org.example;

import javafx.animation.*;
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
import javafx.scene.paint.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class Roue {

    private static final double GOLDEN_ANGLE = 137.50776405003785;
    private static final Color METAL_LIGHT = Color.web("#cfcfcf");
    private static final Color METAL_DARK = Color.web("#777777");
    private static final Color FIRE_START = Color.web("#ff5722");
    private static final Color FIRE_END = Color.web("#8b0000");
    private static final Color HIGHLIGHT = Color.web("#ff2200");
    private static final String BASE_FILL_KEY = "baseFill";

    private final StackPane root = new StackPane();
    private final Group wheelGroup = new Group();
    private final ImageView wheelImg = new ImageView();
    private final List<Arc> arcs = new ArrayList<>();
    private final Resultat resultat;

    private String[] seatNames = new String[0];
    private int malusHash;

    private RotateTransition spinRT;
    private ParallelTransition winFx;
    private Consumer<String> spinCallback;

    private double dragX, dragY;

    public Roue(Resultat res) {
        resultat = res;

        root.setAlignment(Pos.CENTER);
        root.setPrefSize(Main.WHEEL_RADIUS * 2, Main.WHEEL_RADIUS * 2);

        wheelGroup.setCache(true);
        wheelGroup.setCacheHint(CacheHint.ROTATE);
        root.getChildren().add(wheelGroup);

        wheelImg.setSmooth(true);
        wheelImg.setCache(true);
        wheelImg.setCacheHint(CacheHint.ROTATE);
        wheelImg.setVisible(false);
        root.getChildren().add(wheelImg);

        SVGPath spear = new SVGPath();
        spear.setContent("M0,-" + (Main.WHEEL_RADIUS + 18) + " L-8,-" + (Main.WHEEL_RADIUS - 4) +
                " L0,-" + (Main.WHEEL_RADIUS - 14) + " L8,-" + (Main.WHEEL_RADIUS - 4) + " Z");
        spear.setFill(HIGHLIGHT);
        spear.setStroke(Color.BLACK);
        spear.setStrokeWidth(1.2);
        root.getChildren().add(spear);

        enableDrag();
    }

    public Node getRootPane() { return root; }
    public void resetPosition() { root.setTranslateX(0); root.setTranslateY(0); }
    public void setOnSpinFinished(Consumer<String> cb) { spinCallback = cb; }

    public void updateWheelDisplay(ObservableList<String> malus) {
        if (malus.isEmpty()) {
            wheelGroup.getChildren().clear();
            arcs.clear();
            seatNames = new String[0];
            return;
        }
        int h = malus.hashCode();
        if (h == malusHash && seatNames.length > 0) return;
        malusHash = h;

        seatNames = malus.toArray(new String[0]);
        wheelGroup.getChildren().clear();
        arcs.clear();
        wheelGroup.setRotate(0);

        addDecorRings();

        double step = 360d / seatNames.length;
        double start = 0;
        for (int i = 0; i < seatNames.length; i++) {
            Arc a = buildSector(start, step, colorByIndex(i));
            arcs.add(a);
            wheelGroup.getChildren().add(a);
            start += step;
        }
        refreshSnapshot();
    }

    public void spinTheWheel(ObservableList<String> malus) {
        updateWheelDisplay(malus);
        spinTheWheel();
    }

    public void spinTheWheel() {
        if (winFx != null) { winFx.stop(); clearHighlight(); }
        if (seatNames.length == 0) {
            resultat.setMessage("Aucun malus – impossible de lancer la roue.");
            return;
        }

        wheelGroup.setVisible(false);
        wheelImg.setRotate(0);
        wheelImg.setVisible(true);

        int idx = ThreadLocalRandom.current().nextInt(seatNames.length);
        double sector = 360d / seatNames.length;
        double target = idx * sector + sector / 2 - 90;
        double finalAngle = 6 * 360 + target;

        if (spinRT == null) {
            spinRT = new RotateTransition(Duration.seconds(OptionRoue.getSpinDuration()), wheelImg);
            spinRT.setInterpolator(Interpolator.SPLINE(0.2, 0, 0.2, 1));
        }
        spinRT.stop();
        spinRT.setFromAngle(0);
        spinRT.setToAngle(finalAngle);
        spinRT.setOnFinished(e -> {
            wheelGroup.setRotate(wheelImg.getRotate());
            wheelImg.setVisible(false);
            wheelGroup.setVisible(true);
            String m = seatNames[idx];
            resultat.setMessage("Malus : " + m);
            if (spinCallback != null) spinCallback.accept(m);
            highlightWinner(idx);
        });
        spinRT.playFromStart();
    }

    private void highlightWinner(int idx) {
        Arc a = arcs.get(idx);
        Paint base = (Paint) a.getProperties().get(BASE_FILL_KEY);
        a.setEffect(new Glow(1));

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(a.fillProperty(), HIGHLIGHT)),
                new KeyFrame(Duration.seconds(.6), new KeyValue(a.fillProperty(), base)));
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);

        winFx = new ParallelTransition(pulse);
        winFx.play();
    }

    private void clearHighlight() {
        for (Arc a : arcs) {
            a.setEffect(null);
            a.setStroke(METAL_LIGHT);
            a.setStrokeWidth(1.2);
            Paint base = (Paint) a.getProperties().get(BASE_FILL_KEY);
            if (base != null) a.setFill(base);
        }
    }

    private void refreshSnapshot() {
        wheelGroup.applyCss();
        wheelGroup.layout();
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        sp.setTransform(new Scale(.75, .75));
        WritableImage img = wheelGroup.snapshot(sp, null);
        wheelImg.setImage(img);
        wheelImg.setFitWidth(img.getWidth());
        wheelImg.setFitHeight(img.getHeight());
    }

    private void addDecorRings() {
        Circle outer = new Circle(Main.WHEEL_RADIUS + 6, METAL_DARK);
        outer.setStroke(METAL_LIGHT);
        outer.setStrokeWidth(4);

        Group rivets = new Group();
        int n = 32;
        double r = Main.WHEEL_RADIUS + 6;
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            rivets.getChildren().add(new Circle(r * Math.cos(a), r * Math.sin(a), 3, METAL_LIGHT));
        }
        wheelGroup.getChildren().addAll(outer, rivets);
    }

    private Arc buildSector(double start, double extent, Color tint) {
        Arc a = new Arc(0, 0, Main.WHEEL_RADIUS, Main.WHEEL_RADIUS, start, extent);
        a.setType(ArcType.ROUND);
        Paint fill = new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, FIRE_START.interpolate(tint, .25)),
                new Stop(.45, tint),
                new Stop(1, FIRE_END));
        a.setFill(fill);
        a.getProperties().put(BASE_FILL_KEY, fill);
        a.setStroke(METAL_LIGHT);
        a.setStrokeWidth(1.2);
        return a;
    }

    private static Color colorByIndex(int idx) {
        double h = (idx * GOLDEN_ANGLE) % 360;
        return Color.hsb(h, .65, .9);
    }

    private void enableDrag() {
        root.setOnMousePressed(e -> { dragX = e.getSceneX() - root.getTranslateX(); dragY = e.getSceneY() - root.getTranslateY(); root.setCursor(Cursor.CLOSED_HAND); });
        root.setOnMouseDragged(e -> { root.setTranslateX(e.getSceneX() - dragX); root.setTranslateY(e.getSceneY() - dragY); });
        root.setOnMouseReleased(e -> root.setCursor(Cursor.OPEN_HAND));
        root.setCursor(Cursor.OPEN_HAND);
    }
}
