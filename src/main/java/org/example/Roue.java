package org.example;

import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Roue de loterie – une couleur par malus ; la case gagnante clignote
 * en arc‑en‑ciel grâce à un Timeline cyclique, avec pulsation + halo.
 */
public class Roue {

    /* ============================================================ */
    /* 1)  Paramètres visuels                                       */
    /* ============================================================ */

    private static final Color METAL_LIGHT  = Color.web("#cfcfcf");
    private static final Color METAL_DARK   = Color.web("#777777");
    private static final Color FIRE_START   = Color.web("#ff5722");   // centre
    private static final Color FIRE_END     = Color.web("#8b0000");   // bord
    private static final Color HIGHLIGHT    = Color.web("#ff2200");   // glow gagnant

    private static final String BASE_FILL_KEY = "baseFill";

    private static final double GOLDEN_ANGLE    = 137.50776405003785;

    /* ============================================================ */
    /* 2)  Couleur unique par index                                 */
    /* ============================================================ */
    private static Color colorByIndex(int idx){
        double h = (idx * GOLDEN_ANGLE) % 360;
        return Color.hsb(h, .65, .9);
    }

    /* ============================================================ */
    /* 3)  Attributs                                                */
    /* ============================================================ */
    private final StackPane root;
    private final Group     wheelGroup;
    private final Resultat  resultat;
    private final List<Arc> arcs = new ArrayList<>();

    private String[] seatNames;
    private Color[]  seatColors;

    // Dernier hash de la liste de malus pour éviter les reconstructions inutiles
    private int malusHash = 0;

    private SVGPath  spear;
    private ParallelTransition winFx;
    private Consumer<String>   spinCallback;
    private List<String> lastMalus = List.of();

    // drag
    private double dragX, dragY;

    /* ============================================================ */
    /* 4)  Constructeur                                             */
    /* ============================================================ */
    public Roue(Resultat res){
        this.resultat = res;

        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(Main.WHEEL_RADIUS*2, Main.WHEEL_RADIUS*2);

        wheelGroup = new Group();
        wheelGroup.setCache(true);
        wheelGroup.setCacheHint(CacheHint.ROTATE);
        root.getChildren().add(wheelGroup);

        spear = new SVGPath();
        spear.setContent("M0,-" + (Main.WHEEL_RADIUS + 18) + " L-8,-" +
                (Main.WHEEL_RADIUS - 4) + " L0,-" + (Main.WHEEL_RADIUS - 14) +
                " L8,-" + (Main.WHEEL_RADIUS - 4) + " Z");
        spear.setFill(HIGHLIGHT);
        spear.setStroke(Color.BLACK);
        spear.setStrokeWidth(1.2);
        root.getChildren().add(spear);

        enableDrag();
    }

    /* ============================================================ */
    /* 5)  API                                                      */
    /* ============================================================ */
    public Node getRootPane(){ return root; }
    public void resetPosition(){ root.setTranslateX(0); root.setTranslateY(0); }
    public void setOnSpinFinished(Consumer<String> cb){ spinCallback = cb; }

    /* ============================================================ */
    /* 6)  Construction                                             */
    /* ============================================================ */
    public void updateWheelDisplay(ObservableList<String> malus){
        if (malus.isEmpty()) {
            wheelGroup.getChildren().clear();
            arcs.clear();
            seatNames = new String[0];
            return;
        }
        int newHash = malus.hashCode();
        if (newHash == malusHash) {
            return; // aucune modification de la liste
        }
        malusHash = newHash;
        buildSeatArrays(malus);

        wheelGroup.setRotate(0);
        wheelGroup.getChildren().clear();
        arcs.clear();

        addDecorRings();

        double step = 360d/ seatNames.length, start=0;
        for(int i=0;i<seatNames.length;i++){
            Arc a = buildSector(start, step, seatColors[i]);
            arcs.add(a);
            wheelGroup.getChildren().add(a);
            start += step;
        }
    }

