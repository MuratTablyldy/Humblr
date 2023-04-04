package ru.skillbox.humblr.ui.login.starterFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.StarterViewBinding

class StarterFragment : Fragment() {
    private var _binding: StarterViewBinding? = null
    val binding: StarterViewBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StarterViewBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PagerAdapter()
        adapter.addList(
            listOf(
                Page(R.drawable.ic_page1, R.string.welcome, R.string.welcome_text),
                Page(R.drawable.ic_group_7, R.string.sub_humblr, R.string.sub_humblr_text),
                Page(R.drawable.ic_group_10, R.string.action_humblr, R.string.sub_humblr_text)
            )
        )
        binding.pager.adapter = adapter
        binding.dotsIndicator.setViewPager2(binding.pager)
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    binding.button.text = resources.getText(R.string.exit)
                } else {
                    binding.button.text = resources.getText(R.string.skip)
                }
            }
        })
        val skip = resources.getText(R.string.skip)
        val exit = resources.getText(R.string.exit)
        binding.button.setOnClickListener { view ->
            when (binding.button.text) {
                skip -> {
                    val position = binding.pager.currentItem
                    binding.pager.currentItem = position + 1
                }
                exit -> {
                    val action = StarterFragmentDirections.actionStarterFragmentToLogFragment()
                    findNavController().navigate(action)
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}