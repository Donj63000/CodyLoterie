package org.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.bonus.Bonus;

public final class Participant {

    private final StringProperty  name   = new SimpleStringProperty(this, "name",   "");
    private final IntegerProperty level  = new SimpleIntegerProperty(this, "level", 1);
    private final StringProperty  classe = new SimpleStringProperty(this, "classe", "");
    private final ObservableList<Bonus> bonuses = FXCollections.observableArrayList();

    public Participant(String n, int l, String c) {
        setName(n);
        setLevel(l);
        setClasse(c);
    }

    public String  getName()             { return name.get(); }
    public int     getLevel()            { return level.get(); }
    public String  getClasse()           { return classe.get(); }

    public void    setName(String v)     { name  .set(v == null ? "" : v.trim()); }
    public void    setLevel(int v)       { level .set(Math.max(v, 0)); }
    public void    setClasse(String v)   { classe.set(v == null ? "" : v.trim()); }

    public StringProperty  nameProperty()   { return name;   }
    public IntegerProperty levelProperty()  { return level;  }
    public StringProperty  classeProperty() { return classe; }

    public ObservableList<Bonus> getBonusList()        { return bonuses; }
    public void addBonus(Bonus b)                       { if (b != null) bonuses.add(b); }
    public void removeBonus(Bonus b)                    { bonuses.remove(b); }

    @Override public String toString() { return name.get(); }
}
