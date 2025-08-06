package com.example.project2.ui.view

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project2.Destionation
import com.example.project2.model.Drawing
import com.example.project2.ui.viewmodel.DrawingScreenViewModel

@Composable
fun DrawingScreen(
    viewModel: DrawingScreenViewModel = viewModel(),
    image: Uri?,
    navController: NavController
){
    val cxt = LocalContext.current
    //CARICA IMMAGINE
    LaunchedEffect(Unit){
        image?.let {
            viewModel.loadImage(image, cxt)
        }
    }
    //ELEMENTI IN COLONNA
    Column {
        val drawings by viewModel.drawings.collectAsStateWithLifecycle()
        val toolSettings by viewModel.currentToolSettings.collectAsStateWithLifecycle()
        var currentDrawing by remember { mutableStateOf<Drawing?>(null) }
        val image by viewModel.image.collectAsStateWithLifecycle()

        //SALVA IL DISEGNO
        Button(
            onClick = {
                viewModel.save(cxt) {
                    navController.navigate(Destionation.Home.path) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }
        ) {
            Text("save")
        }
        //Area di disegno
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(toolSettings) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentDrawing = Drawing(
                                path = Path().apply { moveTo(offset.x, offset.y) },
                                settings = toolSettings
                            )
                        },
                        onDrag = { change, offset ->
                            currentDrawing?.let { drawing ->
                                val newPath = drawing.path.copy().apply { lineTo(change.position.x, change.position.y) }
                                currentDrawing = drawing.copy(path = newPath)
                            }
                        },
                        onDragEnd = {
                            currentDrawing?.let { drawing ->
                                viewModel.addPath(drawing)
                            }
                            currentDrawing = null
                        },
                        onDragCancel = {
                            currentDrawing = null
                        }
                    )
                }
        ) {
            //immagine base
            image?.let { image: Bitmap ->
                drawImage(image.asImageBitmap())
            }
            drawings.forEach { //disegni gia fatti
                drawPath(
                    path = it.path,
                    color = it.settings.color,
                    style = Stroke(it.settings.size, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            currentDrawing?.let { //sto disegnando
                drawPath(
                    path = it.path,
                    color = it.settings.color,
                    style = Stroke(it.settings.size, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        //casella strumenti pittura
        ToolBox(
            viewModel.availableColors,
            toolSettings.color,
            viewModel::setToolColor,
            toolSettings.size,
            viewModel::setToolSize,
            viewModel::undoDrawing,
            viewModel::clearDrawing
        )
    }
}

@Composable
fun ToolBox(
    availableColors: List<Color>,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    currentSize: Float,
    onSizeSelected: (Float) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit
){
    //una colonna con 2 righe
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        //selezione colore
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("color: ")
            //per ogni colore mette il bottone
            availableColors.forEach {
                IconButton(onClick = { onColorSelected(it) }) {
                    Icon(
                        imageVector = if (currentColor == it) Icons.Default.Star else Icons.Default.Circle,
                        contentDescription = null,
                        tint = it
                    )
                }
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
            }
            //bottone torna indietro
            IconButton(onClick = onUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = null
                )
            }
            //bottone cancella tutto
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
        //dimensione strumento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("size ${currentSize.toInt()}")
            Slider(
                value = currentSize,
                onValueChange = onSizeSelected,
                valueRange = 1f..30f,
                steps = 14
            )
        }
    }

}