package com.example.postapi.database

class LinkRepository(private val linkDao: LinkDao) {
    suspend fun insert(link: Link) {
        linkDao.insert(link)
    }

    fun getAllLinks(): List<Link> = linkDao.getAllLinks()
}