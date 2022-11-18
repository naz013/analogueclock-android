package com.github.naz013.clockapp.adapter

class BiDirectionalContract<C, P> {

    private var childListener: ((P) -> Unit)? = null
    private var parentListener: ((C) -> Unit)? = null

    fun notifyParent(data: C) {
        parentListener?.invoke(data)
    }

    fun notifyChild(data: P) {
        childListener?.invoke(data)
    }

    fun listenChild(listener: (C) -> Unit) {
        this.parentListener = listener
    }

    fun listenParent(listener: (P) -> Unit) {
        this.childListener = listener
    }
}