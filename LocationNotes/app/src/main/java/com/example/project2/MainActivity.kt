package com.example.project2

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project2.data.FormRepository
import com.example.project2.ui.theme.Project2Theme
import com.example.project2.ui.view.DrawingScreen
import com.example.project2.ui.view.FormScreen
import com.example.project2.ui.view.GalleryScreen
import com.example.project2.ui.view.HomeScreen
import com.example.project2.ui.view.LoginScreen
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        // entry iniziale
        val defaultEntry = FormEntry(
            text = "Sample entry",
            imageUri = Uri.parse("content://media/picker_get_content/0/com.android.providers.media.photopicker/media/31"),
            townName = "Sample Town",
            audioUri = null
        )
        FormRepository.addEntry(defaultEntry)
         */

        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        val navController = rememberNavController()
                        NavHost(
                            navController,
                            startDestination = Destionation.Login.path //da dove inizia
                        ) {
                            //login
                            composable(Destionation.Login.path) {
                                LoginScreen (
                                    //viene chiamata quando la password è giusta
                                    onLoginSuccess = {
                                        navController.navigate(Destionation.Home.path) {
                                            popUpTo(0)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            //home
                            composable(Destionation.Home.path) {
                                HomeScreen(
                                    //funzioni che vengono chiamate dai pulsanti della view
                                    onNavigateToGallery = {
                                        navController.navigate(Destionation.Gallery.path) //ti porta sotto (path che apre la galleria)
                                    },
                                    onNavigateToForm = {
                                        navController.navigate(Destionation.Form.path)
                                    },
                                    onEditEntry = { index ->
                                        navController.navigate("/form/edit/$index")
                                    }
                                )
                            }
                            //Modifica di una entry esistente
                            composable(Destionation.EditForm.path) {
                                val index = it.arguments?.getString("index")?.toIntOrNull()
                                val entry = index?.let { FormRepository.getEntry(it) }

                                FormScreen(
                                    existingEntry = entry,
                                    onSave = { updated ->
                                        if (index != null) {
                                            //aggiorna l'oggetto
                                            FormRepository.updateEntry(index, updated)
                                        }
                                        navController.navigate(Destionation.Home.path) {
                                            popUpTo(0)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            //galleria
                            composable(Destionation.Gallery.path) {
                                GalleryScreen(
                                    //funzione che definisce cosa fare quando viene premuta un immagine
                                    onSelectedImage = {
                                        navController.navigate(Destionation.Drawing(it).builtPath())
                                    }
                                )
                            }
                            //modifica foto
                            composable(Destionation.Drawing.path) {
                                val uri = it.arguments?.getString("uri")?.let { URLDecoder.decode(it, "utf-8").toUri() }
                                DrawingScreen(image = uri, navController = navController)
                            }
                            //form: pagina per inserire una nuova entry
                            composable(Destionation.Form.path) { //route
                                FormScreen(                     //UI
                                    onSave = { entry ->        //funzione che puo essere chiamata dalla UI
                                        //prende la entry mandata dalla UI e la salva come repository
                                        FormRepository.addEntry(entry)
                                        //poi torna alla home
                                        navController.navigate(Destionation.Home.path) {
                                            popUpTo(0) //cancella stack di navigazione
                                            launchSingleTop = true //evita duplicati
                                        }
                                    }
                                )
                            }

                        }

                    }
                }
            }
        }
    }
}

//invece di scrivere /home/... uso Destination.Home.path e si occupa sta classe di
sealed class Destionation(val path: String){
    //definisce percorsi
    data object Login : Destionation("/login")
    data object Home : Destionation("/home")
    data object Gallery : Destionation("/gallery")
    data object Form : Destionation("/form")
    data class EditForm(val index: Int) : Destionation("/form/edit/$index") {
        companion object {
            const val path = "/form/edit/{index}"
        }
    }
    //quello del disegno è modificato in base alla foto selezionata
    data class Drawing(val uri: Uri) : Destionation(path){
        fun builtPath() : String{
            return "/drawing/${URLEncoder.encode(uri.toString(), "utf-8")}"
        }
        companion object{
            val path = "/drawing/{uri}"
        }
    }

}