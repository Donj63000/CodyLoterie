package org.example;

import javafx.collections.ObservableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Outils très simples pour :
 *   • sauvegarder l’état courant dans un fichier texte ;
 *   • remettre à zéro toutes les listes (nouvelle loterie).
 *
 * On reste volontairement « light » : un seul fichier texte,
 * pas de dépendance externe, format ultra-lisible.
 */
public final class Save {

    private Save() {}                      // classe utilitaire

    private static final Path FILE = Path.of("loterie-save.txt");

    /* ---------- Sauvegarde ---------- */
    public static void save(ObservableList<Participant> participants) throws IOException {

        StringBuilder sb = new StringBuilder("#Participants\n");
        for (Participant p : participants) {
            // nom;level;classe
            sb.append(p.getName()).append(';')
                    .append(p.getLevel()).append(';')
                    .append(p.getClasse()).append('\n');
        }

        // Ancienne section Objets/Bons supprimée

        Files.writeString(FILE, sb.toString());
    }

    /* ---------- Nettoyage ---------- */
    public static void reset(ObservableList<Participant> participants) {
        participants.clear();
    }
}
