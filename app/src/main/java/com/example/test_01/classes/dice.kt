package com.example.test_01.classes

import android.util.Log

class dice (val sides:Int=6) {
    fun roll(): Int {
        Log.i("Files","rolling dice")
        return (1..sides).random()
    }
}