package com.example.rssnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rssnews.databinding.ActivityNewsListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import kotlinx.coroutines.*

class NewsListActivity : AppCompatActivity() {
    // View binding for accessing UI elements
    private lateinit var binding: ActivityNewsListBinding
    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth
    // Firebase Realtime Database instance
    private lateinit var database: FirebaseDatabase

    private val newsAdapter = NewsAdapter(
        onItemClick = { article -> openArticle(article) },
        onFavoriteClick = { article -> toggleFavorite(article) }
    )
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    // Flag, true se sei sulla pagina dei preferiti
    private var isShowingFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = Firebase.auth
        database = Firebase.database("https://rssnews2-8d49f-default-rtdb.europe-west1.firebasedatabase.app/")

        //chiama funzioni setup
        setupRecyclerView()
        setupBottomNavigation()
        setupLogoutButton()
        loadNews()
    }

    private fun setupRecyclerView() {
        binding.newsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NewsListActivity)
            adapter = newsAdapter
        }
    }

    //barra per cambiare tra tutti a solo i preferiti
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_all_news -> {
                    isShowingFavorites = false
                    loadNews()
                    true
                }
                R.id.navigation_favorites -> {
                    isShowingFavorites = true //mette a true
                    loadFavorites() //ricarica le news
                    true
                }
                else -> false
            }
        }
    }

    //bottone per fare logout
    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    //prende gli articoli dal sito
    private fun loadNews() {
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_kraj.htm")
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(url.openStream(), "UTF-8")

                val articles = mutableListOf<Article>()
                var currentArticle: Article? = null
                var eventType = parser.eventType

                // prende tutti gli articoli
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "item" -> currentArticle = Article()
                                "title" -> {
                                    val title = parser.nextText()
                                    currentArticle?.title = if (title.length > 100) {
                                        title.substring(0, 97) + "..."
                                    } else {
                                        title
                                    }
                                }
                                "description" -> {
                                    val description = parser.nextText()
                                    // Clean HTML tags and entities from description
                                    val cleanDescription = description
                                        .replace(Regex("<[^>]*>"), "")
                                        .replace("&nbsp;", " ")
                                        .replace("&amp;", "&")
                                        .replace("&lt;", "<")
                                        .replace("&gt;", ">")
                                        .replace("&quot;", "\"")
                                        .replace("&#39;", "'")
                                        .trim()
                                    
                                    currentArticle?.description = if (cleanDescription.length > 150) {
                                        cleanDescription.substring(0, 147) + "..."
                                    } else {
                                        cleanDescription
                                    }
                                }
                                "link" -> {
                                    currentArticle?.link = parser.nextText()
                                    currentArticle?.id = currentArticle?.link?.hashCode()?.toString() ?: ""
                                }
                                "enclosure" -> {
                                    val imageUrl = parser.getAttributeValue(null, "url")
                                    currentArticle?.imageUrl = imageUrl
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "item" && currentArticle != null) {
                                articles.add(currentArticle!!)
                                currentArticle = null
                            }
                        }
                    }
                    eventType = parser.next()
                }

                withContext(Dispatchers.Main) {
                    newsAdapter.submitList(articles) // manda gli articoli all'adapter che li stampa su schermo
                    checkReadStatus(articles)
                    checkFavoriteStatus(articles)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //controlla se è un preferito
    private fun checkFavoriteStatus(articles: List<Article>) {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/favorites")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteIds = snapshot.children.mapNotNull { it.key }.toSet()
                    articles.forEach { it.isFavorite = it.id in favoriteIds }
                    newsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //manda all'adapter solo i preferiti
    private fun loadFavorites() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/favorites")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteIds = snapshot.children.mapNotNull { it.key }.toSet()
                    val currentList = newsAdapter.currentList
                    val favoriteArticles = currentList.filter { it.id in favoriteIds }
                    newsAdapter.submitList(favoriteArticles) //manda i preferiti
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //controlla se è letto
    private fun checkReadStatus(articles: List<Article>) {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/read")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val readIds = snapshot.children.mapNotNull { it.key }.toSet()
                    articles.forEach { it.isRead = it.id in readIds }
                    newsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //apre un articolo e lo segna come letto
    private fun openArticle(article: Article) {
        // Mark as read in Firebase
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/read/${article.id}")
            .setValue(true)

        // Open in browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
        startActivity(intent)
    }

    //funzione per la pressione del cuore
    //toglie o aggiunge ai preferiti
    private fun toggleFavorite(article: Article) {
        val userId = auth.currentUser?.uid ?: return
        val isFavorite = article.isFavorite
        val reference = database.getReference("users/$userId/favorites/${article.id}")

        if (isFavorite) {
            reference.removeValue()
        } else {
            reference.setValue(true)
        }

        article.isFavorite = !isFavorite
        newsAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() //spegnere l'asincronia
    }
}

data class Article(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var link: String = "",
    var imageUrl: String? = null,
    var isRead: Boolean = false,
    var isFavorite: Boolean = false
) 