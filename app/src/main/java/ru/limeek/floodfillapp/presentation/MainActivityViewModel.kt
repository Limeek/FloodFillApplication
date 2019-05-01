package ru.limeek.floodfillapp.presentation

import androidx.lifecycle.ViewModel
import ru.limeek.floodfillapp.model.Algorithm

class MainActivityViewModel: ViewModel(){
    var bitmapMatrix: Array<Array<Boolean>>? = null
    var bitmapSize = Pair(10,10)
    var spinnerEntries = Algorithm.values()


    fun generateMatrix(){
        bitmapMatrix = Array(bitmapSize.first){
            Array(bitmapSize.second){ (0..1).random() == 1}
        }
    }

}