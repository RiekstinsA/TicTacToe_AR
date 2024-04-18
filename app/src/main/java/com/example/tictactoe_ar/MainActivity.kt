package com.example.tictactoe_ar

import com.example.tictactoe_ar.databinding.ActivityMainBinding
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import kotlin.random.nextInt

// definē MainActivity klasi, kas veido programmas galvenā ekrāna funkcijas
class MainActivity : AppCompatActivity() {

    // deklarē mainīgo, kur glabā galvenā ekrāna izkārtojumu un funkcijas
    lateinit var binding : ActivityMainBinding

    // ņem .XML izkārtojuma failu un pārvērš to objektos, ar kuriem programma var strādāt
    override fun onCreate(savedInstanceState: Bundle?) { // komanda strādā, kad tiek izveidots ekrāns
        super.onCreate(savedInstanceState) // nodrošina, ka viss nepieciešamais ekrāna darbībai ir iestatīts
        binding = ActivityMainBinding.inflate(layoutInflater) // komanda veic ielādi

        // izkārtojumu parāda ekrānā, lai lietotājs to varētu redzēt un izmantot
        setContentView(binding.root)

        // darbība klikšķinot uz pogas "Play Offline"
        binding.playOfflineBtn.setOnClickListener {
            createOfflineGame() // izveido "Offline" spēli
        }

        // darbība klikšķinot uz pogas "Create Online Game"
        binding.createOnlineGameBtn.setOnClickListener {
            createOnlineGame() // izveido "Online" spēli
        }

        // darbība klikšķinot uz pogas "Join Online Game"
        binding.joinOnlineGameBtn.setOnClickListener {
            joinOnlineGame() // pievienojas jau aktīvai "Online" spēlei
        }

    }


    fun createOfflineGame(){ // funkcija "Offline" spēlei
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED // spēles modelis, kas norāda ka lietotājs pievienojies spēlei
            )
        )
        startGame() // komanda aizved uz pašas spēles ekrānu
    }

    fun createOnlineGame(){ // funkcija "Online" spēles izveidošanai
        GameData.myID = "X" // iestata pašreizējo spēlētāju par "X"
        GameData.saveGameModel( // saglabā spēles modeli ar statusu "CREATED" un unikālu spēles ID
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId = Random.nextInt(1000..9999).toString() // randomizē spēles ID no 1000 līdz 9999
            )
        )
        startGame() // komanda aizved uz pašas spēles ekrānu
    }

    fun joinOnlineGame(){ // funkcija "Online" spēles pievienošanās
        var gameId = binding.gameIdInput.text.toString() // iegūst spēles ID no lietotāja ievades
        if(gameId.isEmpty()){
            binding.gameIdInput.setError("Please enter game ID") // apstrādā kļūdainu ievadi
            return
        }
        GameData.myID = "O" // iestata pašreizējo spēlētāju kā "O"
        Firebase.firestore.collection("games") // iegūst spēles modeli no Firestore izmantojot spēles ID
            .document(gameId)
            .get()
            .addOnSuccessListener {
                val model = it?.toObject(GameModel::class.java) // ņem ievades datus no Firestore
                if(model==null){ // pārbauda vai dati ir tukši
                    binding.gameIdInput.setError("Please enter valid game ID") // ja tukši, izvada paziņojumu
                }else{
                    model.gameStatus = GameStatus.JOINED // izveido spēlei jaunu statusu "JOINED"
                    GameData.saveGameModel(model) // saglabā jauno spēles modeli
                    startGame() // ved uz spēles ekrānu
                }
            }

    }

    fun startGame(){ // funkcija, kas aizved lietotāju uz spēles ekrānu
        startActivity(Intent(this,GameActivity::class.java))
    }

}