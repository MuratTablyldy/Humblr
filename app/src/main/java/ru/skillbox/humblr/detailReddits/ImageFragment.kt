package ru.skillbox.humblr.detailReddits

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import ru.skillbox.humblr.databinding.FragmentImageBinding

class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    val binding: FragmentImageBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        val arguments: Bundle = requireArguments()
        val imageRes = arguments.getString(KEY_IMAGE_RES)?.replace("amp;", "")

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        binding.image.transitionName = imageRes
        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(this)
            .load(imageRes)
            .listener(object : RequestListener<Drawable?> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                    // startPostponedEnterTransition() should also be called on it to get the transition
                    // going in case of a failure.
                    parentFragment?.startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                    // startPostponedEnterTransition() should also be called on it to get the transition
                    // going when the image is ready.
                    parentFragment?.startPostponedEnterTransition()
                    return false
                }
            })
            .into(binding.image)
        return binding.root
    }

    companion object {
        private const val KEY_IMAGE_RES = "com.google.samples.gridtopager.key.imageRes"
        fun newInstance(url: String): ImageFragment {
            val fragment = ImageFragment()
            val argument = Bundle()
            argument.putString(KEY_IMAGE_RES, url)
            fragment.arguments = argument
            return fragment
        }
    }
}