package com.example.gallerysync.fragments

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallerysync.GalleryAdapter
import com.example.gallerysync.R
import com.example.gallerysync.viewmodel.GalleryViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.gallery_fragment.*

class GalleryFragment : Fragment() {

    private lateinit var viewModel: GalleryViewModel
    private val pictures by lazy {
        ArrayList<String>(viewModel.getGallerySize(this.requireContext()))
    }

    private val galleryAdapter by lazy {
        GalleryAdapter { path ->
            (this.requireActivity() as AppCompatActivity).supportActionBar?.hide()
            findNavController().navigate(
                GalleryFragmentDirections.actionGalleryFragmentToImagePreviewFragment(
                    path
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)

        requestReadStoragePermission()
    }

    private fun requestReadStoragePermission() {
        val readStorage = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this.requireContext(),
                readStorage
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(readStorage), 3)
        } else
            init()
    }

    private fun init() {
        val pageSize = 20
        val gridLayoutManager = GridLayoutManager(this.activity, 2)
        recycler_view.apply {
            layoutManager = gridLayoutManager
            adapter = galleryAdapter
        }

        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (gridLayoutManager.findLastVisibleItemPosition() == galleryAdapter.itemCount - 1) {
                    loadPictures(pageSize)
                }
            }
        })

        loadPictures(pageSize)
        camera_button.setOnClickListener {
            (this.requireActivity() as AppCompatActivity).supportActionBar?.hide()
            findNavController().navigate(R.id.action_galleryFragment_to_cameraFragment)
        }
    }

    private fun loadPictures(pageSize: Int) {
        viewModel.getImagesFromGallery(this.requireContext(), pageSize) {
            if (it.isNotEmpty()) {
                pictures.addAll(it)
                galleryAdapter.update(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gallery_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sync) {
            val rotate = ObjectAnimator.ofFloat(recycler_view, View.ROTATION, -360f, 0f)
            val rotateY = ObjectAnimator.ofFloat(recycler_view, View.ROTATION_Y, -360f, 0f)
            val set = AnimatorSet()
            set.playTogether(rotate, rotateY)
            set.duration = 1000
            set.start()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            init()
        else {
            Snackbar
                .make(
                    this.requireView(),
                    "Permission Required to Fetch Gallery.",
                    Snackbar.LENGTH_LONG
                )
                .setAction("Allow") { requestReadStoragePermission() }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        (this.requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    companion object {
        const val TAG = "GalleryFragment"
    }
}
