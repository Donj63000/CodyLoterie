package org.example;

import javafx.beans.property.*;

public class Participant {

    private final StringProperty  name   = new SimpleStringProperty();
    private final IntegerProperty level  = new SimpleIntegerProperty();
    private final StringProperty  classe = new SimpleStringProperty();

    public Participant(String n, int l, String c) {
        name.set(n); level.set(l); classe.set(c);
    }

    public String getName()   { return name.get(); }
    public int    getLevel()  { return level.get(); }
    public String getClasse() { return classe.get(); }

    public void setName(String v){ name.set(v); }
    public void setLevel(int v){  level.set(v); }
    public void setClasse(String v){ classe.set(v); }

    public StringProperty  nameProperty()   { return name; }
    public IntegerProperty levelProperty()  { return level; }
    public StringProperty  classeProperty() { return classe; }
}
