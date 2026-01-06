# 🧪 Guide de Test des Capteurs

Ce guide explique comment tester les fonctionnalités de capteurs (mouvement et LiDAR) dans l'application de lecture de manga.

## 📋 Prérequis

### Appareils compatibles
- **Capteurs de mouvement** : Tous les appareils Android modernes (accéléromètre + gyroscope)
- **LiDAR/ToF** : Appareils haut de gamme uniquement (ex: Samsung Galaxy S20+, Google Pixel, etc.)

### Vérifier la disponibilité des capteurs
Les capteurs sont détectés automatiquement au démarrage. Si un capteur n'est pas disponible, l'application continuera de fonctionner sans lui.

## 🚀 Étapes de Test

### 1. Compiler et Installer l'Application

```bash
# Sur Windows (PowerShell)
.\gradlew.bat assembleDebug

# Installer sur l'appareil connecté
.\gradlew.bat installDebug
```

Ou utilisez Android Studio :
- Cliquez sur **Run** (▶️) ou appuyez sur `Shift + F10`
- Sélectionnez votre appareil/émulateur
- L'application sera compilée et installée automatiquement

### 2. Naviguer vers l'Écran de Lecture

1. **Ouvrir l'application**
2. **Naviguer vers un manga** :
   - Accédez à l'onglet **Home**, **Search**, ou **Explore**
   - Sélectionnez un manga
3. **Ouvrir les détails** :
   - Cliquez sur un manga pour voir ses détails
4. **Lancer la lecture** :
   - Cliquez sur le bouton **"📖 Read Manga"**
   - Sélectionnez un chapitre
   - L'écran de lecture s'ouvre avec les capteurs activés

### 3. Tester les Capteurs de Mouvement

#### Test d'Orientation
1. **Tenez l'appareil en mode portrait** (vertical)
   - Vérifiez que la barre supérieure affiche "Portrait"
2. **Tournez l'appareil en mode paysage** (horizontal)
   - Vérifiez que l'affichage change pour "Landscape"
3. **Retournez l'appareil** (upside down)
   - Vérifiez que l'orientation est détectée

#### Test de Navigation par Mouvement
1. **Inclinez l'appareil vers la gauche**
   - La page suivante devrait s'afficher automatiquement
   - Attendez 1 seconde entre chaque mouvement
2. **Inclinez l'appareil vers la droite**
   - La page précédente devrait s'afficher
3. **Testez plusieurs fois** pour vérifier la réactivité

#### Test de Détection de Vibration
1. **Secouez légèrement l'appareil**
   - Une carte devrait apparaître en bas avec "📳 Device vibrating"
2. **Arrêtez de secouer**
   - La carte devrait disparaître

#### Test de Chute Libre
1. **Lancez l'appareil en l'air** (faites attention !)
   - Un avertissement rouge devrait apparaître : "⚠️ Free fall detected!"
2. **Rattrapez l'appareil**
   - L'avertissement devrait disparaître

#### Afficher les Informations Détaillées
1. **Cliquez sur l'icône 📊** dans la barre supérieure
2. **Vérifiez les informations affichées** :
   - Orientation
   - Accélération (m/s²)
   - Rotation (rad/s)
   - Statut de vibration
   - Mouvement horizontal

### 4. Tester le LiDAR/ToF

#### Vérifier la Disponibilité
1. **Regardez la barre supérieure**
   - Si le LiDAR est disponible, vous verrez l'icône **🔦**
   - Si non disponible, l'icône n'apparaîtra pas

#### Afficher les Informations LiDAR
1. **Cliquez sur l'icône 🔦** dans la barre supérieure
2. **Une carte devrait apparaître** avec :
   - Statut actif/inactif (point vert si actif)
   - Distance mesurée (en mètres et centimètres)
   - Distance moyenne
   - Plage de mesure (min/max)
   - Précision du capteur

#### Test de Mesure de Distance
1. **Pointez l'appareil vers un objet proche** (20-50 cm)
   - La distance devrait être affichée en temps réel
2. **Éloignez l'objet**
   - La distance devrait augmenter
