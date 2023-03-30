package ru.skillbox.humblr.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.FavoritesSavedBinding
import ru.skillbox.humblr.utils.ZoomTransformer
import ru.skillbox.humblr.utils.adapters.FavoritesAdapter
import ru.skillbox.humblr.utils.adapters.FavoritesSavedAdapter

class FavoritesSavedFragment() : Fragment() {
    var _binding: FavoritesSavedBinding? = null
    val binding: FavoritesSavedBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoritesSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pager.isUserInputEnabled = false
        // binding.pager.setPageTransformer(ZoomTransformer())
        var type: FavoritesSavedAdapter.Type
        val arg = arguments?.getInt(FavoritesAdapter.ARG_TYPE)
        when (arg) {
            0 -> type = FavoritesSavedAdapter.Type.COMMENTS
            else -> type = FavoritesSavedAdapter.Type.LINKS
        }
        binding.pager.adapter = FavoritesSavedAdapter(this, type)
        TabLayoutMediator(binding.tub, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getText(R.string.saved)
                }
                1 -> {
                    tab.text = resources.getText(R.string.all)
                }
            }
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    enum class TabType {
        COMMENTS, LINKS
    }

    companion object {
        const val TYPE = "TYPE"
    }
}