package com.example.project2.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.example.project2.R
import com.example.project2.model.FormEntry
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@Composable
fun FormScreen(
    onSave: (FormEntry) -> Unit,
    existingEntry: FormEntry? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf(existingEntry?.text ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(existingEntry?.imageUri) }
    var audioUri by remember { mutableStateOf<Uri?>(existingEntry?.audioUri) }

    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }

    //controlla il permesso, se c'è chiama la funzione per registrare
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startRecording(context) { uri, mediaRecorder ->
                audioUri = uri
                recorder = mediaRecorder
                isRecording = true
            }
        } else {
            Toast.makeText(context, context.getString(R.string.microphone_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //titolo
        Text(
            text = if (existingEntry != null) context.getString(R.string.edit_entry) else context.getString(R.string.new_entry),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        //dove inserisci il testo
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(context.getString(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        //bottone per selezionare immagine
        ElevatedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(context.getString(R.string.select_image))
        }

        //immagine (se inserita)
        imageUri?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(context.getString(R.string.audio), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        //tasto per registrare audio
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = {
                    //se stava registrando ferma
                    if (isRecording) {
                        try {
                            recorder?.stop()
                        } catch (e: Exception) {
                            Log.e("AudioRecord", "stop() failed", e)
                        } finally {
                            recorder?.release()
                            recorder = null
                            isRecording = false
                        }
                    } else {
                        //chiama la funzione di registrazione
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRecording) context.getString(R.string.stop) else context.getString(R.string.record))
            }

            if (audioUri != null) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        //save button
        Button(
            onClick = {
                scope.launch {
                    val townName = if (ContextCompat.checkSelfPermission( //controllo permesso
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getTownName(context) //se hai il permesso chiama funzione
                    } else {
                        context.getString(R.string.permission_denied)
                    }

                    //chiama la funzione del main per salvare l'oggetto
                    onSave(FormEntry(text, imageUri!!, townName, audioUri))
                }
            },
            enabled = imageUri != null && text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (existingEntry != null) context.getString(R.string.update_entry) else context.getString(R.string.save_entry))
        }
    }
}

//funzione per registrare
fun startRecording(context: Context, onStart: (Uri, MediaRecorder) -> Unit) {
    val fileName = "voice_note_${System.currentTimeMillis()}.mp4"
    val file = File(context.cacheDir, fileName)
    val recorder = MediaRecorder()

    try {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        Log.d("AudioRecord", "Recording started: ${file.absolutePath}")
        onStart(file.toUri(), recorder)

    } catch (e: Exception) {
        Log.e("AudioRecord", "Recording failed", e)
        Toast.makeText(context, "Registrazione fallita: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        recorder.release()
    }
}

suspend fun getTownName(context: Context): String {
    return withContext(Dispatchers.IO) {
        //servizio gps
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            //prende posizione
            val location = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
            } catch (e: SecurityException) {
                null
            }

            //se null
            if (location == null) return@withContext "Unknown"

            //indirizzo
            val geocoder = Geocoder(context, Locale.getDefault())
            //nome della citta
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.locality ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
