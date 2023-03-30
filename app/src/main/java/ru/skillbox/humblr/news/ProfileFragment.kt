package ru.skillbox.humblr.news


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.UserInfoFragmentBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.Com


@AndroidEntryPoint
class ProfileFragment : Fragment() {
    var _binding: UserInfoFragmentBinding? = null
    val binding: UserInfoFragmentBinding
        get() = _binding!!
    val viewModel: ProfileViewModel by viewModels()
    var sharedPreff:SharedPreferences?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreff=(activity as MainActivity).getSharedPreferences("night",0)
        val isNight=sharedPreff?.getBoolean("night_mode",false)
        if(isNight==true){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.switchy.isChecked=true
        }
        binding.switchy.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked){
                binding.switchy.isChecked=true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val editor=sharedPreff!!.edit()
                editor.putBoolean("night_mode",true)
                editor.apply()
            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val editor=sharedPreff!!.edit()
                binding.switchy.isChecked=false
                editor.putBoolean("night_mode",false)
                editor.apply()
            }
        }
        viewModel.getMe()
        viewModel.getSubreddits(null, null, null, null)
        bind()
        binding.switchy
        binding.removeSavedButton.setOnClickListener {
            unsaveAll()
        }
        binding.friendsListButton.setOnClickListener {
            val direction = ProfileFragmentDirections.actionProfileFragmentToFriendsFragment()
            findNavController().navigate(direction)
        }
        binding.exitButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.exit()
                (activity as MainActivity).goToLogin()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun bind() {
        viewModel.apply {
            account.observe(viewLifecycleOwner) {
                Glide.with(requireContext()).load(it.snoovatar).into(binding.avatar)
                binding.userName.text = it.name
                binding.nickName.text = it.id
                binding.comments.text
                viewModel.getCommentsMine(
                    it.name!!,
                    null,
                    null,
                    null,
                    null,
                    true,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top
                )
            }
            subreddits.observe(viewLifecycleOwner) {
                if (it != null)
                    binding.subreddits.text =
                        String.format(resources.getString(R.string.subredditsNum), it.count())
            }
            mineComments.observe(viewLifecycleOwner) {
                if (it != null)
                    binding.comments.text =
                        String.format(resources.getString(R.string.commentsNum), it.count())
            }
        }
    }

    fun unsaveAll() {
        lifecycleScope.launch {
            val saved = viewModel.getCommentsSaved(
                null,
                null,
                null,
                null,
                null,
                RedditApi.Time.all,
                2,
                RedditApi.Sort.top
            )
            when (saved) {
                is Result.Success -> {
                    val comments = saved.data.data.children
                    comments?.forEach {
                        viewModel.unsave("t1_${it.data.id}")
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            when (val savedLinks = viewModel.getSavedSubreddit(
                null,
                null,
                null,
                null,
                null,
                RedditApi.Time.all,
                2,
                RedditApi.Sort.top
            )) {
                is Result.Success -> {
                    val sav = savedLinks.data.data.children
                    sav?.forEach { link ->
                        val iq = link.data as Created
                        viewModel.unsave("t3_${iq.getIds()}")
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}