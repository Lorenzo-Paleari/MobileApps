package com.example.project2.ui.view

import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.project2.R
import com.example.project2.data.FormRepository

@Composable
fun HomeScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToForm: () -> Unit,
    onEditEntry: (Int) -> Unit
) {
    val context = LocalContext.current
    val entries = remember { FormRepository.getEntries() } //prende tutte le entry

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.welcome),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //bottone per andare alla galleria, da cui poi vai alla modifica foto
            FilledTonalButton(onClick = onNavigateToGallery) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.modify_photo))
            }
            //bottone pagina creare new entry
            FilledTonalButton(onClick = onNavigateToForm) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.new_entry))
            }
        }

        //elenco di entry
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(entries) { index, entry ->
                ElevatedCard(
                    //premendo su uno ti apre l'edit
                    onClick = { onEditEntry(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        //testo
                        Text(
                            text = entry.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        //immagine
                        Image(
                            painter = rememberAsyncImagePainter(entry.imageUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        //audio
                        entry.audioUri?.let { audioUri ->
                            val mediaPlayer = remember { MediaPlayer() }
                            val isPlaying = remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    try {
                                        if (isPlaying.value) {
                                            mediaPlayer.stop()
                                            mediaPlayer.reset()
                                            isPlaying.value = false
                                        } else {
                                            mediaPlayer.reset()
                                            mediaPlayer.setDataSource(context, audioUri)
                                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                                            mediaPlayer.prepare()
                                            mediaPlayer.start()
                                            isPlaying.value = true
                                            mediaPlayer.setOnCompletionListener {
                                                isPlaying.value = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, context.getString(R.string.cannot_play_audio), Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlaying.value) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                                Text(stringResource(R.string.audio_attached))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        //posizione
                        Text(
                            text = stringResource(R.string.town_label, entry.townName),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
