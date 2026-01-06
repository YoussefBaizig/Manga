# 🔧 Comment Accorder la Permission de Luminosité Automatique

Ce guide explique comment accorder la permission `WRITE_SETTINGS` nécessaire pour que la fonctionnalité de luminosité automatique fonctionne.

## 📱 Méthode 1 : Via l'Application (Recommandé)

### Étapes :

1. **Ouvrez l'écran de lecture** d'un chapitre de manga
2. **Cliquez sur l'icône ☀️** dans la barre supérieure pour afficher les informations du capteur de lumière
3. **Activez le toggle "Auto Brightness"**
4. **Si la permission n'est pas accordée**, une carte d'avertissement apparaîtra avec un bouton **"Open Settings"**
5. **Cliquez sur "Open Settings"**
6. **Dans les paramètres Android** :
   - Cherchez l'option **"Modify system settings"** ou **"Modifier les paramètres système"**
   - Activez le toggle pour autoriser l'application
7. **Retournez à l'application**
8. **Désactivez puis réactivez** le toggle "Auto Brightness" pour vérifier que la permission est accordée

## 📱 Méthode 2 : Via les Paramètres Android Manuellement

### Étapes :

1. **Ouvrez les Paramètres Android** sur votre appareil
2. **Allez dans "Apps"** ou **"Applications"**
3. **Trouvez votre application** (ex: "MangaVerse" ou le nom de votre app)
4. **Cliquez sur l'application**
5. **Allez dans "Permissions"** ou **"Permissions"**
6. **Cherchez "Modify system settings"** ou **"Modifier les paramètres système"**
7. **Activez le toggle** pour autoriser

### Chemin complet (selon la version Android) :

**Android 6.0+ (Marshmallow et plus récent) :**
```
Paramètres → Apps → [Nom de l'app] → Permissions → Modifier les paramètres système
```

**Android 11+ :**
```
Paramètres → Apps → [Nom de l'app] → Permissions avancées → Modifier les paramètres système
```

**Certains appareils Samsung/OnePlus :**
```
Paramètres → Apps → [Nom de l'app] → Autorisations spéciales → Modifier les paramètres système
```

## 🔍 Vérifier que la Permission est Accordée

Après avoir accordé la permission :

1. **Retournez à l'application**
2. **Ouvrez les informations du capteur de lumière** (icône ☀️)
3. **Activez le toggle "Auto Brightness"**
4. **Vérifiez** :
   - ✅ Le toggle devrait rester activé (pas grisé)
   - ✅ Le message d'avertissement devrait disparaître
   - ✅ La luminosité devrait commencer à s'ajuster automatiquement selon la lumière ambiante

## ⚠️ Notes Importantes

### Pourquoi cette permission est nécessaire ?

La permission `WRITE_SETTINGS` est nécessaire pour que l'application puisse modifier la luminosité de l'écran automatiquement. C'est une permission système spéciale qui nécessite une autorisation explicite de l'utilisateur pour des raisons de sécurité.

### Sécurité

- Cette permission permet uniquement de modifier la luminosité de l'écran
- L'application ne peut pas modifier d'autres paramètres système
- Vous pouvez révoquer cette permission à tout moment depuis les paramètres Android

### Compatibilité

- **Android 6.0+ (API 23+)** : Permission requise
- **Android 5.1 et inférieur** : Permission accordée automatiquement

## 🐛 Dépannage

### Le bouton "Open Settings" ne fonctionne pas

1. **Essayez la méthode manuelle** (Méthode 2)
2. **Vérifiez que les paramètres Android sont accessibles**
3. **Redémarrez l'application** après avoir accordé la permission

### La permission est accordée mais ne fonctionne pas

1. **Désactivez puis réactivez** le toggle "Auto Brightness"
2. **Vérifiez que le capteur de lumière fonctionne** (icône ☀️ devrait être visible)
3. **Changez la lumière ambiante** pour tester l'ajustement automatique
4. **Vérifiez les logs** dans Android Studio (Logcat) pour voir les messages d'erreur

### Le toggle reste grisé

- Cela signifie que la permission n'est pas encore accordée
- Suivez les étapes de la Méthode 1 ou 2 pour accorder la permission

## 📊 Test de la Fonctionnalité

Une fois la permission accordée, testez la luminosité automatique :

1. **Activez "Auto Brightness"**
2. **Allez dans une pièce sombre** (Mode Nuit)
   - La luminosité devrait diminuer automatiquement (~15%)
3. **Allez dans une pièce normalement éclairée** (Mode Normal)
   - La luminosité devrait s'ajuster (~50%)
4. **Allez à l'extérieur** ou sous une lumière forte (Mode Contraste Élevé)
   - La luminosité devrait augmenter (~90%)

## ✅ Checklist

- [ ] Permission `WRITE_SETTINGS` accordée dans les paramètres Android
- [ ] Toggle "Auto Brightness" activé dans l'application
- [ ] Message d'avertissement disparu
- [ ] Luminosité s'ajuste automatiquement selon la lumière ambiante
- [ ] Mode Nuit : Luminosité réduite (~15%)
- [ ] Mode Normal : Luminosité modérée (~50%)
- [ ] Mode Contraste Élevé : Luminosité élevée (~90%)

---

**Note** : Si vous avez des problèmes, vérifiez les logs dans Android Studio (Logcat) avec le filtre `BrightnessManager` pour voir les messages de débogage.

