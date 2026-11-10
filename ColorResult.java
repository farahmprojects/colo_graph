package graph;

/**
 * Résultat d'une coloration de graphe.
 *
 * <p>Cette classe stocke :</p>
 * <ul>
 *   <li>{@code color[v]}  : couleur assignée au sommet v (entier ≥ 1, 0 = non coloré)</li>
 *   <li>{@code order[v]}  : rang (étape) à laquelle v a été coloré (1 = premier)</li>
 *   <li>{@code ni[c]}     : nombre de sommets déjà colorés quand la couleur c
 *                           a été utilisée pour la première fois (ni[1]=1, ni[2]=2 toujours)</li>
 *   <li>{@code nColors}   : nombre total de couleurs utilisées χ_D(G)</li>
 *   <li>{@code rho}       : ratio ρ = ni[χ_D] / n, fraction du graphe colorée
 *                           avant l'introduction de la dernière couleur</li>
 * </ul>
 *
 * <p><b>Interprétation de ρ :</b></p>
 * <ul>
 *   <li>ρ proche de 0 → la dernière couleur apparaît tôt : structure "difficile" dès le début</li>
 *   <li>ρ = 1         → la dernière couleur n'est nécessaire qu'au dernier sommet</li>
 * </ul>
 */
public class ColorResult {

    // =========================================================================
    // Champs
    // =========================================================================

    /** color[v] = couleur du sommet v (1-indexé ; 0 = non coloré). */
    public final int[] color;

    /** order[v] = rang de coloration du sommet v (1 = premier sommet coloré). */
    public final int[] order;

    /**
     * ni[c] = nombre de sommets colorés juste après que la couleur c
     * a été attribuée pour la première fois.
     * Taille n+1 pour indexer directement par couleur (couleurs ∈ [1..n]).
     */
    public final int[] ni;

    /** Nombre total de couleurs utilisées : χ_D(G). */
    public int nColors;

    /** Ratio ρ = ni[nColors] / n. */
    public double rho;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Initialise un résultat vide pour un graphe à {@code n} sommets.
     * Toutes les couleurs sont à 0 (non coloré).
     *
     * @param n nombre de sommets du graphe
     */
    public ColorResult(int n) {
        color   = new int[n];
        order   = new int[n];
        ni      = new int[n + 1];  // indice 0 inutilisé, couleurs de 1 à n
        nColors = 0;
        rho     = 0.0;
    }

    // =========================================================================
    // Calcul du ratio ρ
    // =========================================================================

    /**
     * Calcule ρ = ni[nColors] / n.
     * À appeler après que tous les sommets ont été colorés.
     *
     * @param n nombre total de sommets du graphe
     */
    public void computeRho(int n) {
        rho = (double) ni[nColors] / n;
    }

    // =========================================================================
    // Vérification de la validité de la coloration
    // =========================================================================

    /**
     * Vérifie que la coloration est propre : aucune arête n'a ses deux
     * extrémités de la même couleur.
     *
     * @param g le graphe coloré
     * @return {@code true} si la coloration est valide, {@code false} sinon
     */
    public boolean verify(Graph g) {
        for (int u = 0; u < g.getN(); u++) {
            for (int v : g.getNeighbors(u)) {
                if (color[u] == color[v]) return false;
            }
        }
        return true;
    }

    // =========================================================================
    // Affichage formaté
    // =========================================================================

    /**
     * Affiche une ligne du tableau de résultats dans le format :
     * <pre>
     *   NomAlgo           χD      ni[χD]       ρ         ms
     * </pre>
     *
     * @param label nom de l'algorithme (affiché dans la 1ère colonne)
     * @param n     nombre de sommets (pour le calcul de ρ)
     * @param ms    temps d'exécution en millisecondes
     */
    public void printTableRow(String label, int n, double ms) {
        System.out.printf("  %-28s %5d  %10d  %7.4f  %10.3f%n",
                label, nColors, ni[nColors], rho, ms);
    }
}
