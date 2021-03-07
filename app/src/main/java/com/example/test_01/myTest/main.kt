package com.example.test_01

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.test_01.myTest.test
import java.util.*

//open face posibila mostenirea clasei
open class cub(x_length:Int =1, y_length:Int=2, z_length:Int=3) {
    var x_length = x_length
    open var y_length = y_length
    var z_length: Int = z_length
        get() = x_length * y_length //getter custom
    protected var w: Int = 10
        private set(n) {
            field = n*10
            x_length=100
        }

    init {
        println("sunt in blocul init a cubului")
    }

    constructor(lista_param: List<Int>) : this() //un consttructor secundar trebuie sa apeleze neaparat constructorul default mai intai
    {
        x_length = lista_param[0]
        y_length = lista_param[1]
        z_length = lista_param[2]
    }

    fun print_all(param: Int = 132) {
        println("Am numerele x: ${x_length}, y: ${y_length}, z: ${z_length}, \nparametru primit ${param}, \nvolum:${x_length}")
    }
}

class subCub(x_length:Int =1, y_length:Int=1, z_length:Int=1) : cub(x_length=x_length,y_length=y_length,z_length=z_length){
    fun print_x(){
        print(x_length)
    }
    override var y_length: Int = 0
        get() = 100
}

@RequiresApi(Build.VERSION_CODES.N)
fun main() {
    println("hi")
    val cub = cub(z_length = 1, y_length = 2, x_length = 3)
    val array_java = ArrayList<Int>().apply {
        add(1)
        add(2)
        add(3)
    }
    array_java.stream().forEach { x: Int -> println(x) }


    val aux = array_java.stream()
        .map { x: Int -> x.toString() + "str" } //NU E NEVOIE collect(Collector.toList())
    aux.forEach { x: String -> println(x) }

    val cub2 = cub()
    val cub3 = cub(array_java)
    cub.print_all()

    val clasa_java_proprie = test(4)
    println("\n")
    println("Am apelat o metoda java proprie si am obtinut ${clasa_java_proprie.p1}")

    for (i: Int in 1..6)
        print(i)
}