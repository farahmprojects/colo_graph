# Projet Coloration de Graphes
## Guide de compilation et d'utilisation

---

## Structure du projet

```
coloration_graphe/
├── src/
│   └── graph/
│       ├── Graph.java          # Structure de graphe + chargement fichiers
│       ├── ColorResult.java    # Résultat de coloration (couleurs, ni, ρ)
│       ├── Dsatur.java         # DSATUR naïf   O(n²) temps / O(n²) mémoire
│       ├── DsaturOpt.java      # DSATUR opt.   O((n+m)·log n) / O(n+m)  [bonus 5b]
│       ├── Greedy.java         # Glouton naturel + Welsh-Powell            [bonus 5a]
│       ├── Main.java           # Point d'entrée principal
│       └── BenchmarkLarge.java # Benchmark grands graphes
├── bin/                        # Bytecode compilé (créé par javac)
└── README.md
```

---

## Étape 1 — Compilation

Depuis la racine du projet :

```bash
# Créer le répertoire de sortie
mkdir -p bin

# Compiler tous les fichiers Java en une seule commande
javac -d bin src/graph/*.java
```

En cas de succès, aucun message n'est affiché et le dossier `bin/` est rempli.

**Sur Windows** (cmd), remplacer `/` par `\` et `*.java` par une liste explicite :
```bat
mkdir bin
javac -d bin src\graph\Graph.java src\graph\ColorResult.java src\graph\Dsatur.java src\graph\DsaturOpt.java src\graph\Greedy.java src\graph\Main.java src\graph\BenchmarkLarge.java
```

---

## Étape 2 — Exécution

### Mode démo (graphe exemple + graphes aléatoires)
```bash
java -cp bin graph.Main
```

### Un fichier, format détecté automatiquement
```bash
java -cp bin graph.Main monGraphe.col            # DIMACS (.col ou .clq)
java -cp bin graph.Main monGraphe.txt            # SNAP   (défaut)
java -cp bin graph.Main monGraphe.tsv            # KONECT (.tsv ou .konect)
```

### Forcer le format
```bash
java -cp bin graph.Main monGraphe.txt dimacs
java -cp bin graph.Main monGraphe.txt snap
java -cp bin graph.Main monGraphe.txt konect
```

### Mode batch (plusieurs fichiers)
```bash
java -cp bin graph.Main --batch gc.col peter.col queen11.col
```

### Benchmark grands graphes (bonus 5b)
```bash
java -cp bin graph.BenchmarkLarge
```

### Ajuster la mémoire JVM pour les très grands graphes
```bash
java -Xmx4g -cp bin graph.Main monGrandGraphe.txt
```

---

## Étape 3 — Lecture des résultats

Le tableau de sortie contient 4 colonnes :

| Colonne | Signification |
|---------|---------------|
| `χD`    | Nombre de couleurs utilisées par l'algorithme |
| `ni[χD]`| Nombre de sommets colorés quand la dernière couleur a été introduite |
| `ρ`     | Ratio = ni[χD] / n (de 0 à 1) |
| `ms`    | Temps d'exécution en millisecondes |

---

## Formats de fichiers supportés

### DIMACS (`.col`, `.clq`)
```
c commentaire
p edge 9 14
e 1 2
e 1 3
...
```
> Sommets numérotés à partir de **1**

### SNAP (`.txt`, `.edges`)
```
# commentaire
0 1
0 2
1 3
...
```
> Sommets numérotés à partir de **0** (ou 1 selon le dataset)

### KONECT (`.tsv`, `.konect`)
```
% commentaire
1 2
1 3
2 4
...
```
> Sommets numérotés à partir de **1**

---

## Sources de graphes de test

| Source | URL | Format |
|--------|-----|--------|
| DIMACS | https://mat.tepper.cmu.edu/COLOR/instances.html | DIMACS |
| SNAP   | https://snap.stanford.edu/data/index.html | SNAP |
| KONECT | http://konect.cc | KONECT |

---

## Description des algorithmes

### DSATUR naïf (`Dsatur.java`)
- **Principe** : à chaque étape, choisit le sommet de DSAT (degré de saturation) maximum
- **Temps** : O(n²) — scan linéaire à chaque étape
- **Mémoire** : O(n²) — tableau `boolean[n][n+2]`
- **Usage** : graphes jusqu'à ~2 000 sommets

### DSATUR optimisé (`DsaturOpt.java`) — *Bonus 5b*
- **Optimisation 1** : `HashSet<Integer>` par sommet → mémoire O(n+m) au lieu de O(n²)
- **Optimisation 2** : `PriorityQueue` + lazy deletion → O((n+m)·log n) au lieu de O(n²)
- **Résultat identique** à la version naïve (même critère de sélection)
- **Usage** : graphes jusqu'à 1 000 000 de sommets

### Glouton ordre naturel (`Greedy.naturalOrder`) — *Bonus 5a*
- Visite les sommets dans l'ordre 0, 1, …, n-1
- Temps O(n+m), mémoire O(n)
- Qualité généralement inférieure à DSATUR

### Glouton Welsh-Powell (`Greedy.welshPowell`) — *Bonus 5a*
- Trie d'abord les sommets par degré décroissant
- Meilleure qualité que l'ordre naturel, toujours plus rapide que DSATUR
- Temps O(n log n + m), mémoire O(n)
