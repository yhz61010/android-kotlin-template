package com.leovp.androidtemplate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.leovp.androidbase.framework.BaseFragment
import com.leovp.androidtemplate.databinding.AppFragmentHomeBinding

class HomeFragment : BaseFragment<AppFragmentHomeBinding>() {

    override fun getTagName(): String = "HF"
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): AppFragmentHomeBinding {
        return AppFragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
    }
}
