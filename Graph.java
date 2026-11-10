package graph;

import java.io.*;
import java.util.*;

/**
 * Représentation d'un graphe non orienté par liste d'adjacence.
 *
 * <p>Chaque sommet est identifié par un entier dans [0, n-1].
 * Les arêtes sont stockées sous forme de listes d'adjacence (ArrayList),
 * ce qui donne :</p>
 * <ul>
 *   <li>Ajout d'arête : O(degré) pour vérifier les doublons</li>
 *   <li>Parcours des voisins : O(degré)</li>
 *   <li>Mémoire : O(n + m)</li>
 * </ul>
 *
 * <p>Formats de fichiers supportés :</p>
 * <ul>
 *   <li><b>DIMACS</b> (.col, .clq) : format compétition de coloration</li>
 *   <li><b>SNAP</b> (.txt, .gz dézippé) : graphes Stanford Network Analysis Project</li>
 *   <li><b>KONECT</b> (.tsv, .konect) : graphes Koblenz Network Collection</li>
 * </ul>
 */
public class Graph {

    /** Nombre de sommets (fixé à la construction, immuable). */
    private final int n;

    /** Nombre d'arêtes (incrémenté à chaque addEdge réussie). */
    private int m;

    /** Liste d'adjacence : adj.get(v) contient les voisins de v. */
    private final List<List<Integer>> adj;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Crée un graphe vide à {@code n} sommets (sans arête).
     *
     * @param n nombre de sommets, doit être >= 1
     */
    public Graph(int n) {
        this.n   = n;
        this.m   = 0;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    // =========================================================================
    // Modification du graphe
    // =========================================================================

    /**
     * Ajoute l'arête {u, v} si elle n'existe pas déjà.
     * Les boucles (u == v) et les arêtes hors bornes sont ignorées silencieusement.
     *
     * @param u premier sommet
     * @param v second sommet
     */
    public void addEdge(int u, int v) {
        // Validation : indices valides et pas de boucle
        if (u < 0 || v < 0 || u >= n || v >= n || u == v) return;
        // Éviter les multi-arêtes
        if (adj.get(u).contains(v)) return;

        // Ajout symétrique (graphe non orienté)
        adj.get(u).add(v);
        adj.get(v).add(u);
        m++;
    }

    // =========================================================================
    // Accesseurs
    // =========================================================================

    /** @return le nombre de sommets n */
    public int getN() { return n; }

    /** @return le nombre d'arêtes m */
    public int getM() { return m; }

    /**
     * Retourne la liste des voisins de {@code v}.
     * La liste est une vue directe interne — ne pas la modifier.
     *
     * @param v indice du sommet (0 ≤ v < n)
     * @return liste des voisins
     */
    public List<Integer> getNeighbors(int v) { return adj.get(v); }

    /**
     * Retourne le degré du sommet {@code v} (nombre de voisins).
     *
     * @param v indice du sommet (0 ≤ v < n)
     * @return degré de v
     */
    public int getDegree(int v) { return adj.get(v).size(); }

    // =========================================================================
    // Chargement depuis fichier — format DIMACS
    // =========================================================================

    /**
     * Charge un graphe au format DIMACS (.col ou .clq).
     *
     * <p>Syntaxe :</p>
     * <pre>
     *   c  commentaire
     *   p edge &lt;n&gt; &lt;m&gt;   — déclaration du graphe (sommets numérotés à partir de 1)
     *   e &lt;u&gt; &lt;v&gt;         — arête entre u et v
     * </pre>
     *
     * @param f chemin vers le fichier
     * @return graphe chargé
     * @throws IOException en cas d'erreur de lecture
     */
    public static Graph loadDimacs(String f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        Graph g = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("c")) continue;                          // commentaire
            if (line.startsWith("p")) {                                  // en-tête
                String[] parts = line.split("\\s+");
                g = new Graph(Integer.parseInt(parts[2]));
            }
            if (line.startsWith("e") && g != null) {                    // arête
                String[] parts = line.split("\\s+");
                // DIMACS numérote à partir de 1 → on décrémente
                g.addEdge(Integer.parseInt(parts[1]) - 1,
                          Integer.parseInt(parts[2]) - 1);
            }
        }
        br.close();
        return g;
    }

    // =========================================================================
    // Chargement depuis fichier — format SNAP
    // =========================================================================

    /**
     * Charge un graphe au format SNAP (Stanford).
     *
     * <p>Chaque ligne non commentée (#) contient deux entiers séparés par
     * une ou plusieurs espaces. Les sommets peuvent commencer à 0 ou 1,
     * et peuvent ne pas être contigus — on prend {@code max_id + 1} comme taille.</p>
     *
     * @param f chemin vers le fichier
     * @return graphe chargé
     * @throws IOException en cas d'erreur de lecture
     */
    public static Graph loadSnap(String f) throws IOException {
        // Première passe : trouver l'identifiant max pour allouer le graphe
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        int u = Integer.parseInt(parts[0]);
                        int v = Integer.parseInt(parts[1]);
                        if (u > max) max = u;
                        if (v > max) max = v;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Deuxième passe : ajouter les arêtes
        Graph g = new Graph(max + 1);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        g.addEdge(Integer.parseInt(parts[0]),
                                  Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return g;
    }

    // =========================================================================
    // Chargement depuis fichier — format KONECT
    // =========================================================================

    /**
     * Charge un graphe au format KONECT (Koblenz).
     *
     * <p>Similaire à SNAP mais les commentaires commencent par '%'
     * et les sommets sont numérotés à partir de 1.</p>
     *
     * @param f chemin vers le fichier
     * @return graphe chargé
     * @throws IOException en cas d'erreur de lecture
     */
    public static Graph loadKonect(String f) throws IOException {
        // Première passe : trouver le max pour dimensionner le graphe
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        int u = Integer.parseInt(parts[0]);
                        int v = Integer.parseInt(parts[1]);
                        if (u > max) max = u;
                        if (v > max) max = v;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Deuxième passe : remplissage (sommets 1-indexés → on décrémente)
        Graph g = new Graph(max);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        g.addEdge(Integer.parseInt(parts[0]) - 1,
                                  Integer.parseInt(parts[1]) - 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return g;
    }

    // =========================================================================
    // Chargement automatique (détection du format par extension)
    // =========================================================================

    /**
     * Charge un graphe en détectant automatiquement le format d'après l'extension.
     *
     * <ul>
     *   <li>.col, .clq → DIMACS</li>
     *   <li>.tsv, .konect → KONECT</li>
     *   <li>tout autre → SNAP (par défaut)</li>
     * </ul>
     *
     * @param filename chemin vers le fichier
     * @return graphe chargé
     * @throws IOException en cas d'erreur de lecture
     */
    public static Graph loadAuto(String filename) throws IOException {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".col") || lower.endsWith(".clq")) {
            return loadDimacs(filename);
        }
        if (lower.endsWith(".tsv") || lower.endsWith(".konect")) {
            return loadKonect(filename);
        }
        // Par défaut : SNAP
        return loadSnap(filename);
    }

    // =========================================================================
    // Génération de graphe aléatoire (modèle Erdős–Rényi G(n, p))
    // =========================================================================

    /**
     * Génère un graphe aléatoire selon le modèle Erdős–Rényi G(n, p).
     *
     * <p>Chaque arête potentielle {i, j} (i &lt; j) est ajoutée
     * indépendamment avec probabilité {@code p}.</p>
     *
     * @param n    nombre de sommets
     * @param p    probabilité d'arête (0 ≤ p ≤ 1)
     * @param seed graine pour la reproductibilité des résultats
     * @return graphe aléatoire
     */
    public static Graph randomGraph(int n, double p, long seed) {
        Random rng = new Random(seed);
        Graph g = new Graph(n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (rng.nextDouble() < p) {
                    g.addEdge(i, j);
                }
            }
        }
        return g;
    }
}
