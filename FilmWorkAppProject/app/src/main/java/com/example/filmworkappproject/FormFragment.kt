package com.example.filmworkappproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.example.filmworkappproject.data.FilmRepository
import com.example.filmworkappproject.data.InMemoryFilmRepository
import com.example.filmworkappproject.databinding.FragmentFormBinding
import com.example.filmworkappproject.model.Film
import com.example.filmworkappproject.model.FormType

private const val FORM_TYPE = "type"

class FormFragment : Fragment() {
    private lateinit var  binding: FragmentFormBinding //per accedere agli elementi
    private lateinit var formType: FormType //se è New o in Edit
    private lateinit var repository: FilmRepository //l'oggetto con tutti i film
    private lateinit var film: Film //film aperto

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent> //intent di selezione immagine
    private var selectedImageUri: Uri? = null //uri dell'immagine presa dalla galleria


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(FORM_TYPE, FormType::class.java)
            } else{
                it.getSerializable(FORM_TYPE) as? FormType
            } ?: FormType.New
        }
        repository = InMemoryFilmRepository
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentFormBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val formType = formType

        // quando scegli l'immagine salva l'uri e la mostra
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.EditImage.setImageURI(uri)
                }
            }
        }

        //bottone salvataggio
        with(binding.saveButton) {
            text = when(formType) { //il testo sul bottone
                is FormType.Edit -> "save"
                FormType.New -> "add"
            }
            setOnClickListener {
                save(formType)
            }
        }

        //imposta il film
        film = when (formType) {
            //se è Edit
            is FormType.Edit -> {
                (formType as FormType.Edit).film.also {
                    // imposta i dati del film nel form
                    binding.titleField.setText(it.name)
                    binding.editTextDate.setText(it.releaseDate)
                    val categoryArray = resources.getStringArray(R.array.category_list)
                    val position = categoryArray.indexOf(it.category)
                    binding.categorySelector.setSelection(position)
                    binding.checkBoxVisto.isChecked = it.seen
                    binding.commentInputBox.setText(it.comment ?: "")
                    binding.commentInputBox.isEnabled = it.seen
                    //immagine
                    if (it.imageUri != null) {
                        binding.EditImage.setImageURI(it.imageUri.toUri())
                    } else {
                        binding.EditImage.setImageResource(it.icon)
                    }
                    //se è visto blocca tutto
                    if (it.seen) {
                        binding.titleField.isEnabled = false
                        binding.editTextDate.isEnabled = false
                        binding.categorySelector.isEnabled = false
                        binding.checkBoxVisto.isEnabled = false
                        binding.commentInputBox.isEnabled = false
                        binding.EditImage.isEnabled = false

                    }
                }
            }
            // se è un nuovo film
            FormType.New -> {
                film = Film("", R.drawable.defaultphoto) //creo film
                binding.EditImage.setImageResource(film.icon) //setto immagine default
                film
            }
        }

        // quando clicki sull'immagine apre la galleria
        binding.EditImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK) //intent di tipo pick per selezionare un oggetto
            intent.type = "image/*" //tipo di file che cerca
            imagePickerLauncher.launch(intent) //lancia l'intent aprendo la galleria
        }

        // se il film non è visto
        if (!film.seen) {
            //listener quando viene clickato
            binding.checkBoxVisto.setOnCheckedChangeListener { _, isChecked ->
                binding.commentInputBox.isEnabled = isChecked
                if (!isChecked) binding.commentInputBox.setText("") //se non è checkato cancella commento
            }
        }
    }

    //funzione quando premi il bottone save
    private fun save(formType: FormType){
        //controlli dei valori

        val title = binding.titleField.text.toString().trim()
        val yearStr = binding.editTextDate.text.toString().trim()

        // Controllo titolo vuoto
        if (title.isEmpty()) {
            binding.titleField.error = "title can't be null"
            return
        }

        // Controllo anno valido
        val year: Int
        try {
            year = yearStr.toInt()
        } catch (e: NumberFormatException) {
            binding.editTextDate.error = "wrong format, just put a year (es. 2025)"
            return
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (year > currentYear + 2) {
            binding.editTextDate.error = "can't put a year over: ${currentYear + 2}"
            return
        }
        if (year < 1895) {
            binding.editTextDate.error = "year can't be under 1895 (the invention of the first film)"
            return
        }

        // obbligatory comment
        if (binding.checkBoxVisto.isChecked && binding.commentInputBox.text.toString().isBlank()) {
            binding.commentInputBox.error = "you have to put a comment if you have seen it"
            return
        }

        //salvataggio in un oggetto
        film = film.copy(
            name = binding.titleField.text.toString(),
            releaseDate = binding.editTextDate.text.toString(),
            category = binding.categorySelector.selectedItem.toString(),
            seen = binding.checkBoxVisto.isChecked,
            comment = if (binding.checkBoxVisto.isChecked) binding.commentInputBox.text.toString() else null,
            imageUri = selectedImageUri?.toString() ?: film.imageUri
        )

        when (formType) {
            //se edit chiama la funzione che lo aggiorna
            is FormType.Edit -> repository.edit(formType.film, film)
            //se new chiama quello che ne aggiunge uno nuovo
            FormType.New -> repository.addFilm(film)
        }

        // chiude il fragment
        findNavController().popBackStack()
    }
}