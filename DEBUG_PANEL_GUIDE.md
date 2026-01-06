# 🐛 Guide du Panneau de Débogage

## Vue d'ensemble

Le panneau de débogage intégré vous permet de voir tous les logs de l'application directement dans l'interface utilisateur, sans avoir besoin d'accéder à Logcat.

## Comment accéder au panneau de débogage

1. **Ouvrez l'application** et naviguez vers un chapitre de manga
2. **Dans la barre supérieure**, cherchez l'icône **🔍** (loupe)
3. **Cliquez sur l'icône** pour ouvrir le panneau de débogage
4. L'icône changera en **🐛** (insecte) lorsque le panneau est ouvert

## Fonctionnalités du panneau

### Affichage des messages

Le panneau affiche :
- **Horodatage** : Heure à laquelle le message a été généré (format HH:mm:ss)
- **Tag** : Source du message (ex: `ChapterReader`, `MotionSensor`, `LidarSensor`, etc.)
- **Message** : Le contenu du message de débogage
- **Niveau** : Couleur selon le niveau (DEBUG, INFO, WARNING, ERROR)

### Niveaux de messages

- **DEBUG** (gris) : Messages informatifs pour le débogage détaillé
- **INFO** (bleu) : Messages informatifs normaux
- **WARNING** (orange) : Avertissements (ex: capteur non disponible)
- **ERROR** (rouge) : Erreurs (ex: échec de démarrage d'un capteur)

### Boutons

- **Clear** : Efface tous les messages du panneau
- **Close** : Ferme le panneau de débogage

## Messages que vous verrez

### Initialisation des capteurs

```
[HH:mm:ss] [ChapterReader] Initializing motion sensors...
[HH:mm:ss] [ChapterReader] Motion sensors available: true
[HH:mm:ss] [ChapterReader] Motion sensors started successfully
```

### Interactions utilisateur

```
[HH:mm:ss] [ChapterReader] Debug panel toggle clicked
[HH:mm:ss] [ChapterReader] Sensor info toggle clicked
[HH:mm:ss] [ChapterReader] Tap detected at x=500, screenWidth=1080, index=0
[HH:mm:ss] [ChapterReader] Navigating to next page: 1
```

### Mises à jour des capteurs

```
[HH:mm:ss] [MotionSensor] State updated: PORTRAIT
[HH:mm:ss] [LidarSensor] Distance: 25.5cm
[HH:mm:ss] [LightSensor] Light level: 150.0 lux
```

### Erreurs

```
[HH:mm:ss] [ChapterReader] Error starting motion sensors: [message d'erreur]
[HH:mm:ss] [ChapterReader] Motion sensors not available on this device
```

## Comment utiliser pour déboguer

### 1. Vérifier l'initialisation des capteurs

1. Ouvrez le panneau de débogage
2. Regardez les messages d'initialisation
3. Vérifiez que vous voyez :
   - `Initializing motion sensors...`
   - `Motion sensors available: true/false`
   - `Motion sensors started successfully` (si disponible)

### 2. Tester les interactions

1. Cliquez sur les boutons de la barre supérieure
2. Dans le panneau, vous devriez voir : `[Tag] toggle clicked`
3. Si vous ne voyez pas ces messages, le bouton ne reçoit pas les clics

### 3. Tester la navigation tactile

1. Tapez sur l'écran (gauche ou droite)
2. Dans le panneau, vous devriez voir :
   - `Tap detected at x=..., screenWidth=..., index=...`
   - `Navigating to previous/next page: ...`
3. Si vous ne voyez pas ces messages, les gestes tactiles ne sont pas détectés

### 4. Vérifier les capteurs

1. Ouvrez le panneau de débogage
2. Bougez le téléphone ou changez l'éclairage
3. Vous devriez voir des messages de mise à jour des capteurs :
   - `[MotionSensor] State updated: ...`
   - `[LidarSensor] Distance: ...`
   - `[LightSensor] Light level: ...`

### 5. Identifier les erreurs

1. Ouvrez le panneau de débogage
2. Cherchez les messages en rouge (ERROR)
3. Lisez le message d'erreur pour comprendre le problème

## Limites

- **Maximum 100 messages** : Le panneau garde les 100 derniers messages en mémoire
- **Affichage des 50 derniers** : Seuls les 50 derniers messages sont affichés dans le panneau
- **Mise à jour toutes les 500ms** : Le panneau se met à jour automatiquement toutes les 500ms
- **Pas de persistance** : Les messages sont perdus lorsque vous fermez l'application

## Conseils

1. **Gardez le panneau ouvert** pendant que vous testez pour voir tous les messages en temps réel
2. **Utilisez "Clear"** régulièrement pour éviter d'avoir trop de messages
3. **Filtrez visuellement** : Les messages d'erreur sont en rouge, les avertissements en orange
4. **Vérifiez les horodatages** : Pour voir quand les événements se produisent

## Exemple de session de débogage

```
[14:30:15] [ChapterReader] Initializing motion sensors...
[14:30:15] [ChapterReader] Motion sensors available: true
[14:30:15] [ChapterReader] Motion sensors started successfully
[14:30:16] [ChapterReader] Initializing LiDAR sensor...
[14:30:16] [ChapterReader] LiDAR sensor not available on this device
[14:30:17] [ChapterReader] Light sensor available: true
[14:30:17] [ChapterReader] Light sensor started successfully
[14:30:20] [ChapterReader] Debug panel toggle clicked
[14:30:25] [ChapterReader] Sensor info toggle clicked
[14:30:30] [MotionSensor] State updated: PORTRAIT
[14:30:35] [ChapterReader] Tap detected at x=800, screenWidth=1080, index=0
[14:30:35] [ChapterReader] Navigating to next page: 1
```

## Résolution de problèmes

### Le panneau ne s'affiche pas

- Vérifiez que vous avez cliqué sur l'icône 🔍 dans la barre supérieure
- L'icône devrait changer en 🐛 lorsque le panneau est ouvert

### Aucun message n'apparaît

- Interagissez avec l'application (cliquez sur des boutons, tapez sur l'écran)
- Les messages apparaissent lorsque des événements se produisent

### Trop de messages

- Utilisez le bouton "Clear" pour effacer les messages
- Fermez et rouvrez le panneau si nécessaire

### Messages d'erreur

- Lisez le message d'erreur complet pour comprendre le problème
- Vérifiez que les permissions nécessaires sont accordées
- Vérifiez que votre appareil supporte les capteurs requis