    /* ============================================================ */
    /* 7)  Spin                                                     */
    /* ============================================================ */
    public void spinTheWheel(ObservableList<String> malus){
        if (!malus.equals(lastMalus)) {
            updateWheelDisplay(malus);
            lastMalus = List.copyOf(malus);
        }
        spin();
    }
    private void spin(){

        if (winFx != null) { winFx.stop(); clearHighlight(); }

        if (seatNames.length == 0) {
            resultat.setMessage("Aucun malus – impossible de lancer la roue.");
            return;
        }

        int idx = ThreadLocalRandom.current().nextInt(seatNames.length);
        double sector = stepAngle();
        double target = idx * sector + sector / 2 - 90;
        double totalTurns = 6;                           // à ajuster
        double finalAngle = totalTurns * 360 + target;
        double dur = OptionRoue.getSpinDuration();

        DoubleProperty angle = wheelGroup.rotateProperty();

        // 1) Accélération (0 → 720 ° en 10 % du temps, ease-in)
        KeyFrame kfStart = new KeyFrame(
                Duration.seconds(dur * .10),
                new KeyValue(angle, 720, Interpolator.SPLINE(0.42, 0.0, 1.0, 1.0))
        );

        // 2) Décélération (720 ° → finalAngle en 90 % du temps, ease-out)
        KeyFrame kfStop = new KeyFrame(
                Duration.seconds(dur),
                new KeyValue(angle, finalAngle, Interpolator.SPLINE(0.0, 0.0, 0.58, 1.0))
        );

        Timeline spin = new Timeline(
                new KeyFrame(Duration.ZERO,     new KeyValue(angle, 0)),
                kfStart,
                kfStop
        );

        spin.setOnFinished(e -> {
            String malus = seatNames[idx];
            resultat.setMessage("Malus : " + malus);
            if (spinCallback != null) spinCallback.accept(malus);
            highlightWinner(idx);
        });

        spin.play();
    }

    private double stepAngle() {
        return 360.0 / seatNames.length;
    }

    /* ============================================================ */
    /* 8)  Effet gagnant  : halo incandescent                   */
    /* ============================================================ */
    private void highlightWinner(int idx){
        if(idx<0||idx>=arcs.size()) return;
        Arc a = arcs.get(idx);

        Paint basePaint = (Paint) a.getProperties().get(BASE_FILL_KEY);

        // Halo incandescent
        a.setEffect(new Glow(1.0));

        // Pulsation rouge vif
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,      new KeyValue(a.fillProperty(), HIGHLIGHT)),
                new KeyFrame(Duration.seconds(.6),new KeyValue(a.fillProperty(), basePaint))
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);

        winFx = new ParallelTransition(pulse);
        winFx.play();
    }
    private void clearHighlight(){
        for (Arc a : arcs) {
            a.setEffect(null);
            a.setStroke(METAL_LIGHT);
            a.setStrokeWidth(1.2);

            Paint base = (Paint) a.getProperties().get(BASE_FILL_KEY);
            if (base != null) a.setFill(base);
        }
    }

    /* ============================================================ */
    /* 9)  Dessin : secteurs, anneaux, moyeu                        */
    /* ============================================================ */
    private void addDecorRings(){
        // Large anneau métal sombre
        Circle outer = new Circle(Main.WHEEL_RADIUS + 6, METAL_DARK);
        outer.setStroke(METAL_LIGHT);
        outer.setStrokeWidth(4);

        // Anneau rivets (petits cercles)
        Group rivets = new Group();
        int rivetCount = 32;
        double r = Main.WHEEL_RADIUS + 6;
        for (int i = 0; i < rivetCount; i++){
            double ang = 2*Math.PI*i/rivetCount;
            Circle c = new Circle(
                    r*Math.cos(ang),
                    r*Math.sin(ang),
                    3, METAL_LIGHT
            );
            rivets.getChildren().add(c);
        }
        wheelGroup.getChildren().addAll(outer, rivets);
    }
    private Arc buildSector(double start, double extent, Color tint){
        Arc a = new Arc(0,0, Main.WHEEL_RADIUS, Main.WHEEL_RADIUS, start, extent);
        a.setType(ArcType.ROUND);

        // dégradé radial flammes
        Paint fill = new RadialGradient(
                0, 0,
                0.0, 0.0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, FIRE_START.interpolate(tint, 0.25)),
                new Stop(0.45, tint),
                new Stop(1.00, FIRE_END)
        );
        a.setFill(fill);
        a.getProperties().put(BASE_FILL_KEY, fill);
        a.setStroke(METAL_LIGHT);
        a.setStrokeWidth(1.2);
        return a;
    }

    /* ============================================================ */
    /* 10)  Données : distribution des malus                        */
    /* ============================================================ */
    private void buildSeatArrays(ObservableList<String> malus){
        int n = malus.size();
        seatNames  = malus.toArray(new String[0]);
        seatColors = new Color[n];
        for (int i = 0; i < n; i++) seatColors[i] = colorByIndex(i);
    }

    /* ============================================================ */
    /* 11)  Drag & drop                                             */
    /* ============================================================ */
    private void enableDrag(){
        root.setOnMousePressed(e->{ dragX=e.getSceneX()-root.getTranslateX(); dragY=e.getSceneY()-root.getTranslateY(); root.setCursor(Cursor.CLOSED_HAND);} );
        root.setOnMouseDragged(e->{ root.setTranslateX(e.getSceneX()-dragX); root.setTranslateY(e.getSceneY()-dragY);} );
        root.setOnMouseReleased(e-> root.setCursor(Cursor.OPEN_HAND));
        root.setCursor(Cursor.OPEN_HAND);
    }
}
