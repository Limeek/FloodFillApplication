package ru.limeek.floodfillapp.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Observer
import ru.limeek.floodfillapp.model.Algorithm
import ru.limeek.floodfillapp.model.Rectangle
import kotlin.math.min

class BitmapCustomView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private val defaultSize: Int = 512
    private val viewModel = BitmapViewModel()

    private val finishObserver = Observer<Rectangle> {
        invalidate()
    }

    init {
        viewModel.fillFinished.observeForever(finishObserver)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewModel.viewSize = viewModel.viewSize.copy(
            measureDimension(widthMeasureSpec),
            measureDimension(heightMeasureSpec)
        )
        setMeasuredDimension(viewModel.viewSize.first, viewModel.viewSize.second)
    }

    private fun measureDimension(measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(size, defaultSize)
            MeasureSpec.UNSPECIFIED -> defaultSize
            else -> defaultSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawBitmap(canvas)
    }

    private fun drawBitmap(canvas: Canvas) {
        viewModel.calculateRectangleSizes()
        for (i in 0 until viewModel.bitmapMatrix.size)
            for (j in 0 until viewModel.bitmapMatrix[i].size) {
                val rectangle = viewModel.bitmapMatrix[i][j]
                viewModel.calculateRectanglesCoordinates(rectangle, i, j)
                drawRectangle(canvas, rectangle)
            }
    }

    private fun drawRectangle(canvas: Canvas, rectangle: Rectangle) {
        canvas.drawRect(
            rectangle.left,
            rectangle.top,
            rectangle.right,
            rectangle.bottom,
            Paint().apply { color = rectangle.colorFill }
        )
    }

    fun setBitmapMatrix(matrix: Array<Array<Boolean>>) {
        viewModel.clearMatrix()
        viewModel.initMatrix(matrix)
    }

    fun setBitmapSize(bitmapSize: Pair<Int, Int>) {
        viewModel.bitmapSize = bitmapSize
    }

    fun triggerFillEvent(event: MotionEvent): Boolean {
        viewModel.triggerFill(event.x, event.y)
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {

            val sizeX = state.getInt("sizeX")
            val sizeY = state.getInt("sizeY")
            viewModel.speed = state.getLong("speed")

            viewModel.bitmapSize = Pair(sizeX, sizeY)

            for (i in 0 until sizeX) {
                viewModel.initMatrixLine(i, state.getParcelableArray("$i") as Array<Rectangle>)
            }

            val viewState: Parcelable? = state.getParcelable("superState")

            super.onRestoreInstanceState(viewState)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("sizeX", viewModel.bitmapSize.first)
        bundle.putInt("sizeY", viewModel.bitmapSize.second)
        bundle.putLong("speed", viewModel.speed)

        for (i in 0 until viewModel.bitmapMatrix.size)
            bundle.putParcelableArray("$i", viewModel.bitmapMatrix[i])

        return bundle
    }

    private fun unsubscribe() {
        viewModel.fillFinished.removeObserver(finishObserver)
    }

    fun setAlgorithm(algorithm: Algorithm) {
        viewModel.algorithm = algorithm
    }

    fun setSpeed(speed: Long){
        viewModel.speed = speed
    }

    fun isFloodFillRunning(): Boolean {
        return viewModel.floodFillJob != null && !viewModel.floodFillJob!!.isCompleted
    }
}