package com.example.testingfirebaserealtime.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.testingfirebaserealtime.DataClass
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import kotlinx.coroutines.tasks.await

class FirebasePagingSource(
    private val query: Query
) : PagingSource<DataSnapshot, DataClass>() {

    override suspend fun load(params: LoadParams<DataSnapshot>): LoadResult<DataSnapshot, DataClass> {
        return try {
            val currentPage = params.key ?: query.get().await()

            val dataList = mutableListOf<DataClass>()
            for (snapshot in currentPage.children) {
                val dataClass = snapshot.getValue(DataClass::class.java)
                dataClass?.let { dataList.add(it) }
            }

            val nextKey = if (dataList.isEmpty()) {
                null
            } else {
                currentPage.children.lastOrNull()
            }

            LoadResult.Page(
                data = dataList,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<DataSnapshot, DataClass>): DataSnapshot? {
        return null
    }
}