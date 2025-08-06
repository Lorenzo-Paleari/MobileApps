package com.example.project2.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.project2.ui.viewmodel.GalleryScreenViewModel

//permesso da richiedere
private val PERMISSION_NAME = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE
}

@Composable
fun GalleryScreen(
    viewModel: GalleryScreenViewModel = viewModel(),
    onSelectedImage : (Uri) -> Unit
){
    val ctx = LocalContext.current
    //ricordo se ha il permesso
    var hasPermission by remember { mutableStateOf(ctx.checkSelfPermission(PERMISSION_NAME) == PackageManager.PERMISSION_GRANTED) }

    //chiedo il permesso
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        hasPermission = it
    }

    if (hasPermission){
        //carica immagini
        val images by viewModel.images.collectAsStateWithLifecycle()
        LaunchedEffect(Unit){
            viewModel.loadImages(ctx)
        }
        //passa all'altro composable per mostrare le immagini
        GalleryView(images, onSelectedImage)
    }else{
        //richiedi permesso
        LaunchedEffect(Unit){
            launcher.launch(PERMISSION_NAME)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("You don't have permissions")
            Button(onClick = { launcher.launch(PERMISSION_NAME) }) {
                Text("Request permission")
            }
        }
    }
}


@Composable
private fun GalleryView(
    images : List<Uri>, //lista immagini
    onSelect : (Uri) -> Unit //quando clicco su una foto
){
    if (images.isEmpty()){
        Text("No images found")
    }
    else{
        //griglia immagini galleria
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp)
        ) {
            //oggetti clickabili
            items(images, {it.toString()}) { uri ->
                Image(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(onClick = {onSelect(uri)}), //chiama la funzione passata
                                            // in questo caso ho passato la funzione per disegnare
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "null",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}