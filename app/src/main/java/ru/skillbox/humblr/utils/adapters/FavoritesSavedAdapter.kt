package ru.skillbox.humblr.utils.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.skillbox.humblr.news.ResFragment

class FavoritesSavedAdapter(fragment: Fragment, val type: Type) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ResFragment()
        fragment.arguments = Bundle().apply {
            if (type == Type.COMMENTS) {
                when (position) {
                    0 -> {
                        putString(ARG_TYPE, "COMMENTS_ALL")
                    }
                    1 -> {
                        putString(ARG_TYPE, "COMMENTS_SAVED")
                    }
                }

            } else {
                when (position) {
                    0 -> {
                        putString(ARG_TYPE, "LINKS_SAVED")
                    }
                    1 -> {
                        putString(ARG_TYPE, "LINKS_ALL")

                    }
                }
            }
        }
        return fragment
    }

    enum class Type { COMMENTS, LINKS }
    companion object {
        const val ARG_TYPE = "TYPE"

    }

}