3. **Rapprochez l'objet**
   - La distance devrait diminuer

#### Note sur les Appareils sans LiDAR
- Si votre appareil n'a pas de capteur LiDAR/ToF, la fonctionnalité ne sera pas disponible
- L'application fonctionnera normalement sans le LiDAR
- Les capteurs de mouvement fonctionneront toujours

### 5. Test de Navigation Alternative

Même si les capteurs ne fonctionnent pas, vous pouvez toujours naviguer :
- **Tap gauche de l'écran** : Page précédente
- **Tap droit de l'écran** : Page suivante
- **Scroll vertical** : Navigation normale dans la liste

## 🔍 Vérification des Logs

Pour voir les logs des capteurs dans Android Studio :

1. **Ouvrez Logcat** (View → Tool Windows → Logcat)
2. **Filtrez par tag** :
   - `MotionSensorManager` : Logs des capteurs de mouvement
   - `LidarSensorManager` : Logs du LiDAR
3. **Recherchez les messages** :
   - "Accelerometer started" / "Gyroscope started"
   - "LiDAR sensor started" / "No ToF sensor found"
   - Messages d'erreur ou d'avertissement

## ⚠️ Dépannage

### Les capteurs ne fonctionnent pas
1. **Vérifiez que l'appareil a les capteurs** :
   - Accéléromètre : présent sur tous les appareils modernes
   - Gyroscope : présent sur la plupart des appareils
   - LiDAR : seulement sur certains appareils haut de gamme

2. **Vérifiez les permissions** :
   - Les capteurs de mouvement ne nécessitent pas de permissions
   - Le LiDAR utilise le capteur de proximité (pas de permission requise)

3. **Redémarrez l'application** :
   - Fermez complètement l'application
   - Rouvrez-la et naviguez vers l'écran de lecture

### L'orientation ne change pas
- **Attendez quelques secondes** : la détection peut prendre un moment
- **Bougez l'appareil plus lentement** : les mouvements rapides peuvent être ignorés

### La navigation par mouvement ne fonctionne pas
- **Inclinez plus fortement** l'appareil
- **Attendez 1 seconde** entre chaque mouvement (cooldown)
- **Vérifiez que vous êtes sur une page** (pas en chargement)

### Le LiDAR ne mesure pas
- **Votre appareil n'a peut-être pas de capteur LiDAR/ToF**
- **Vérifiez la distance** : le capteur a une plage limitée (généralement 0.01m - 5m)
- **Assurez-vous que l'objet est bien éclairé** : certains capteurs nécessitent de la lumière

## 📱 Appareils Recommandés pour Tester

### Capteurs de Mouvement (tous les appareils)
- ✅ Tous les appareils Android modernes

### LiDAR/ToF (appareils spécifiques)
- Samsung Galaxy S20+ et plus récents
- Google Pixel 4 et plus récents
- Certains appareils OnePlus haut de gamme
- Appareils Apple (iPhone 12 Pro et plus récents)

## 🎯 Checklist de Test

- [ ] Application compile et s'installe correctement
- [ ] Navigation vers l'écran de lecture fonctionne
- [ ] Orientation portrait détectée
- [ ] Orientation paysage détectée
- [ ] Navigation par inclinaison gauche (page suivante)
- [ ] Navigation par inclinaison droite (page précédente)
- [ ] Détection de vibration fonctionne
- [ ] Informations des capteurs s'affichent (icône 📊)
- [ ] LiDAR détecté (si disponible)
- [ ] Mesure de distance LiDAR fonctionne (si disponible)
- [ ] Informations LiDAR s'affichent (icône 🔦)
- [ ] Navigation tactile fonctionne toujours
- [ ] Aucune erreur dans les logs

## 💡 Conseils

1. **Testez sur un appareil réel** : Les émulateurs Android ne simulent pas toujours les capteurs correctement
2. **Testez dans différentes conditions** : Lumière, position, etc.
3. **Vérifiez les logs** : Ils donnent des informations utiles sur le fonctionnement des capteurs
4. **Testez progressivement** : Commencez par les capteurs de mouvement, puis testez le LiDAR si disponible

