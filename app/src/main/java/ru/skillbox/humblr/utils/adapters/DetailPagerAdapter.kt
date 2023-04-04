package ru.skillbox.humblr.utils.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.skillbox.humblr.detailReddits.ImageFragment

class DetailPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var pictList: List<String> = emptyList()
    override fun getItemCount(): Int {
        return pictList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImageFragment.newInstance(pictList[position])
    }

    fun setList(list: List<String>) {
        pictList = list
        notifyItemRangeChanged(0, list.lastIndex)
    }
}