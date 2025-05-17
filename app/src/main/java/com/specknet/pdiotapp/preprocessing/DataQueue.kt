package com.specknet.pdiotapp.preprocessing

import java.util.LinkedList
import java.util.Queue
class DataQueue<E : Any> : LinkedList<E>(), Queue<E> {
    companion object {
        const val MAX_SIZE = 50

        fun toString(array: FloatArray): String {
            return array.joinToString(separator = ",")
        }
    }

    @Synchronized
    fun addPop(e: E) {
        if (size < MAX_SIZE) {
            add(e)
        } else {
            pop()
            add(e)
        }
    }

    fun isFull(): Boolean {
        return size == MAX_SIZE
    }

    @Synchronized
    fun arrayToString(): String {
        return this.joinToString(separator = ",")
    }
}
