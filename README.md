# Movie Backend

Projet de référence pour le cours de Développement FullStack Polytech Nancy

## Migration

Pour la gestion de la migration vous pouvez choisir entre Flyway et Liquibase. Les 2 implémentations sont configurés sur ce projet.
Le but est le même pour les 2 implémentations, pouvoir gérer la migration de la DB mais selon 2 vues différentes. 
L'une en pure SQL pour un dialect donnée (Flyway) et l'autre en faisant abstraction du dialect et en ayant une vision descriptive (Liquibase).

### Liquibase workflow
Après avoir défini une entity ou un changement sur une entity existante :
1. Générer le fichier de diff `db/liquibase-changelog/db.changelog-generated.xml` entre la DB actuelle et les entités JPA (nécessite `liquibase.properties` configuré, notamment `referenceUrl` pointant vers le package des entités)
```bash
mvn liquibase:diff
```
2. Créer votre fichier de migration `db/liquibase-changelog/db.changelog-<custom>.xml` et récupérer le/les changeset du fichier de diff
3. Adapter le naming des index, contraintes, etc. pour respecter la convention de nommage du projet (cf. ci-dessous)
4. Ajouter le/les rollback au changeset (optionnel)
5. Référencer le nouveau fichier de changelog dans `db/liquibase-changelog/db.changelog-master.xml`
6. Jouer l'update
```bash
mvn liquibase:update
```
7. Tester le rollback si déclaré
```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

### Flyway workflow
1. Créer un nouveau fichier de migration SQL - convention de nommage `V{version}__{description}.sql` (⚠️ double underscore avant la description : un fichier mal nommé, ex. `V1_xxx.sql`, est silencieusement ignoré par Flyway sans erreur)
2. Jouer l'update
```bash
mvn process-resources flyway:migrate -Dflyway.configFiles=flyway.conf
```
3. Vérifier l'état des migrations appliquées
```bash
mvn flyway:info -Dflyway.configFiles=flyway.conf
```

> ⚠️ Une migration déjà appliquée (sur n'importe quel environnement) ne doit **jamais** être modifiée : Flyway calcule un checksum par fichier et rejettera toute divergence. Pour corriger une erreur, créez une nouvelle migration.
>
> ℹ️ Contrairement à Liquibase, Flyway (édition Community) ne propose pas de rollback automatique : toute annulation doit être écrite manuellement dans une nouvelle migration `V{version+1}`.

> ℹ️ Seul un des deux outils doit être actif à la fois. C'est piloté par `spring.flyway.enabled` / `spring.liquibase.enabled` dans `application.yaml` (Flyway est actif par défaut sur ce projet).

### Convention de nommage

Les tables et colonnes suivent le `snake_case` (standard SQL). Pour éviter toute ambiguïté entre le nom de la table et celui de la colonne dans les noms de contraintes/index (ex. `movie_id` sur `library_entries`), la partie colonne est écrite en `camelCase` :

- primary keys: `PK_{table}`
- foreign keys: `FK_{table}_{columnInCamelCase}`
- unique keys: `UX_{table}_{columnInCamelCase}`
- indexes: `IX_{table}_{columnInCamelCase}`

Exemples : `PK_library_entries`, `FK_library_entries_movieId`, `UX_movies_externalId`.
