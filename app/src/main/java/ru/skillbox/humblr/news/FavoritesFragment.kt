package ru.skillbox.humblr.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.FavoritesFragmentBinding
import ru.skillbox.humblr.utils.adapters.FavoritesAdapter

class FavoritesFragment:Fragment() {
    var _binding:FavoritesFragmentBinding?=null
    val binding:FavoritesFragmentBinding
    get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FavoritesFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pager.isUserInputEnabled=false
        binding.pager.adapter=FavoritesAdapter(this)
        TabLayoutMediator(binding.tub,binding.pager){ tab, position ->
            when(position){
                0->{
                    tab.text=resources.getText(R.string.comments)
                }
                1->{
                    tab.text=resources.getText(R.string.subreddits)
                }
            }
        }.attach()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}