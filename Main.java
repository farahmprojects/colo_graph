package graph;

/**
 * Point d'entrée principal — coloration de graphes.
 *
 * <h2>Modes d'utilisation</h2>
 *
 * <h3>1. Sans argument — mode démo</h3>
 * <pre>
 *   java -cp bin graph.Main
 * </pre>
 * <p>Exécute le graphe exemple du sujet (n=9) puis une série de graphes aléatoires.</p>
 *
 * <h3>2. Un fichier — mode simple</h3>
 * <pre>
 *   java -cp bin graph.Main monGraphe.col
 *   java -cp bin graph.Main monGraphe.txt snap
 *   java -cp bin graph.Main monGraphe.tsv konect
 * </pre>
 * <p>Le deuxième argument optionnel force le format : {@code dimacs}, {@code snap},
 * {@code konect} ou {@code auto} (détection par extension, valeur par défaut).</p>
 *
 * <h3>3. Plusieurs fichiers — mode batch</h3>
 * <pre>
 *   java -cp bin graph.Main --batch fichier1.col fichier2.txt ...
 * </pre>
 * <p>Benchmark tous les fichiers listés en séquence.</p>
 *
 * <h2>Algorithmes comparés</h2>
 * <ul>
 *   <li>DSATUR naïf O(n²) / O(n²) — {@link Dsatur}</li>
 *   <li>DSATUR optimisé O((n+m) log n) / O(n+m) — {@link DsaturOpt} <i>[bonus 5b]</i></li>
 *   <li>Glouton ordre naturel — {@link Greedy#naturalOrder} <i>[bonus 5a]</i></li>
 *   <li>Glouton Welsh-Powell — {@link Greedy#welshPowell} <i>[bonus 5a]</i></li>
 * </ul>
 */
public class Main {

    // =========================================================================
    // Méthode de benchmark
    // =========================================================================

    /**
     * Lance les quatre algorithmes de coloration sur le graphe {@code g}
     * et affiche les résultats dans un tableau formaté.
     *
     * <p>Mesure le temps d'exécution de chaque algorithme séparément
     * avec {@code System.nanoTime()} pour une précision sous-milliseconde.</p>
     *
     * @param g    graphe à colorier
     * @param name nom affiché dans l'en-tête du tableau
     */
    static void benchmark(Graph g, String name) {
        int n = g.getN();
        int m = g.getM();
        // Densité du graphe : proportion d'arêtes existantes parmi les arêtes possibles
        double density = n > 1 ? (2.0 * m) / ((double) n * (n - 1)) : 0;

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Graphe : " + name);
        System.out.printf("  n=%d  m=%d  densité=%.6f%n", n, m, density);
        System.out.println();

        // --- Mesures de temps + exécution ---
        long t0;
        t0 = System.nanoTime(); ColorResult rd = Dsatur.colorize(g);           double td = (System.nanoTime() - t0) / 1e6;
        t0 = System.nanoTime(); ColorResult ro = DsaturOpt.colorize(g);        double to = (System.nanoTime() - t0) / 1e6;
        t0 = System.nanoTime(); ColorResult rn = Greedy.naturalOrder(g);       double tn = (System.nanoTime() - t0) / 1e6;
        t0 = System.nanoTime(); ColorResult rw = Greedy.welshPowell(g);        double tw = (System.nanoTime() - t0) / 1e6;

        // --- Affichage tabulaire ---
        System.out.printf("  %-28s %5s %10s %7s %10s%n",
                "Algorithme", "χD", "ni[χD]", "ρ", "ms");
        rd.printTableRow("DSATUR (naïf)",         n, td);
        ro.printTableRow("DSATUR (optimisé)",      n, to);
        rn.printTableRow("Glouton naturel",         n, tn);
        rw.printTableRow("Glouton Welsh-Powell",    n, tw);
        System.out.println();
    }

    // =========================================================================
    // Graphe exemple du sujet (n=9)
    // =========================================================================

    /**
     * Construit le graphe exemple décrit dans le sujet (9 sommets).
     * Utilisé pour valider les implémentations sur un cas connu.
     *
     * @return graphe exemple
     */
    static Graph makeExample() {
        Graph g = new Graph(9);
        int[][] edges = {
            {5,4},{5,6},{5,7},{5,0},{5,2},
            {4,6},{4,7},{4,0},
            {6,7},{6,8},
            {7,8},
            {0,3},
            {2,3},{1,3}
        };
        for (int[] e : edges) g.addEdge(e[0], e[1]);
        return g;
    }

    // =========================================================================
    // Point d'entrée
    // =========================================================================

    /**
     * Point d'entrée principal.
     *
     * @param args arguments de ligne de commande (voir Javadoc de la classe)
     * @throws Exception en cas d'erreur de chargement de fichier
     */
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║            Coloration de Graphes             ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // ---- Mode démo (aucun argument) ----
        if (args.length == 0) {
            System.out.println("=== Graphe exemple du sujet ===\n");
            benchmark(makeExample(), "Exemple sujet (n=9)");

            System.out.println("=== Graphes aléatoires G(n, p) ===\n");
            int[]    sizes = {100, 500, 1000};
            double[] probs = {0.05, 0.10, 0.30};
            for (int s : sizes) {
                for (double p : probs) {
                    benchmark(Graph.randomGraph(s, p, 42),
                              String.format("G(%d, %.2f)", s, p));
                }
            }
            return;
        }

        // ---- Mode batch : --batch fichier1 fichier2 ... ----
        if (args[0].equals("--batch")) {
            for (int i = 1; i < args.length; i++) {
                try {
                    benchmark(Graph.loadAuto(args[i]), args[i]);
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement de : " + args[i]);
                }
            }
            return;
        }

        // ---- Mode simple : un fichier avec format optionnel ----
        String fmt = (args.length >= 2) ? args[1] : "auto";
        Graph g = switch (fmt) {
            case "dimacs" -> Graph.loadDimacs(args[0]);
            case "snap"   -> Graph.loadSnap(args[0]);
            case "konect" -> Graph.loadKonect(args[0]);
            default       -> Graph.loadAuto(args[0]);
        };
        benchmark(g, args[0]);

        // Vérification de la validité de la coloration DSATUR optimisé
        boolean valid = DsaturOpt.colorize(g).verify(g);
        System.out.println("Vérification DSATUR opt : " + (valid ? "OK ✓" : "ERREUR ✗"));
    }
}
