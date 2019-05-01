package ru.limeek.floodfillapp.presentation

import android.graphics.Color
import kotlinx.coroutines.*
import ru.limeek.floodfillapp.model.Algorithm
import ru.limeek.floodfillapp.model.Rectangle
import ru.limeek.floodfillapp.util.SingleLiveEvent
import java.util.*
import kotlin.coroutines.CoroutineContext

class BitmapViewModel : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

    var algorithm: Algorithm = Algorithm.ONE
    var bitmapSize = Pair(10, 10)
    var viewSize = Pair(0, 0)
    var targetColor = Color.BLACK
    var replacementColor = Color.BLUE
    var widthPerDot = 0f
    var heightPerDot = 0f
    var speed: Long = 1500

    var fillFinished = SingleLiveEvent<Rectangle>()
    var floodFillJob: Job? = null

    lateinit var bitmapMatrix: Array<Array<Rectangle>>

    fun clearMatrix() {
        bitmapMatrix = Array(bitmapSize.first) {
            Array(bitmapSize.second) { Rectangle() }
        }
    }

    fun initMatrix(matrix: Array<Array<Boolean>>) {
        bitmapMatrix = Array(bitmapSize.first) { i ->
            Array(bitmapSize.second) { j ->
                if (matrix[i][j])
                    Rectangle(Color.BLACK, i, j)
                else
                    Rectangle(Color.WHITE, i, j)
            }
        }
    }

    fun initMatrixLine(lineNumber: Int, listRecs: Array<Rectangle>){
        bitmapMatrix[lineNumber] = listRecs
    }

    fun calculateRectangleSizes() {
        widthPerDot = sizePerDot(viewSize.first, bitmapSize.first)
        heightPerDot = sizePerDot(viewSize.second, bitmapSize.second)
    }

    fun calculateRectanglesCoordinates(rectangle: Rectangle, xIteration: Int, yIteration: Int) {
        rectangle.left = widthPerDot * xIteration
        rectangle.right = widthPerDot * (xIteration + 1)
        rectangle.top = heightPerDot * yIteration
        rectangle.bottom = heightPerDot * (yIteration + 1)
    }

    private fun getTouchedRectangle(x: Float, y: Float): Rectangle? {
        for (i in 0 until bitmapMatrix.size) {
            for (j in 0 until bitmapMatrix[i].size) {
                val rect = bitmapMatrix[i][j]
                if (x < rect.right && x > rect.left &&
                    y < rect.bottom && y > rect.top
                ) {
                    return rect
                }
            }
        }
        return null
    }

    fun triggerFill(x: Float, y: Float){
        when(algorithm){
            Algorithm.ONE -> floodFillLeft(x,y)
            Algorithm.TWO -> floodFillRight(x,y)
            Algorithm.THREE -> floodFillDown(x,y)
            Algorithm.FOUR -> floodFillUp(x,y)

        }
    }

    private fun floodFillLeft(x: Float, y: Float) {
        floodFillJob = launch(Dispatchers.Default) {
            val startRect = getTouchedRectangle(x, y)
            val rectQueue: Queue<Rectangle> = LinkedList<Rectangle>()

            if (startRect != null) {
                targetColor = startRect.colorFill
                replacementColor = when(targetColor){
                    Color.BLACK -> Color.BLUE
                    Color.YELLOW -> Color.WHITE
                    Color.WHITE -> Color.YELLOW
                    Color.BLUE -> Color.BLACK
                    else -> Color.BLACK
                }
                rectQueue.add(startRect)

                while (rectQueue.isNotEmpty()) {
                    val rect = rectQueue.poll()
                    var xPos = rect.x
                    var yPos = rect.y

                    while (xPos > 0 && bitmapMatrix[xPos - 1][yPos].colorFill == targetColor)
                        xPos--

                    var spanUp = false
                    var spanDown = false

                    while (xPos < bitmapSize.first && bitmapMatrix[xPos][yPos].colorFill == targetColor) {
                        bitmapMatrix[xPos][yPos].colorFill = replacementColor
                        fillFinished.postValue(bitmapMatrix[xPos][yPos])
                        delay(speed)

                        if (!spanDown && yPos < bitmapSize.second - 1 && bitmapMatrix[xPos][yPos + 1].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos][yPos + 1])
                            spanDown = true
                        } else if (spanDown && yPos < bitmapSize.second - 1 && bitmapMatrix[xPos][yPos + 1].colorFill != targetColor)
                            spanDown = false

                        if (!spanUp && yPos > 0 && bitmapMatrix[xPos][yPos - 1].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos][yPos - 1])
                            spanUp = true
                        } else if (spanUp && yPos > 0 && bitmapMatrix[xPos][yPos - 1].colorFill != targetColor) {
                            spanUp = false
                        }

                        xPos++
                    }
                }
            }
        }
    }

    private fun floodFillRight(x: Float, y: Float) {
        floodFillJob = launch(Dispatchers.Default) {
            val startRect = getTouchedRectangle(x, y)
            val rectQueue: Queue<Rectangle> = LinkedList<Rectangle>()

            if (startRect != null) {
                targetColor = startRect.colorFill
                replacementColor = when(targetColor){
                    Color.BLACK -> Color.BLUE
                    Color.YELLOW -> Color.WHITE
                    Color.WHITE -> Color.YELLOW
                    Color.BLUE -> Color.BLACK
                    else -> Color.BLACK
                }
                rectQueue.add(startRect)

                while (rectQueue.isNotEmpty()) {
                    val rect = rectQueue.poll()
                    var xPos = rect.x
                    var yPos = rect.y

                    while (xPos < bitmapSize.first - 1 && bitmapMatrix[xPos + 1][yPos].colorFill == targetColor)
                        xPos++

                    var spanUp = false
                    var spanDown = false

                    while (xPos >= 0 && bitmapMatrix[xPos][yPos].colorFill == targetColor) {
                        bitmapMatrix[xPos][yPos].colorFill = replacementColor
                        fillFinished.postValue(bitmapMatrix[xPos][yPos])
                        delay(speed)
                        if (!spanUp && yPos > 0 && bitmapMatrix[xPos][yPos - 1].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos][yPos - 1])
                            spanUp = true
                        } else if (spanUp && yPos > 0 && bitmapMatrix[xPos][yPos - 1].colorFill != targetColor) {
                            spanUp = false
                        }

                        if (!spanDown && yPos < bitmapSize.second - 1 && bitmapMatrix[xPos][yPos + 1].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos][yPos + 1])
                            spanDown = true
                        } else if (spanDown && yPos < bitmapSize.second - 1 && bitmapMatrix[xPos][yPos + 1].colorFill != targetColor)
                            spanDown = false

                        xPos--
                    }
                }
            }
        }
    }

    private fun floodFillDown(x: Float, y: Float){
        floodFillJob = launch(Dispatchers.Default) {
            val startRect = getTouchedRectangle(x, y)
            val rectQueue: Queue<Rectangle> = LinkedList<Rectangle>()

            if (startRect != null) {
                targetColor = startRect.colorFill
                replacementColor = when(targetColor){
                    Color.BLACK -> Color.BLUE
                    Color.YELLOW -> Color.WHITE
                    Color.WHITE -> Color.YELLOW
                    Color.BLUE -> Color.BLACK
                    else -> Color.BLACK
                }
                rectQueue.add(startRect)

                while (rectQueue.isNotEmpty()) {
                    val rect = rectQueue.poll()
                    var xPos = rect.x
                    var yPos = rect.y

                    while (yPos < bitmapSize.second - 1 && bitmapMatrix[xPos][yPos + 1].colorFill == targetColor)
                        yPos++

                    var spanLeft = false
                    var spanRight = false

                    while (yPos >= 0 && bitmapMatrix[xPos][yPos].colorFill == targetColor) {
                        bitmapMatrix[xPos][yPos].colorFill = replacementColor
                        fillFinished.postValue(bitmapMatrix[xPos][yPos])
                        delay(speed)
                        if (!spanLeft && xPos > 0 && bitmapMatrix[xPos - 1][yPos].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos - 1][yPos])
                            spanLeft = true
                        } else if (spanLeft && xPos > 0 && bitmapMatrix[xPos - 1][yPos].colorFill != targetColor) {
                            spanLeft = false
                        }

                        if (!spanRight && xPos < bitmapSize.first - 1 && bitmapMatrix[xPos + 1][yPos].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos + 1][yPos])
                            spanRight = true
                        } else if (spanRight && xPos < bitmapSize.first - 1 && bitmapMatrix[xPos + 1][yPos].colorFill != targetColor)
                            spanRight = false

                        yPos--
                    }
                }
            }
        }
    }

    private fun floodFillUp(x: Float, y: Float){
        floodFillJob = launch(Dispatchers.Default) {
            val startRect = getTouchedRectangle(x, y)
            val rectQueue: Queue<Rectangle> = LinkedList<Rectangle>()

            if (startRect != null) {
                targetColor = startRect.colorFill
                replacementColor = when(targetColor){
                    Color.BLACK -> Color.BLUE
                    Color.YELLOW -> Color.WHITE
                    Color.WHITE -> Color.YELLOW
                    Color.BLUE -> Color.BLACK
                    else -> Color.BLACK
                }
                rectQueue.add(startRect)

                while (rectQueue.isNotEmpty()) {
                    val rect = rectQueue.poll()
                    var xPos = rect.x
                    var yPos = rect.y

                    while (yPos > 0 && bitmapMatrix[xPos][yPos - 1].colorFill == targetColor)
                        yPos--

                    var spanLeft = false
                    var spanRight = false

                    while (yPos < bitmapSize.second && bitmapMatrix[xPos][yPos].colorFill == targetColor) {
                        bitmapMatrix[xPos][yPos].colorFill = replacementColor
                        fillFinished.postValue(bitmapMatrix[xPos][yPos])
                        delay(speed)
                        if (!spanLeft && xPos > 0 && bitmapMatrix[xPos - 1][yPos].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos - 1][yPos])
                            spanLeft = true
                        } else if (spanLeft && xPos > 0 && bitmapMatrix[xPos - 1][yPos].colorFill != targetColor) {
                            spanLeft = false
                        }

                        if (!spanRight && xPos < bitmapSize.first - 1 && bitmapMatrix[xPos + 1][yPos].colorFill == targetColor) {
                            rectQueue.add(bitmapMatrix[xPos + 1][yPos])
                            spanRight = true
                        } else if (spanRight && xPos < bitmapSize.first - 1 && bitmapMatrix[xPos + 1][yPos].colorFill != targetColor)
                            spanRight = false

                        yPos++
                    }
                }
            }
        }
    }

    private fun sizePerDot(viewSize: Int, bitmapSize: Int): Float {
        return viewSize.toFloat() / bitmapSize
    }
}