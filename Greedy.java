package graph;

import java.util.Arrays;

/**
 * Algorithmes gloutons de coloration de graphe — <b>Question bonus 5(a)</b>.
 *
 * <h2>Principe des algorithmes gloutons</h2>
 * <p>Un algorithme glouton colorie les sommets dans un ordre fixé à l'avance,
 * en attribuant à chaque sommet la plus petite couleur non utilisée par ses
 * voisins déjà colorés. Contrairement à DSATUR, l'ordre est calculé une seule
 * fois avant le début de la coloration et ne dépend pas de l'état courant.</p>
 *
 * <h2>Deux variantes implantées</h2>
 * <ul>
 *   <li>{@link #naturalOrder} : ordre des sommets 0, 1, 2, …, n-1</li>
 *   <li>{@link #welshPowell} : ordre des sommets par degré décroissant (Welsh-Powell, 1967)</li>
 * </ul>
 *
 * <h2>Complexité commune</h2>
 * <ul>
 *   <li><b>Temps</b> : O(n + m) pour la coloration (O(n log n) en plus pour Welsh-Powell)</li>
 *   <li><b>Mémoire</b> : O(n) — tableau {@code used} de taille n+2</li>
 * </ul>
 *
 * <p>Les algorithmes gloutons sont généralement plus rapides que DSATUR mais
 * produisent souvent plus de couleurs (moins bonne qualité de coloration).</p>
 */
public class Greedy {

    // =========================================================================
    // Méthode commune de coloration dans un ordre donné
    // =========================================================================

    /**
     * Colorie le graphe {@code g} en visitant les sommets dans l'ordre {@code perm}.
     *
     * <p>Pour chaque sommet {@code v} de la permutation :</p>
     * <ol>
     *   <li>On marque les couleurs déjà utilisées par ses voisins colorés.</li>
     *   <li>On attribue la plus petite couleur non marquée.</li>
     *   <li>On nettoie le tableau de marquage pour la prochaine itération.</li>
     * </ol>
     *
     * <p>Le tableau {@code used} est réutilisé à chaque étape et nettoyé
     * manuellement (sans {@code Arrays.fill}) pour rester en O(degré) par sommet.</p>
     *
     * @param g    graphe à colorier
     * @param perm permutation des indices de sommets définissant l'ordre de visite
     * @return résultat de coloration
     */
    private static ColorResult colorizeByOrder(Graph g, int[] perm) {
        int n = g.getN();
        ColorResult r = new ColorResult(n);

        // used[c] = true si la couleur c est déjà utilisée par un voisin coloré du sommet courant
        // Taille n+2 pour accepter jusqu'à n couleurs (cas dégénéré : chemin complet)
        boolean[] used = new boolean[n + 2];

        for (int step = 0; step < n; step++) {
            int v = perm[step];

            // ---- Marquer les couleurs des voisins déjà colorés ----
            for (int w : g.getNeighbors(v)) {
                if (r.color[w] != 0) {
                    used[r.color[w]] = true;
                }
            }

            // ---- Choisir la plus petite couleur disponible ----
            int c = 1;
            while (used[c]) c++;

            r.color[v] = c;
            r.order[v] = step + 1;

            // Enregistrer la première apparition de la couleur c
            if (c > r.nColors) {
                r.nColors = c;
                r.ni[c]   = step + 1;
            }

            // ---- Nettoyer le tableau used (uniquement les cases utilisées) ----
            // On réinitialise uniquement les couleurs marquées pour rester en O(degré)
            for (int w : g.getNeighbors(v)) {
                if (r.color[w] != 0) {
                    used[r.color[w]] = false;
                }
            }
        }

        r.computeRho(n);
        return r;
    }

    // =========================================================================
    // Variante 1 : ordre naturel
    // =========================================================================

    /**
     * Coloration gloutonne dans l'ordre naturel des sommets (0, 1, …, n-1).
     *
     * <p>C'est la variante la plus simple : l'ordre de visite est simplement
     * l'ordre d'indexation. La qualité de la coloration dépend fortement de
     * la numérotation choisie pour les sommets du graphe.</p>
     *
     * @param g graphe à colorier
     * @return résultat de coloration
     */
    public static ColorResult naturalOrder(Graph g) {
        int n = g.getN();
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;   // identité
        return colorizeByOrder(g, perm);
    }

    // =========================================================================
    // Variante 2 : algorithme Welsh-Powell
    // =========================================================================

    /**
     * Coloration gloutonne par ordre de degré décroissant (Welsh-Powell, 1967).
     *
     * <p>L'idée est de colorier en premier les sommets les plus "contraignants"
     * (degré élevé), ce qui réduit en général le nombre de couleurs nécessaires
     * par rapport à l'ordre naturel.</p>
     *
     * <p><b>Borne théorique :</b> Welsh-Powell garantit χ(G) ≤ max_v min(d(v)+1, n).</p>
     *
     * <p>En cas d'égalité de degrés, on trie par indice croissant pour rendre
     * le résultat déterministe.</p>
     *
     * @param g graphe à colorier
     * @return résultat de coloration
     */
    public static ColorResult welshPowell(Graph g) {
        int n = g.getN();

        // Créer le tableau de permutation trié par degré décroissant
        // (Integer[] pour utiliser un comparateur lambda)
        Integer[] perm = new Integer[n];
        for (int i = 0; i < n; i++) perm[i] = i;

        Arrays.sort(perm, (a, b) -> {
            int diff = g.getDegree(b) - g.getDegree(a);   // degré décroissant
            return diff != 0 ? diff : a - b;              // indice croissant (bris d'égalité)
        });

        // Convertir Integer[] → int[] pour colorizeByOrder
        int[] permInt = new int[n];
        for (int i = 0; i < n; i++) permInt[i] = perm[i];

        return colorizeByOrder(g, permInt);
    }
}
