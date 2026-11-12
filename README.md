# P2 DFS BEA Java

Starter code Java du projet P2 OpenClassrooms : application console de suivi des dépenses.

## Contraintes techniques

- Java 25
- Spring Boot 4.0.0, utilisé uniquement pour démarrer l'application console
- Maven
- Persistance locale dans `src/main/resources/emma_expenses.csv`
- Aucun endpoint REST, aucune interface web, aucune base de données et aucun ORM
- Montants représentés avec `BigDecimal`
- Dates représentées avec `LocalDate` au format `dd/MM/yyyy`

## Prérequis

- JDK 25 configuré dans `JAVA_HOME`
- Maven 3.8 ou version ultérieure

## Démarrer le projet

Depuis le dossier `p2-dfs-bea-java` :

```bash
mvn clean package
mvn spring-boot:run
```

Le fichier CSV est créé avec l'en-tête `date,category,description,amount` s'il est absent ou vide.
L'application accepte les montants saisis avec un point ou une virgule décimale.

Pour lancer le fichier JAR construit :

```bash
java -jar target/p2-dfs-bea-java-0.1.0-SNAPSHOT.jar
```

## Fonctionnalités du starter

- Affichage de l'historique, trié du plus récent au plus ancien
- Ajout d'une dépense dans `emma_expenses.csv`
- Création automatique du fichier et de son en-tête
- Gestion des dates et montants invalides sans stack trace utilisateur
- Gestion d'un historique absent ou vide
- Jeu de données d'exemple dans `src/main/resources/emma_expenses.csv`

## Fonctionnalités à implémenter par l'étudiant

- Total global et statistiques par catégorie
- Nombre de mois distincts et moyennes mensuelles
- Simulation d'achat d'une voiture
- Simulation de prêt immobilier avec limite d'endettement à 35 %
- Tests unitaires et tests ciblés des nouvelles fonctionnalités

Les tests préécrits des fonctionnalités avancées ne sont volontairement pas inclus dans ce starter afin que le projet compile avant leur implémentation.

## Organisation du code

```text
src/main/java/com/openclassrooms/p2dfsbea/
├── P2DfsBeaApplication.java  # point d'entrée Spring Boot
├── model/Expense.java         # modèle fortement typé
├── repository/ExpenseRepository.java
├── service/ExpenseService.java
└── ui/ConsoleMenu.java
```

Le projet reste volontairement limité à une application console pédagogique.

