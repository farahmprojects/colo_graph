package graph;

/**
 * Benchmark grands graphes — <b>Question bonus 5(b)</b>.
 *
 * <p>Compare les deux implémentations de DSATUR sur des graphes de taille
 * croissante pour mettre en évidence le gain apporté par l'optimisation
 * mémoire (HashSet) et l'optimisation temps (PriorityQueue + lazy deletion).</p>
 *
 * <h2>Protocole</h2>
 * <ul>
 *   <li>Graphes aléatoires G(n, p) avec graine fixe 42 pour reproductibilité</li>
 *   <li>La densité p est ajustée pour éviter des graphes trop denses
 *       (et donc trop lents) aux grandes tailles</li>
 *   <li>La version naïve est ignorée (skipped) pour n &gt; 2 000 car elle
 *       consomme trop de mémoire et prend un temps prohibitif</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 *   java -cp bin graph.BenchmarkLarge
 * </pre>
 */
public class BenchmarkLarge {

    /**
     * Lance le benchmark et affiche les résultats sur la sortie standard.
     *
     * @param args ignoré
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║   Benchmark : DSATUR naïf O(n²) vs optimisé O((n+m) log n)      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("  %-17s %7s %9s %6s %12s %10s %8s%n",
                "Graphe", "n", "m", "χD", "naïf (ms)", "opt (ms)", "gain");
        System.out.println("  " + "─".repeat(72));

        // Tailles et densités croissantes
        // La densité diminue pour n grand afin de garder m raisonnable
        int[]    sizes = {500, 1000, 2000, 5000, 10000};
        double[] probs = {0.05, 0.05, 0.05, 0.02,  0.01};

        for (int i = 0; i < sizes.length; i++) {
            int    n = sizes[i];
            double p = probs[i];

            // Construire le graphe aléatoire avec graine fixe pour reproductibilité
            Graph g = Graph.randomGraph(n, p, 42);

            // ---- Version naïve (limitée à n ≤ 2000 pour éviter OOM) ----
            double tNaif  = -1;
            int    chiNaif = -1;
            if (n <= 2000) {
                long t0 = System.nanoTime();
                ColorResult r = Dsatur.colorize(g);
                tNaif   = (System.nanoTime() - t0) / 1e6;
                chiNaif = r.nColors;
            }

            // ---- Version optimisée (fonctionne pour toutes les tailles) ----
            long t0 = System.nanoTime();
            ColorResult rOpt = DsaturOpt.colorize(g);
            double tOpt = (System.nanoTime() - t0) / 1e6;

            // Vérifier que les deux versions donnent le même χ_D
            boolean sameResult = (chiNaif == -1 || chiNaif == rOpt.nColors);

            // Formatage des colonnes
            String naifStr = (tNaif < 0) ? "    (skipped)"
                                          : String.format("%12.1f", tNaif);
            String gainStr = (tNaif < 0) ? "       —"
                                          : String.format("%6.1fx", tNaif / tOpt);
            String label   = String.format("G(%d, %.2f)", n, p);

            System.out.printf("  %-17s %7d %9d %6d %s %10.1f %s %s%n",
                    label, n, g.getM(), rOpt.nColors,
                    naifStr, tOpt, gainStr,
                    sameResult ? "" : "⚠ χ diffère !");
        }

        // ---- Rappel théorique de la consommation mémoire ----
        System.out.println();
        System.out.println("  Consommation mémoire nc[n][n+2] (version naïve) :");
        System.out.printf("    n =  1 000 → %6.0f Mo%n",  1_000.0 *  1_002 / 1e6);
        System.out.printf("    n = 10 000 → %6.0f Mo%n", 10_000.0 * 10_002 / 1e6);
        System.out.printf("    n = 50 000 → %6.0f Go%n", 50_000.0 * 50_002 / 1e9);
        System.out.println();
        System.out.println("  Mémoire HashSet (version optimisée) ≈ O(n + m) → toujours raisonnable.");
        System.out.println("  La version optimisée est utilisable sur des graphes SNAP à 1M de sommets.");
    }
}
