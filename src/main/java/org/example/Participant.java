package org.example;

/**
 * Simple classe de données décrivant un participant :
 * - Pseudo (name)
 * - Classe du personnage
 * - Niveau (level)
 */
public class Participant {
    private String name;
    private int level;         // Niveau du joueur
    private String classe;     // Classe du personnage

    public Participant(String name, int level, String classe) {
        this.name = name;
        this.level = level;
        this.classe = classe;
    }

    /* ============== Getters et Setters ============== */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }
}
