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
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Roue {

    private static final Color METAL_LIGHT = Color.web("#cfcfcf");
    private static final Color METAL_DARK = Color.web("#777777");
    private static final Color FIRE_START = Color.web("#ff5722");
    private static final Color FIRE_END = Color.web("#8b0000");
    private static final Color HIGHLIGHT = Color.web("#ff2200");
    private static final double GOLDEN_ANGLE = 137.50776405003785;
    private static Color color(int i){ return Color.hsb((i*GOLDEN_ANGLE)%360,.65,.9); }

    private final StackPane root;
    private final Group wheelGroup;
    private final ImageView wheelImg=new ImageView();
    private final Resultat resultat;
    private final List<Arc> arcs=new ArrayList<>();
    private String[] seatNames=new String[0];
    private Color[] seatColors=new Color[0];
    private ParallelTransition winFx;
    private Consumer<String> spinCallback;
    private AnimationTimer spinTimer;
    private double dragX,dragY;

    private long startNanos;
    private double totalAngle;
    private double durationSec;

    public Roue(Resultat r){
        resultat=r;
        root=new StackPane();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(Main.WHEEL_RADIUS*2,Main.WHEEL_RADIUS*2);
        wheelGroup=new Group();
        wheelGroup.setCache(true);
        wheelGroup.setCacheHint(CacheHint.SPEED);
        wheelImg.setSmooth(true);
        wheelImg.setCache(true);
        wheelImg.setCacheHint(CacheHint.ROTATE);
        root.getChildren().addAll(wheelGroup,wheelImg);
        SVGPath spear=new SVGPath();
        spear.setContent("M0,-"+(Main.WHEEL_RADIUS+18)+" L-8,-"+(Main.WHEEL_RADIUS-4)+" L0,-"+(Main.WHEEL_RADIUS-14)+" L8,-"+(Main.WHEEL_RADIUS-4)+" Z");
        spear.setFill(HIGHLIGHT);
        spear.setStroke(Color.BLACK);
        spear.setStrokeWidth(1.2);
        root.getChildren().add(spear);
        root.setOnMousePressed(e->{dragX=e.getSceneX()-root.getTranslateX();dragY=e.getSceneY()-root.getTranslateY();root.setCursor(Cursor.CLOSED_HAND);});
        root.setOnMouseDragged(e->{root.setTranslateX(e.getSceneX()-dragX);root.setTranslateY(e.getSceneY()-dragY);});
        root.setOnMouseReleased(e->root.setCursor(Cursor.OPEN_HAND));
        root.setCursor(Cursor.OPEN_HAND);
    }

    public Node getRootPane(){return root;}
    public void resetPosition(){root.setTranslateX(0);root.setTranslateY(0);}
    public void setOnSpinFinished(Consumer<String> cb){spinCallback=cb;}

    public void updateWheelDisplay(ObservableList<String> malus){
        int n=malus.size();
        seatNames=malus.toArray(new String[0]);
        seatColors=new Color[n];
        for(int i=0;i<n;i++)seatColors[i]=color(i);
        wheelGroup.getChildren().clear();
        arcs.clear();
        addRings();
        double step=360d/n;
        double a=0;
        for(int i=0;i<n;i++){arcs.add(addSector(a,step,seatColors[i]));a+=step;}
        snapshot();
    }

    public void spinTheWheel(ObservableList<String> m){updateWheelDisplay(m);spin();}

    private void spin(){
        if(spinTimer!=null)spinTimer.stop();
        if(winFx!=null){winFx.stop();clearHighlight();}
        if(seatNames.length==0){resultat.setMessage("Aucun malus – impossible de lancer la roue.");return;}
        int idx=ThreadLocalRandom.current().nextInt(seatNames.length);
        double sector=360.0/seatNames.length;
        totalAngle=5*360+idx*sector+sector/2-90;
        durationSec=OptionRoue.getSpinDuration();
        wheelGroup.setVisible(false);
        wheelImg.setRotate(0);
        wheelImg.setVisible(true);
        startNanos=System.nanoTime();
        spinTimer=new AnimationTimer(){
            @Override public void handle(long now){
                double t=(now-startNanos)/1_000_000_000.0;
                if(t>=durationSec){
                    wheelImg.setRotate(totalAngle);
                    stop();
                    wheelGroup.setRotate(wheelImg.getRotate());
                    wheelImg.setVisible(false);
                    wheelGroup.setVisible(true);
                    String m=seatNames[idx];
                    resultat.setMessage("Malus : "+m);
                    if(spinCallback!=null)spinCallback.accept(m);
                    highlight(idx);
                    return;
                }
                double f=t/durationSec;
                double ease=1-Math.pow(1-f,5);
                wheelImg.setRotate(totalAngle*ease);
            }
        };
        spinTimer.start();
    }

    private void highlight(int idx){
        if(idx<0||idx>=arcs.size())return;
        Arc a=arcs.get(idx);
        a.setEffect(new Glow(1));
        Timeline p=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(a.fillProperty(),HIGHLIGHT)),
                new KeyFrame(Duration.seconds(.6),new KeyValue(a.fillProperty(),a.getFill()))
        );
        p.setCycleCount(Animation.INDEFINITE);
        p.setAutoReverse(true);
        winFx=new ParallelTransition(p);
        winFx.play();
    }

    private void clearHighlight(){arcs.forEach(x->{x.setEffect(null);x.setStroke(METAL_LIGHT);x.setStrokeWidth(1.2);});}

    private void snapshot(){
        wheelGroup.setRotate(0);
        wheelGroup.applyCss();
        wheelGroup.layout();
        SnapshotParameters sp=new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage img=wheelGroup.snapshot(sp,null);
        wheelImg.setImage(img);
        wheelImg.setFitWidth(img.getWidth());
        wheelImg.setFitHeight(img.getHeight());
    }

    private void addRings(){
        Circle outer=new Circle(Main.WHEEL_RADIUS+6,METAL_DARK);
        outer.setStroke(METAL_LIGHT);
        outer.setStrokeWidth(4);
        Group rivets=new Group();
        int n=32;double r=Main.WHEEL_RADIUS+6;
        for(int i=0;i<n;i++){
            double a=2*Math.PI*i/n;
            rivets.getChildren().add(new Circle(r*Math.cos(a),r*Math.sin(a),3,METAL_LIGHT));
        }
        wheelGroup.getChildren().addAll(outer,rivets);
    }

    private Arc addSector(double start,double extent,Color tint){
        Arc a=new Arc(0,0,Main.WHEEL_RADIUS,Main.WHEEL_RADIUS,start,extent);
        a.setType(ArcType.ROUND);
        a.setFill(new RadialGradient(0,0,0,0,1,true,CycleMethod.NO_CYCLE,
                new Stop(0,FIRE_START.interpolate(tint,.25)),
                new Stop(.45,tint),
                new Stop(1,FIRE_END)));
        a.setStroke(METAL_LIGHT);
        a.setStrokeWidth(1.2);
        wheelGroup.getChildren().add(a);
        return a;
    }
}
