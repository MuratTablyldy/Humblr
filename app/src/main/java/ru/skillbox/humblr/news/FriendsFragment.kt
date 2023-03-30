package ru.skillbox.humblr.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.skillbox.humblr.databinding.FriendsListBinding
import ru.skillbox.humblr.utils.adapters.FriendsAdapter

@AndroidEntryPoint
class FriendsFragment : Fragment() {
    var _binding: FriendsListBinding? = null
    val binding: FriendsListBinding
        get() = _binding!!
    var adapter: FriendsAdapter? = null
    val viewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FriendsListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.showLoadingView()
        binding.recyclerView.recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        bind()
        binding.back.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        viewModel.getFriends(null, null, null, null)
        adapter = FriendsAdapter {
            val direction = FriendsFragmentDirections.actionFriendsFragmentToProfileUserFragment(it)
            findNavController().navigate(direction)
        }
        binding.recyclerView.recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun bind() {
        viewModel.apply {
            friends.observe(viewLifecycleOwner) {
                binding.recyclerView.hideAllViews()
                adapter?.addFriends(it)
            }
            empty.observe(viewLifecycleOwner) {
                if (it) {
                    binding.recyclerView.hideAllViews()
                    //binding.recyclerView.showEmptyView(resources.getString(R.string.empty))
                } else {
                    binding.recyclerView.hideAllViews()
                }
            }
            errors.observe(viewLifecycleOwner) {
                binding.recyclerView.showErrorView(it.message)
            }
        }
    }
}