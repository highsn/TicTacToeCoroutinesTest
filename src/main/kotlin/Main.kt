import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

var totalWins = mutableMapOf(Player.RED to 0, Player.BLUE to 0, Player.DRAW to 0)

var squares = mutableMapOf<Int, Player>()
var redIsNext = true

enum class Player {
    RED,
    BLUE,
    DRAW
}

fun main() = runBlocking {
//    val time = start()
    val time = startWithCoroutines()

    println("we have winners: Red = ${totalWins[Player.RED]}")
    println("we have winners: Blue = ${totalWins[Player.BLUE]}")
    println("we have winners: Draw = ${totalWins[Player.DRAW]}")
    println("time was $time")
}

suspend fun startWithCoroutines(): Long = withContext(Dispatchers.Default) {
    return@withContext measureTimeMillis {
        List(100_000) {
            launch {
                newGame {
                    totalWins.computeIfPresent(it) { _, score -> score + 1 }
                }
            }
        }
    }
}

fun start(): Long {
    return measureTimeMillis {
        List(100_000) {
            newGame {
                totalWins.computeIfPresent(it) { _, score -> score + 1 }
            }
        }
    }
}

fun newGame(winner: (Player) -> Unit) {
    squares = mutableMapOf()
    redIsNext = true
    playGame(winner)
}

// all possible lines to win
private val lines: Array<IntArray> = arrayOf(
    intArrayOf(0, 1, 2),
    intArrayOf(3, 4, 5),
    intArrayOf(6, 7, 8),
    intArrayOf(0, 3, 6),
    intArrayOf(1, 4, 7),
    intArrayOf(2, 5, 8),
    intArrayOf(0, 4, 8),
    intArrayOf(2, 4, 6)
)

/*
* Randomly get an empty cell
* if returned -1 then no empty cells remain
 */
fun getCellForPlayer(): Int {
    for (cell in (1..9).toMutableList().also { it.shuffle() }) {
        if (!squares.containsKey(cell)) {
            return cell
        }
    }
    return -1
}

fun playGame(winnerCallback: (Player) -> Unit) {
    squares[getCellForPlayer()] = if (redIsNext) Player.RED else Player.BLUE

    if (squares.containsKey(-1)) {
        winnerCallback.invoke(Player.DRAW)
        return
    }

    val winner = checkWinner()
    if (winner == null) {
        redIsNext = !redIsNext
        playGame(winnerCallback)
    } else {
        winnerCallback.invoke(winner)
    }
}

// return the player which has all tree squares of a line or null if there is none
private fun checkWinner(): Player? {
    if (squares.isNotEmpty()) for (line in lines) {
        val (a, b, c) = line
        if (squares[a] != null && squares[a] == squares[b] && squares[a] == squares[c]) {
            return squares[a]
        }
    }
    return null
}
