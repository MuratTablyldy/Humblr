package ru.skillbox.humblr.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.databinding.WriteMessageFragmentBinding

@AndroidEntryPoint
class WriteMessageFragment() : Fragment() {
    val viewModel: WriteMessageViewModel by viewModels()
    private var _binding: WriteMessageFragmentBinding? = null
    val binding: WriteMessageFragmentBinding
        get() = _binding!!
    val args: WriteMessageFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WriteMessageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userName.text = args.to
        binding.exit.setOnClickListener {
            binding.editor.html = ""
            activity?.onBackPressed()
        }
        binding.editor.setPlaceholder("What are you thoughts?")
        binding.actionItalic.setonClick {
            binding.editor.setItalic()
        }
        binding.actionBold.setonClick {
            binding.editor.setBold()
        }
        binding.actionUnderline.setonClick {
            binding.editor.setUnderline()
        }
        binding.actionAlignCenter.setonClick {
            binding.editor.setAlignCenter()
        }
        binding.actionAlignLeft.setonClick {
            binding.editor.setAlignLeft()
        }
        binding.actionAlignRight.setonClick {
            binding.editor.setAlignRight()
        }
        binding.actionIndent.setonClick {
            binding.editor.setIndent()
        }
        binding.actionOutdent.setonClick {
            binding.editor.setOutdent()
        }
        binding.actionStrikethrough.setonClick {
            binding.editor.setStrikeThrough()
        }
        binding.actionBulet.setonClick {
            binding.editor.setBullets()
        }
        binding.actionUndo.setOnClickListener {
            binding.editor.undo()
        }
        binding.actionRedo.setOnClickListener {
            binding.editor.redo()
        }
        binding.editor.focusEditor()
        binding.post.setOnClickListener {
            if (!binding.title.editText?.text.isNullOrBlank() && !binding.editor.html.isNullOrBlank()) {
                lifecycleScope.launch {
                    when (viewModel.sendMessage(
                        binding.title.editText?.text.toString(),
                        binding.editor.html!!,
                        args.to
                    )) {
                        is Result.Success -> {
                            activity?.onBackPressed()
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                requireContext(), "damn something went wrong", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }
        }
    }
}