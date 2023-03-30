package ru.skillbox.humblr.utils.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.skillbox.humblr.news.FavoritesSavedFragment

class FavoritesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = FavoritesSavedFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_TYPE, position)
        }
        return fragment
    }

    companion object {
        const val ARG_TYPE = "TYPE"
    }
}