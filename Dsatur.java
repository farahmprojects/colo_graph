package graph;

/**
 * Algorithme DSATUR — version naïve (référence).
 *
 * <h2>Principe</h2>
 * <p>DSATUR colorie les sommets un à un en choisissant
 * à chaque étape le sommet non coloré dont le <em>degré de saturation</em>
 * (DSAT = nombre de couleurs distinctes présentes chez ses voisins déjà colorés)
 * est maximal. En cas d'égalité on prend le sommet de plus grand degré,
 * puis le plus petit indice.</p>
 *
 * <p>La couleur attribuée est la plus petite couleur non présente chez les voisins.</p>
 *
 * <h2>Complexité — version naïve</h2>
 * <ul>
 *   <li><b>Temps</b> : O(n²) — à chaque étape, scan linéaire sur tous les sommets
 *       pour trouver le candidat de DSAT maximum</li>
 *   <li><b>Mémoire</b> : O(n²) — tableau {@code nc[n][n+2]} stockant pour chaque sommet
 *       les couleurs interdites (1 bit par couleur × n couleurs possibles × n sommets)</li>
 * </ul>
 *
 * <p>Ces complexités limitent l'utilisation à des graphes d'ordre n ≤ 2 000 environ
 * (au-delà, le tableau {@code nc} consomme plusieurs centaines de Mo et le temps
 * devient prohibitif — voir {@link DsaturOpt} pour la version optimisée).</p>
 */
public class Dsatur {

    /**
     * Lance la coloration DSATUR naïve sur le graphe {@code g}.
     *
     * @param g graphe à colorier
     * @return résultat de coloration (couleurs, ordres, ni, nColors, ρ)
     */
    public static ColorResult colorize(Graph g) {
        int n = g.getN();
        ColorResult r = new ColorResult(n);

        // dsat[v] = degré de saturation courant du sommet v
        int[] dsat = new int[n];

        // nc[v][c] = true si la couleur c est déjà présente chez un voisin coloré de v
        // Taille [n+2] : couleurs de 1 à n (indice 0 inutilisé, +2 pour marge de sécurité)
        // ⚠ PROBLÈME MÉMOIRE : O(n²) → ~400 Mo pour n = 10 000
        boolean[][] nc = new boolean[n][n + 2];

        // --- n étapes de coloration ---
        for (int step = 0; step < n; step++) {

            // ---- Étape 1 : sélectionner le meilleur sommet non coloré ----
            // Critère : max DSAT, puis max degré, puis min indice
            int best = -1;
            for (int v = 0; v < n; v++) {
                if (r.color[v] != 0) continue;     // déjà coloré, on ignore
                if (best == -1) { best = v; continue; }

                int dv = dsat[v],  db = dsat[best];
                int ev = g.getDegree(v), eb = g.getDegree(best);

                if (dv > db)                          best = v;  // DSAT plus grand
                else if (dv == db && ev > eb)         best = v;  // égalité DSAT → degré plus grand
                else if (dv == db && ev == eb && v < best) best = v; // tout égal → plus petit indice
            }

            // ---- Étape 2 : attribuer la plus petite couleur disponible ----
            int c = 1;
            while (nc[best][c]) c++;   // cherche la 1ère couleur non interdite

            r.color[best] = c;
            r.order[best] = step + 1;

            // Enregistre la première apparition de la couleur c
            if (c > r.nColors) {
                r.nColors  = c;
                r.ni[c]    = step + 1;
            }

            // ---- Étape 3 : mettre à jour DSAT des voisins non colorés ----
            for (int w : g.getNeighbors(best)) {
                if (r.color[w] != 0) continue;       // voisin déjà coloré : rien à faire
                if (!nc[w][c]) {                      // couleur c nouvelle pour w ?
                    nc[w][c] = true;                  //   → marquer c comme interdite
                    dsat[w]++;                        //   → incrémenter son DSAT
                }
            }
        }

        r.computeRho(n);
        return r;
    }
}
