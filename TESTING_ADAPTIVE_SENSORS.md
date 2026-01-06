# 🧪 Guide de Test des Capteurs Adaptatifs

Ce guide explique comment tester les **3 catégories de capteurs** nouvellement ajoutées à l'application de lecture de manga.

## 📋 Prérequis

### Appareils compatibles
- **Capteurs de mouvement** : Tous les appareils Android modernes (accéléromètre + gyroscope)
- **Capteurs de lumière (Luxmètre)** : La plupart des appareils Android modernes
- **Capteurs de position** : Tous les appareils Android (accéléromètre + magnétomètre)
- **LiDAR/ToF** : Appareils haut de gamme uniquement (optionnel)

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
   - L'écran de lecture s'ouvre avec tous les capteurs activés

---

## 📱 Test 1 : Capteurs de Mouvement - Navigation par Geste

### Objectif
Tester la navigation entre les pages en inclinant l'appareil.

### Étapes de Test

#### Test 1.1 : Navigation vers la Page Suivante
1. **Ouvrez un chapitre** avec plusieurs pages
2. **Notez le numéro de page actuel** (affiché dans la barre supérieure)
3. **Inclinez légèrement l'appareil vers la GAUCHE**
   - L'appareil doit être en mode portrait
   - Inclinez d'environ 15-30° vers la gauche
   - Maintenez l'inclinaison pendant 1 seconde
4. **Résultat attendu** :
   - ✅ La page suivante devrait s'afficher automatiquement
   - ✅ Le numéro de page dans la barre supérieure devrait augmenter
   - ✅ Une animation de transition devrait être visible

#### Test 1.2 : Navigation vers la Page Précédente
1. **Assurez-vous d'être sur la page 2 ou plus**
2. **Inclinez légèrement l'appareil vers la DROITE**
   - Inclinez d'environ 15-30° vers la droite
   - Maintenez l'inclinaison pendant 1 seconde
3. **Résultat attendu** :
   - ✅ La page précédente devrait s'afficher automatiquement
   - ✅ Le numéro de page devrait diminuer

#### Test 1.3 : Détection de l'Orientation
1. **Tenez l'appareil en mode portrait** (vertical)
   - Vérifiez que la barre supérieure affiche "Portrait"
2. **Tournez l'appareil en mode paysage** (horizontal)
   - Vérifiez que l'affichage change pour "Landscape"
3. **Retournez l'appareil** (upside down)
   - Vérifiez que l'orientation est détectée

#### Test 1.4 : Afficher les Informations Détaillées
1. **Cliquez sur l'icône 📊** dans la barre supérieure
2. **Vérifiez les informations affichées** :
   - Orientation
   - Accélération (m/s²)
   - Rotation (rad/s)
   - Statut de vibration
   - Mouvement horizontal

### ⚠️ Dépannage - Navigation par Geste

**Le geste ne fonctionne pas :**
- ✅ **Inclinez plus fortement** l'appareil (30-45°)
- ✅ **Attendez 1 seconde** entre chaque mouvement (cooldown intégré)
- ✅ **Vérifiez que vous êtes sur une page** (pas en chargement)
- ✅ **Assurez-vous que l'appareil est stable** avant d'incliner

**Navigation trop sensible :**
- Le système a un cooldown de 300ms pour éviter les changements trop rapides
- Si c'est encore trop sensible, vous pouvez ajuster le seuil dans `MotionSensorManager.kt`

---

## ☀️ Test 2 : Capteurs Environnementaux - Adaptation Automatique à la Lumière

### Objectif
Tester l'adaptation automatique du thème et de la luminosité selon la lumière ambiante.

### Étapes de Test

#### Test 2.1 : Mode Nuit (Obscurité)
1. **Allez dans une pièce sombre** (chambre, salle de bain sans lumière)
   - Ou couvrez le capteur de lumière avec votre main
2. **Ouvrez l'écran de lecture**
3. **Résultat attendu** :
   - ✅ Le thème devrait passer en **"🌙 Mode Nuit"**
   - ✅ Les couleurs devraient être **noir/or foncé**
   - ✅ La luminosité devrait être **réduite** (~15%)
   - ✅ Un **filtre de lumière bleue** devrait être actif (écran plus chaud/jaunâtre)
   - ✅ La barre supérieure devrait afficher "🌙 Mode Nuit"

#### Test 2.2 : Mode Normal (Lumière Tamisée)
1. **Allez dans une pièce normalement éclairée** (intérieur, lumière artificielle)
2. **Ouvrez l'écran de lecture**
3. **Résultat attendu** :
   - ✅ Le thème devrait être en **"💡 Mode Normal"**
   - ✅ Les couleurs devraient être le thème standard (noir/crimson)
   - ✅ La luminosité devrait être **modérée** (~50%)
   - ✅ Pas de filtre de lumière bleue

#### Test 2.3 : Mode Contraste Élevé (Lumière Forte)
1. **Allez à l'extérieur** en plein soleil
   - Ou placez l'appareil sous une lampe très lumineuse
