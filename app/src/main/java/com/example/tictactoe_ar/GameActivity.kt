package com.example.tictactoe_ar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tictactoe_ar.databinding.ActivityGameBinding
import com.example.tictactoe_ar.databinding.ActivityMainBinding

//definē GameActivity klasi, kas atbild par pašas spēles ekrānu
class GameActivity : AppCompatActivity(),View.OnClickListener {

    // deklarē izkārtojuma mainīgo
    lateinit var binding: ActivityGameBinding

    // deklarē mainīgo, kas saturēs spēles modeli
    private var gameModel : GameModel? = null

    // funkcija darbosies,kad tiks izveidots ekrāns
    override fun onCreate(savedInstanceState: Bundle?) {

        // nodrošina ka iestatīts viss nepieciešamais ekrāna darbībai
        super.onCreate(savedInstanceState)

        //komanda veic ielādi
        binding = ActivityGameBinding.inflate(layoutInflater)

        // izkārtojumu parāda ekrānā
        setContentView(binding.root)

        // izgūst spēles modeli no datiem
        GameData.fetchGameModel()

        // iestata pogas, kas strādās spēles laukumā, kā "X" un "O"
        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        // iestata starta pogas funkciju
        binding.startGameBtn.setOnClickListener {
            startGame()
        }

        // atjauno spēles interfeisu no izgūtā spēles modeļa
        GameData.gameModel.observe(this){// komanda seko līdzi izmaiņām spēles modelī
            gameModel = it // saglabā izmainīto spēles modeli
            setUI() // izmaina/atjauno ekrānu, pēc jaunā modeļa
        }
    }

    // atjaunina lietotāja interfeisu, skatoties no spēles modeļa
    fun setUI(){
        gameModel?.apply {// ja ir spēles modelis

            // atjaunina pogas spēlē ņemot vērā tā brīža spēles stāvokli
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]

            // atjaunina spēles statusa tekstu, skatoties uz pašreizējo spēles statusu
            binding.startGameBtn.visibility = View.VISIBLE

            binding.gameStatusText.text =
                when(gameStatus){ // kad izveidota online spēle, jautā spēles ID, tad jautā lai lietotājs nospiež sākt
                    GameStatus.CREATED -> {
                        binding.startGameBtn.visibility = View.INVISIBLE // poga sākt spēli neredzama spēles gaitā
                        "Game ID :"+ gameId
                    }
                    GameStatus.JOINED ->{
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS ->{
                        binding.startGameBtn.visibility = View.INVISIBLE // poga sākt spēli neredzama spēles gaitā
                        when(GameData.myID){ // ja ID sakrīt ar savu, rādās teksts "Your turn"
                            currentPlayer -> "Your turn"
                            else ->  currentPlayer + " turn" // ja nesakrīt ID, rādās otra spēlētāja nosaukums
                        }

                    }
                    GameStatus.FINISHED ->{
                        if(winner.isNotEmpty()) { // pārbauda vai nav uzvarētāja
                            when(GameData.myID){ // ja ID sakrīt ar savu, tad lietotājs ir uzvarētājs
                                winner -> "You won"
                                else ->   winner + " Won"
                            }

                        }
                        else "DRAW" // neizšķirts, ja spēles laukums ir pilns, bet nav uzvarētāja
                    }
                }

        }
    }


    fun startGame(){
        gameModel?.apply {
            updateGameData(
                GameModel(
                    gameId = gameId,
                    gameStatus = GameStatus.INPROGRESS
                )
            )
        }
    }

    fun updateGameData(model : GameModel){ // funkcija spēles datu atjaunošanai
        GameData.saveGameModel(model)
    }

    // šeit tiek pārbaudītas visas iespējamās uzvaras kombinācijas - diagonāli, horizontāli un vertikāli
    // ja sakrīt 3 vienādi simboli vienā līnijā, ir zināms uzvarētājs
    fun checkForWinner(){
        val winningPos = arrayOf(
            intArrayOf(0,1,2), // 1. horizontāle
            intArrayOf(3,4,5), // 2. horizontāle
            intArrayOf(6,7,8), // 3. horizontāle
            intArrayOf(0,3,6), // 1. vertikāle
            intArrayOf(1,4,7), // 2. vertikāle
            intArrayOf(2,5,8), // 3. vertikāle
            intArrayOf(0,4,8), // 1. diagonāle
            intArrayOf(2,4,6), // 2. diagonāle
        )

        // pārbauda, vai kāda uzvaras pozīcija ir aizpildīta ar tā paša spēlētāja simbolu
        gameModel?.apply {
            for ( i in winningPos){
                if(
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]]== filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty() // pārbauda, vai simboli uzvaras pozīcijās ir vienādi
                ){
                    gameStatus = GameStatus.FINISHED // ja jā, iestata spēles statusu uz "FINISHED"
                    winner = filledPos[i[0]] // paziņo uzvarētāju
                }
            }

            // pārbauda neizšķirtu, ja visas vietas ir aizpildītas un uzvarētājs nav atrasts
            if( filledPos.none(){ it.isEmpty() }){
                gameStatus = GameStatus.FINISHED
            }

            // saglabā atjaunoto spēles modeli
            updateGameData(this)

        }


    }
    // iestata pogas klikšķus spēles režģī
    override fun onClick(v: View?) {
        gameModel?.apply {
            if(gameStatus!= GameStatus.INPROGRESS){ // pārbauda vai spēle notiek
                Toast.makeText(applicationContext,"Game not started",Toast.LENGTH_SHORT).show()
                return // ja nenotiek, izvada uzlecošu ziņu ar Toast
            }

            // spēle norisinās
            // spēles id ir -1, kad tiek spēlēta offline spēle
            if(gameId!="-1" && currentPlayer!=GameData.myID ){
                Toast.makeText(applicationContext,"Not your turn",Toast.LENGTH_SHORT).show()
                return // ja spēlētājam nav kārta, bet viņš mēģina izpildīt gājienu, tiek izvadīta uzlecoša ziņa
            }

            // iegūst nospiestās pogas pozīciju
            val clickedPos =(v?.tag  as String).toInt()
            // pārbauda vai tā ir tukša
            if(filledPos[clickedPos].isEmpty()){
                // ja ir tukša, ievada pašreizējā spēlētāja simbolu
                filledPos[clickedPos] = currentPlayer
                // ja pašreizējais spēlētājs ir "X", pēc gājiena nomainas uz "O" un otrādi
                currentPlayer = if(currentPlayer=="X") "O" else "X"
                // pēc katra gājiena pārbauda vai nav uzvarētāju
                checkForWinner()
                // pēc katra gājiena arī atjauno spēles datus
                updateGameData(this)
            }

        }
    }
}