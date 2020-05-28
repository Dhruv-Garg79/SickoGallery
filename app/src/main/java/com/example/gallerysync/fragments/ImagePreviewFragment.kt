package com.example.gallerysync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.gallerysync.R
import kotlinx.android.synthetic.main.fragment_image_preview.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class ImagePreviewFragment : Fragment() {

    val args : ImagePreviewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val path = args.imagePath
        Glide.with(this.requireView()).load(File(path)).into(imageView)
    }

}