2. **Ouvrez l'écran de lecture**
3. **Résultat attendu** :
   - ✅ Le thème devrait passer en **"☀️ Contraste Élevé"**
   - ✅ Les couleurs devraient être **noir/blanc pur** (contraste maximum)
   - ✅ La luminosité devrait être **augmentée** (~90%)
   - ✅ Le texte devrait être plus lisible en plein soleil

#### Test 2.4 : Transition Automatique entre Modes
1. **Commencez dans une pièce sombre** (Mode Nuit)
2. **Allumez progressivement la lumière**
3. **Résultat attendu** :
   - ✅ Le thème devrait passer automatiquement de "Mode Nuit" → "Mode Normal" → "Contraste Élevé"
   - ✅ La transition devrait être fluide (pas de clignotement)
   - ✅ La luminosité devrait s'ajuster automatiquement

#### Test 2.5 : Afficher les Informations du Capteur de Lumière
1. **Cliquez sur l'icône ☀️** dans la barre supérieure
2. **Vérifiez les informations affichées** :
   - Niveau de lumière (en lux)
   - Catégorie de lumière (DARK, DIM, NORMAL, BRIGHT, etc.)
   - Condition environnementale (Dark, Night, Bright)
   - Luminosité recommandée
   - Toggle pour activer/désactiver la luminosité automatique

#### Test 2.6 : Activer/Désactiver la Luminosité Automatique
1. **Ouvrez les informations du capteur de lumière** (icône ☀️)
2. **Activez le toggle "Auto Brightness"**
   - ⚠️ **Note** : Vous devrez peut-être accorder la permission `WRITE_SETTINGS` dans les paramètres Android
3. **Changez la lumière ambiante**
4. **Résultat attendu** :
   - ✅ La luminosité de l'écran devrait s'ajuster automatiquement
   - ✅ Si la permission n'est pas accordée, un message d'avertissement s'affichera

### ⚠️ Dépannage - Adaptation à la Lumière

**Le thème ne change pas :**
- ✅ **Attendez quelques secondes** : la détection peut prendre un moment
- ✅ **Changez la lumière plus significativement** : le système a des seuils pour éviter les changements trop fréquents
- ✅ **Vérifiez que le capteur de lumière est disponible** : certains émulateurs n'ont pas de capteur de lumière

