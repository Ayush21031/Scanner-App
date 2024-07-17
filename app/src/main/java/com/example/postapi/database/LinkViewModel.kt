package com.example.postapi.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LinkViewModel(application: Application): AndroidViewModel(application) {
    private val repository: LinkRepository

    init {
        val linkDao = LinkDatabase.getDatabase(application).linkDao()
        repository = LinkRepository(linkDao)
    }

    fun addLink(link: Link){
        viewModelScope.launch {
            repository.insert(link)
        }
    }

    fun getLinks(): List<Link>{
        return repository.getAllLinks()
    }
}
