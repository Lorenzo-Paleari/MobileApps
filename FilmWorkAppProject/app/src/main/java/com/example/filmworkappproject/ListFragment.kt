package com.example.filmworkappproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmworkappproject.adapters.FilmListAdapter
import com.example.filmworkappproject.data.FilmRepository
import com.example.filmworkappproject.data.InMemoryFilmRepository
import com.example.filmworkappproject.databinding.FragmentListBinding
import com.example.filmworkappproject.model.Film
import com.example.filmworkappproject.model.FormType

class ListFragment : Fragment(){
    lateinit var binding: FragmentListBinding //per accedere agli elementi
    lateinit var filmAdapter: FilmListAdapter //classe adapter

    val filmRepository: FilmRepository = InMemoryFilmRepository //istanza dell'oggetto con tutti i film

    //mostra la view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentListBinding.inflate(inflater,container,false) //trasforma in oggetti
            .also{binding = it} //salva il binding
            .root //ritorna la view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        filmAdapter = FilmListAdapter(
            //quando clicki su un film:
            onClick = { film ->
                findNavController().navigate( //porta alla schermata edit
                    R.id.action_listFragment_to_formFragment,
                    bundleOf("type" to FormType.Edit(film)) //e gli mando il film e la modalità edit
                )
            },
            //quando tieni premuto su un film:
            onLongPress = { film ->
                showDeleteConfirmationDialog(film) //parte la funzione per eliminare il film
            }
        )

        binding.FilmList.apply { //applica alla lista dei film
            adapter = filmAdapter // prende i dati dei film da qui
            layoutManager = LinearLayoutManager(context) //organizza in colonna
        }

        // quando premi il pulsante + ti porta alla pagina per aggiungere un nuovo film
        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_formFragment, bundleOf("type" to FormType.New))
        }

        //quando cambi la categoria ricarica la lista
        binding.filterSelecter.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {} //non fa nulla se non selezioni altro
        })

        //se premi sulla checkbox aggiorna la lista
        binding.filtroVisti.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }

        applyFilters() //aggiorna la lista alla fine della creazione
    }

    // aggiorna
    private fun applyFilters() {
        val allFilms = filmRepository.getFilmList() //prende film
        val selectedCategory = binding.filterSelecter.selectedItem.toString() // la categoria scelta
        val onlySeen = binding.filtroVisti.isChecked //se la checkbox è true è true

        // i film filtrati
        val filtered = allFilms.filter { film ->
            //mette a true i film di quella categoria
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Film" -> film.category.equals("Film", ignoreCase = true)
                "Series" -> film.category.equals("Series", ignoreCase = true)
                "Documentary" -> film.category.equals("Documentary", ignoreCase = true)
                else -> false
            }

            //mette a true i film visti se la checkbox è segnata
            val matchesSeen = if (onlySeen) film.seen else true

            //unisce i due filtri tenendo solo quelli che rispettano entrambi
            matchesCategory && matchesSeen
        }

        //ordina i film per releaseDate
        val sorted = filtered.sortedBy { film ->
            film.releaseDate.toIntOrNull() ?: 0
        }

        // manda all'adapter i film
        filmAdapter.filmList = sorted

        //aggiorna numero di film
        binding.itemsNumberDisplay.text = "items: ${sorted.size}"
    }


    //quando passi da un fragment ad un altro
    private fun onChangeDst(
        navController: NavController,
        destination: NavDestination,
        bundle: Bundle?
    ){
        println(destination)
        if(destination.id == R.id.listFragment){
            filmAdapter.filmList = filmRepository.getFilmList()
        }
    }

    //quando il fragment parte aggiunge listener
    override fun onStart(){
        super.onStart()
        findNavController().addOnDestinationChangedListener(::onChangeDst)
    }

    //rimuove listener e chiama stop
    fun onStrop(){
        findNavController().removeOnDestinationChangedListener(::onChangeDst)
        super.onStop()
    }

    // quando tieni premuto per eliminare un film
    private fun showDeleteConfirmationDialog(film: Film) {
        //crea finestra
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Movie")
        builder.setMessage("Are you sure you want to delete '${film.name}'?")
        builder.setPositiveButton("Yes") { _, _ ->
            filmRepository.remove(film)  //se premi si rimuove dalla lista
            applyFilters() // e aggiorna la view
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }
}