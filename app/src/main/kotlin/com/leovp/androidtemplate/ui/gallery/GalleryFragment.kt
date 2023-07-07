package com.leovp.androidtemplate.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.leovp.androidbase.framework.BaseFragment
import com.leovp.androidtemplate.databinding.AppFragmentGalleryBinding

class GalleryFragment : BaseFragment<AppFragmentGalleryBinding>() {

    override fun getTagName(): String = "GF"
    private val galleryViewModel by activityViewModels<GalleryViewModel>()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): AppFragmentGalleryBinding {
        return AppFragmentGalleryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
    }
}
