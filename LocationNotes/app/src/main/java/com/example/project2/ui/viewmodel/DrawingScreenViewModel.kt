package com.example.project2.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.model.Drawing
import com.example.project2.model.ToolSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DrawingScreenViewModel : ViewModel() {
    val availableColors = listOf(Color.Black, Color.Green, Color.Blue, Color.Red)
    val drawings = MutableStateFlow(emptyList<Drawing>())
    val image = MutableStateFlow<Bitmap?>(null)

    val currentToolSettings = MutableStateFlow(ToolSettings())

    fun setToolColor(newColor: Color) {
        currentToolSettings.update { it.copy(color = newColor) }
    }

    fun setToolSize(newSize: Float) {
        currentToolSettings.update { it.copy(size = newSize) }
    }

    fun undoDrawing() {
        drawings.update { prev -> prev.dropLast(1) }
    }

    fun clearDrawing() {
        drawings.update { emptyList() }
    }

    fun addPath(drawing: Drawing) {
        drawings.update { prev -> prev + drawing }
    }

    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                image.update { bitmap }
            }
        }
    }

    fun save(context: Context, onSaved: () -> Unit) {
        viewModelScope.launch {
            val baseImage = image.value ?: return@launch
            val newBitmap = Bitmap.createBitmap(baseImage.width, baseImage.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)

            withContext(Dispatchers.Default) {
                canvas.drawBitmap(baseImage, 0f, 0f, android.graphics.Paint())

                drawings.value.forEach { drawing ->
                    val paint = android.graphics.Paint().apply {
                        color = drawing.settings.color.toArgb()
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = drawing.settings.size
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                    }
                    canvas.drawPath(drawing.path.asAndroidPath(), paint)
                }
            }

            withContext(Dispatchers.IO) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "edited_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val imageUri = context.contentResolver.insert(contentUri, contentValues) ?: return@withContext
                context.contentResolver.openOutputStream(imageUri)?.use {
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(imageUri, contentValues, null, null)
            }
            onSaved()
        }
    }
}
