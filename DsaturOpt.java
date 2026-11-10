package graph;

import java.util.*;

/**
 * Algorithme DSATUR — version optimisée pour les grands graphes.
 *
 * <h2>Deux optimisations par rapport à la version naïve</h2>
 *
 * <h3>Optimisation 1 — Mémoire : HashSet au lieu de boolean[][]</h3>
 * <p>La version naïve utilise {@code nc[v][c]} (tableau 2D boolean) pour savoir
 * si la couleur {@code c} est interdite au sommet {@code v}.
 * Cela coûte O(n²) en mémoire :</p>
 * <ul>
 *   <li>n =  1 000 → ~1 Mo (acceptable)</li>
 *   <li>n = 10 000 → ~100 Mo (problématique)</li>
 *   <li>n = 50 000 → ~2,5 Go (impossible en pratique)</li>
 * </ul>
 * <p>La version optimisée remplace ce tableau par un {@code HashSet<Integer>}
 * par sommet qui stocke uniquement les couleurs effectivement présentes chez
 * les voisins colorés. La mémoire totale est alors O(n + m), ce qui reste
 * raisonnable même pour des graphes à un million de sommets.</p>
 *
 * <h3>Optimisation 2 — Temps : PriorityQueue + lazy deletion au lieu du scan O(n)</h3>
 * <p>La version naïve parcourt tous les n sommets à chaque étape pour trouver
 * le sommet de DSAT maximum → O(n) par étape → O(n²) total.</p>
 *
 * <p>La version optimisée maintient une {@link PriorityQueue} (tas max) qui extrait
 * le meilleur sommet en O(log n). Comme Java ne permet pas de modifier la priorité
 * d'un élément déjà dans la PQ, on utilise le <em>pattern lazy deletion</em> :</p>
 * <ol>
 *   <li>Quand le DSAT d'un sommet augmente, on insère une <b>nouvelle entrée</b>
 *       dans la PQ avec la valeur de DSAT mise à jour.</li>
 *   <li>Quand on extrait une entrée, on vérifie si elle est "périmée"
 *       ({@code entry.dsat != dsat[vertex]}) et on la saute si c'est le cas.</li>
 * </ol>
 *
 * <h2>Complexité — version optimisée</h2>
 * <ul>
 *   <li><b>Temps</b> : O((n + m) log n)</li>
 *   <li><b>Mémoire</b> : O(n + m)</li>
 * </ul>
 *
 * <h2>Résultat garanti identique à la version naïve</h2>
 * <p>Le critère de sélection (DSAT décroissant, puis degré décroissant,
 * puis indice croissant) est strictement identique à {@link Dsatur}.
 * Les deux algorithmes produisent donc exactement la même coloration.</p>
 */
public class DsaturOpt {

    // =========================================================================
    // Classe interne : entrée de la file de priorité
    // =========================================================================

    /**
     * Entrée immutable dans la PriorityQueue.
     *
     * <p>L'ordre de comparaison reflète le critère DSATUR :</p>
     * <ol>
     *   <li>DSAT décroissant (priorité max)</li>
     *   <li>Degré décroissant (en cas d'égalité)</li>
     *   <li>Indice de sommet croissant (bris d'égalité final)</li>
     * </ol>
     */
    private static class PQEntry implements Comparable<PQEntry> {
        /** Indice du sommet dans le graphe. */
        final int vertex;
        /** Valeur de DSAT au moment de l'insertion (peut être périmée). */
        final int dsat;
        /** Degré du sommet (invariant, ne change pas). */
        final int degree;

        PQEntry(int vertex, int dsat, int degree) {
            this.vertex = vertex;
            this.dsat   = dsat;
            this.degree = degree;
        }

        @Override
        public int compareTo(PQEntry o) {
            // 1. DSAT décroissant : o.dsat - this.dsat (ordre naturel Java = min-heap → on inverse)
            if (this.dsat   != o.dsat)   return o.dsat   - this.dsat;
            // 2. Degré décroissant
            if (this.degree != o.degree) return o.degree - this.degree;
            // 3. Indice croissant (bris d'égalité déterministe)
            return this.vertex - o.vertex;
        }
    }

    // =========================================================================
    // Algorithme principal
    // =========================================================================

    /**
     * Lance la coloration DSATUR optimisée sur le graphe {@code g}.
     *
     * @param g graphe à colorier
     * @return résultat de coloration (couleurs, ordres, ni, nColors, ρ)
     */
    @SuppressWarnings("unchecked")
    public static ColorResult colorize(Graph g) {
        int n = g.getN();
        ColorResult r = new ColorResult(n);

        // dsat[v] = degré de saturation courant du sommet v
        int[] dsat = new int[n];

        // neighborColors[v] = ensemble des couleurs déjà présentes chez les voisins colorés de v
        // Mémoire totale : O(n + m) au lieu de O(n²)
        HashSet<Integer>[] neighborColors = new HashSet[n];
        for (int i = 0; i < n; i++) {
            neighborColors[i] = new HashSet<>();
        }

        // File de priorité (max-heap) sur le critère DSATUR
        // Au démarrage : tous les sommets ont DSAT = 0, triés par degré décroissant
        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        for (int v = 0; v < n; v++) {
            pq.offer(new PQEntry(v, 0, g.getDegree(v)));
        }

        // --- n étapes de coloration ---
        for (int step = 0; step < n; step++) {

            // ---- Étape 1 : extraire le meilleur sommet (lazy deletion) ----
            // On saute les entrées périmées ou les sommets déjà colorés
            PQEntry entry;
            do {
                entry = pq.poll();
            } while (entry != null &&
                    (r.color[entry.vertex] != 0          // sommet déjà coloré
                  || entry.dsat != dsat[entry.vertex])); // entrée périmée (DSAT a changé)

            if (entry == null) break;   // ne devrait pas arriver si le graphe est cohérent
            int best = entry.vertex;

            // ---- Étape 2 : attribuer la plus petite couleur disponible ----
            // On cherche le premier entier ≥ 1 absent de neighborColors[best]
            int c = 1;
            while (neighborColors[best].contains(c)) c++;

            r.color[best] = c;
            r.order[best] = step + 1;

            // Enregistre la première apparition de la couleur c
            if (c > r.nColors) {
                r.nColors = c;
                r.ni[c]   = step + 1;
            }

            // ---- Étape 3 : mettre à jour les voisins non colorés ----
            for (int w : g.getNeighbors(best)) {
                if (r.color[w] != 0) continue;            // voisin déjà coloré : rien à faire

                // neighborColors[w].add(c) renvoie true seulement si c était absent
                // → DSAT augmente uniquement si c est une couleur vraiment nouvelle pour w
                if (neighborColors[w].add(c)) {
                    dsat[w]++;
                    // Insérer une nouvelle entrée dans la PQ avec le DSAT mis à jour
                    // (l'ancienne entrée sera reconnue comme périmée et ignorée)
                    pq.offer(new PQEntry(w, dsat[w], g.getDegree(w)));
                }
            }
        }

        r.computeRho(n);
        return r;
    }
}
