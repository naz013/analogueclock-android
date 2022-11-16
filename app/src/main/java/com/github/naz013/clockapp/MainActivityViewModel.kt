package com.github.naz013.clockapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

    private val _time = MutableLiveData<Long>()
    val time: LiveData<Long> = _time


}