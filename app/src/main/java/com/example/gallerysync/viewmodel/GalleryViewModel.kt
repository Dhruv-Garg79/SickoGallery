package com.example.gallerysync.viewmodel

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

class GalleryViewModel : ViewModel() {
    private var startingRow = 0
    private var rowsToLoad = 0
    private var allLoaded = false
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun getImagesFromGallery(context: Context, pageSize: Int, list: (List<String>) -> Unit) {
        uiScope.launch {
            val asyncList = async {
                fetchGalleryImages(context, pageSize)
            }
            list(asyncList.await())
        }
    }

    fun getGallerySize(context: Context): Int {
        val columns =
            arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID
            ) //get all columns of type images
        val orderBy = MediaStore.Images.Media.DATE_ADDED //order data by date

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order

        val rows = cursor!!.count
        cursor.close()
        return rows
    }

    private fun fetchGalleryImages(context: Context, rowsPerLoad: Int): List<String> {
        val galleryImageUrls = ArrayList<String>()
        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID
        ) //get all
        // columns of type images
        val orderBy = MediaStore.Images.Media.DATE_ADDED //order data by date

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, "$orderBy DESC"
        ) //get all data in Cursor by sorting in DESC order

        Log.i("GalleryAllLoaded", "$allLoaded")

        if (cursor != null && !allLoaded) {

            val totalRows = cursor.count

            allLoaded = rowsToLoad == totalRows

            if (rowsToLoad < rowsPerLoad) {
                rowsToLoad = rowsPerLoad
            }

            for (i in startingRow until rowsToLoad) {
                cursor.moveToPosition(i)
                val dataColumnIndex =
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA) //get column index
                galleryImageUrls.add(cursor.getString(dataColumnIndex)) //get Image from column index

            }
            Log.i("TotalGallerySize", "$totalRows")
            Log.i("GalleryStart", "$startingRow")
            Log.i("GalleryEnd", "$rowsToLoad")

            startingRow = rowsToLoad

            if (rowsPerLoad > totalRows || rowsToLoad >= totalRows)
                rowsToLoad = totalRows
            else {
                if (totalRows - rowsToLoad <= rowsPerLoad)
                    rowsToLoad = totalRows
                else
                    rowsToLoad += rowsPerLoad
            }
            cursor.close()
            Log.i("PartialGallerySize", " ${galleryImageUrls.size}")
        }

        return galleryImageUrls
    }

    override fun onCleared() {
        viewModelJob.cancel()
    }
}
