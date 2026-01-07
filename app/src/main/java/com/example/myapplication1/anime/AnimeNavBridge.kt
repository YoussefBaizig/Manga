package com.example.myapplication1.anime

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication1.MainActivity
import com.example.myapplication1.MangaBottomBar
import com.example.myapplication1.ui.navigation.Screen
import com.example.myapplication1.ui.theme.MangaAppTheme

object AnimeNavBridge {
    @JvmStatic
    fun setBottomNavigation(composeView: ComposeView) {
        composeView.setContent {
            MangaAppTheme(darkTheme = true) {
                // On récupère le contexte actuel (qui est AnimeMainActivity)
                val context = LocalContext.current

                MangaBottomBar(
                    // On force la route actuelle à être "anime" pour allumer l'icône
                    currentRoute = Screen.Anime.route,
                    onNavigate = { route ->
                        // Si la route cliquée n'est pas "anime", on navigue
                        if (route != Screen.Anime.route) {

                            // 1. Créer l'intent pour retourner à MainActivity
                            val intent = Intent(context, MainActivity::class.java)

                            // 2. Flags importants :
                            // CLEAR_TOP : Si MainActivity existe déjà en dessous, on revient dessus sans la recréer
                            // SINGLE_TOP : On ne crée pas une nouvelle instance par dessus
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                            // (Optionnel) Si tu veux atterrir sur une page précise (ex: Search),
                            // tu devras passer un extra ici et le gérer dans MainActivity.
                            // intent.putExtra("nav_route", route)

                            // 3. Lancer l'activité
                            context.startActivity(intent)

                            // 4. Fermer AnimeMainActivity pour ne pas qu'elle reste dans la pile
                            if (context is Activity) {
                                context.finish()
                                // Optionnel : désactiver l'animation de transition pour que ce soit fluide
                                context.overridePendingTransition(0, 0)
                            }
                        }
                    }
                )
            }
        }
    }
}