## Structure de code
dossier lib: contient les dépendance ainsi le ficher exporté pour créer la base de données.
dossier src: contient les classes java

## Etapes d'execution
Premierment, éxecutez-vous la classe mainServer.java.

Deuxiement, éxecutez-vous la classe ChatClient.java autant fois que vous voulez instancier un client.

Lorsque l'interface de client se déclenche, vous pouvez soit vous connectez (utilisant des valeurs à partir de la base de données) ou s'inscrire.

Après le login, vous pouvez ajouter des amis et consulter la liste des amis.

Lorsque vous consultez la liste des amis, vous cochez les amis que vous voulez discuter avec ou bien ceux que vous voulez supprimer de la liste des amis.

Au cas de choisir les amis à discuter, il faut déjà ouvrir des instances de ces amis là avant de les choisir (pour que le server connait leurs ports)

## Base de donnée
Vous trouverez le ficher exporté de la base de donné dans le dossier lib