**La luminosité automatique ne fonctionne pas :**
- ✅ **Accordez la permission** : Allez dans Paramètres > Apps > [Nom de l'app] > Permissions > Autoriser la modification des paramètres système
- ✅ **Vérifiez que le toggle est activé** dans les informations du capteur de lumière

**Le filtre de lumière bleue ne s'active pas :**
- ✅ Le filtre s'active uniquement en **Mode Nuit**
- ✅ Vérifiez que vous êtes bien dans une pièce très sombre
- ✅ Le filtre peut être subtil, regardez attentivement la teinte de l'écran

---

## 🧭 Test 3 : Capteurs de Position

### Objectif
Tester la détection de la position et de l'orientation de l'appareil.

### Étapes de Test

#### Test 3.1 : Détection de Position Portrait
1. **Tenez l'appareil en mode portrait** (vertical, normal)
2. **Ouvrez l'écran de lecture**
3. **Cliquez sur l'icône 🧭** dans la barre supérieure
4. **Vérifiez les informations** :
   - ✅ Position : "UPRIGHT" ou "VERTICAL FORWARD"
   - ✅ Rotation recommandée : "PORTRAIT"
   - ✅ Pitch : proche de 0° (appareil droit)
   - ✅ Roll : proche de 0°
   - ✅ Stable : "Yes" si l'appareil ne bouge pas

#### Test 3.2 : Détection de Position Paysage
1. **Tournez l'appareil en mode paysage** (horizontal)
2. **Vérifiez les informations** :
   - ✅ Position : "HORIZONTAL LEFT" ou "HORIZONTAL RIGHT"
   - ✅ Rotation recommandée : "LANDSCAPE LEFT" ou "LANDSCAPE RIGHT"
   - ✅ Pitch : proche de 0°
   - ✅ Roll : proche de ±90°

#### Test 3.3 : Détection d'Inclinaison
1. **Inclinez l'appareil** dans différentes directions
2. **Observez les valeurs en temps réel** :
   - ✅ Pitch : change quand vous inclinez vers l'avant/arrière
   - ✅ Roll : change quand vous inclinez vers la gauche/droite
   - ✅ Tilt Angle : augmente avec l'inclinaison
   - ✅ Position : change selon l'inclinaison (TILTED, etc.)

#### Test 3.4 : Détection de Stabilité
1. **Tenez l'appareil stable** (ne bougez pas)
2. **Vérifiez** :
   - ✅ Stable : "Yes"
3. **Bougez l'appareil légèrement**
4. **Vérifiez** :
   - ✅ Stable : "No" (pendant le mouvement)
   - ✅ Stable : "Yes" (quand vous arrêtez de bouger)

#### Test 3.5 : Position à Plat
1. **Posez l'appareil à plat** sur une table
2. **Vérifiez** :
   - ✅ Position : "FLAT"
   - ✅ Pitch et Roll : proches de 0°
   - ✅ Tilt Angle : proche de 0°

### ⚠️ Dépannage - Capteurs de Position

**Les valeurs ne changent pas :**
- ✅ **Attendez quelques secondes** : les valeurs sont lissées pour éviter les fluctuations
- ✅ **Bougez l'appareil plus significativement** : les petits mouvements peuvent être filtrés

**La position n'est pas détectée correctement :**
- ✅ **Calibrez le magnétomètre** : certains appareils nécessitent une calibration (faites un mouvement en 8 avec l'appareil)
- ✅ **Éloignez-vous des sources magnétiques** : aimants, métaux, etc.

---

## 🎯 Checklist de Test Complète

### Capteurs de Mouvement
- [ ] Navigation vers la page suivante (inclinaison gauche)
- [ ] Navigation vers la page précédente (inclinaison droite)
- [ ] Détection de l'orientation portrait
- [ ] Détection de l'orientation paysage
- [ ] Affichage des informations de mouvement (icône 📊)

### Capteurs Environnementaux (Lumière)
- [ ] Mode Nuit activé en obscurité (🌙)
- [ ] Mode Normal activé en lumière tamisée (💡)
- [ ] Mode Contraste Élevé activé en lumière forte (☀️)
- [ ] Transition automatique entre les modes
- [ ] Luminosité automatique fonctionne (avec permission)
- [ ] Filtre de lumière bleue actif en mode nuit
- [ ] Affichage des informations de lumière (icône ☀️)

### Capteurs de Position
- [ ] Détection de position portrait
- [ ] Détection de position paysage
- [ ] Détection d'inclinaison (pitch, roll)
- [ ] Détection de stabilité
- [ ] Détection de position à plat
- [ ] Affichage des informations de position (icône 🧭)

### Fonctionnalités Générales
- [ ] Tous les capteurs se chargent sans erreur
- [ ] L'application fonctionne même si certains capteurs ne sont pas disponibles
- [ ] Les transitions de thème sont fluides
- [ ] Aucune erreur dans les logs (Logcat)

---

## 📊 Vérification des Logs

Pour voir les logs des capteurs dans Android Studio :

1. **Ouvrez Logcat** (View → Tool Windows → Logcat)
2. **Filtrez par tag** :
   - `MotionSensorManager` : Logs des capteurs de mouvement
   - `LightSensorManager` : Logs du capteur de lumière
   - `PositionSensorManager` : Logs des capteurs de position
   - `BrightnessManager` : Logs de la gestion de luminosité
   - `BlueLightFilter` : Logs du filtre de lumière bleue
3. **Recherchez les messages** :
   - "Accelerometer started" / "Gyroscope started"
   - "Light sensor started"
   - "Position sensors started"
   - Messages d'erreur ou d'avertissement

---

## 💡 Conseils de Test

1. **Testez sur un appareil réel** : Les émulateurs Android ne simulent pas toujours les capteurs correctement, surtout le capteur de lumière
2. **Testez dans différentes conditions** : 
   - Lumière naturelle (extérieur)
   - Lumière artificielle (intérieur)
   - Obscurité (chambre)
3. **Testez progressivement** : Commencez par un type de capteur, puis testez les autres
4. **Vérifiez les permissions** : Certaines fonctionnalités (luminosité automatique) nécessitent des permissions
5. **Observez les transitions** : Les changements de thème et de luminosité devraient être fluides, pas brusques

---

## 🐛 Problèmes Connus et Solutions

### Le capteur de lumière ne fonctionne pas sur l'émulateur
- **Solution** : Testez sur un appareil réel. Les émulateurs Android n'ont généralement pas de capteur de lumière fonctionnel.

### La luminosité automatique nécessite une permission
- **Solution** : Allez dans Paramètres Android > Apps > [Nom de l'app] > Permissions > Autoriser la modification des paramètres système

### Les transitions de thème sont trop rapides/lentes
- **Solution** : Les seuils peuvent être ajustés dans `LightSensorManager.kt` et `AdaptiveTheme.kt`

### Le filtre de lumière bleue n'est pas visible
- **Solution** : Le filtre est subtil par design. Il est plus visible en mode nuit. Vérifiez que vous êtes bien en Mode Nuit (🌙).

---

## ✅ Test de Validation Final

Une fois tous les tests effectués, vérifiez que :

1. ✅ **Navigation par geste** fonctionne de manière fluide et intuitive
2. ✅ **Adaptation automatique à la lumière** fonctionne dans les 3 conditions (obscurité, intérieur, extérieur)
3. ✅ **Détection de position** est précise et réactive
4. ✅ **Aucune erreur** dans les logs
5. ✅ **Performance** : L'application reste fluide même avec tous les capteurs actifs

---

**Bon test ! 🚀**

