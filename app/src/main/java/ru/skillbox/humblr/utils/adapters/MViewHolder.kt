package ru.skillbox.humblr.utils.adapters

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController
import kohii.v1.core.Playback
import kohii.v1.core.Rebinder
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.databinding.ItemLinkBinding
import ru.skillbox.humblr.databinding.LoadingViewBinding
import ru.skillbox.humblr.databinding.SubredditLayoutBinding
import ru.skillbox.humblr.databinding.SubredditLayoutRedditVideoBinding
import ru.skillbox.humblr.databinding.SubredditLayoutYoutubeBinding
import ru.skillbox.humblr.databinding.SubredditPictLayoutViewBinding
import ru.skillbox.humblr.databinding.WithOutLinkItemBinding
import ru.skillbox.humblr.news.NewsFragmentDirections
import ru.skillbox.humblr.utils.MButton
import ru.skillbox.humblr.utils.MCallBack
import java.util.concurrent.atomic.AtomicLong
import kotlin.properties.Delegates

sealed class MViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {
    class NewsWithoutPictViewHolder(containerView: View) : MViewHolder(containerView) {
        var binding: SubredditLayoutBinding? = null

        init {
            binding = DataBindingUtil.bind(containerView)

        }
    }

    class NewsWithRedditVideoHolder(containerView: View) : MViewHolder(containerView),Playback.ArtworkHintListener,
        View.OnClickListener {
        var binding: SubredditLayoutRedditVideoBinding? = null

        init {
            binding = DataBindingUtil.bind(containerView)

        }

         fun onAttached() {
             binding?.root?.setOnClickListener(this)
            binding?.commentButton?.setOnClickListener(this)
        }

        fun onDetached() {
            binding?.root?.setOnClickListener(null)
            binding?.commentButton?.setOnClickListener(null)
        }

        internal var videoData by Delegates.observable<Link.LinkRedditVideo?>(
            initialValue = null,
            onChange = { _, _, value ->
                if (value != null) {
                    image = value.preview?.images?.first()?.source?.url
                    videoLink = value.mediaEmbed!!.reddit_video!!.dash_url
                    binding?.item=value
                    binding?.commentButton?.setOnClickListener(this)

                } else {
                    image = null
                    image = null
                }
            }
        )
        val thumbnail=binding?.thumbnail
        var image:String?=null
        var videoLink:String?=null

        internal val videoTag: String?
            get() = this.videoData?.let { "$it::$bindingAdapterPosition" }
        internal val rebinder: Rebinder?
            get() = this.videoTag?.let {
                val reb=Rebinder(it)
                reb.with {
                    controller=object:Playback.Controller{
                        override fun kohiiCanPause(): Boolean {
                            return true
                        }

                        override fun kohiiCanStart(): Boolean {
                            return true
                        }

                        override fun setupRenderer(playback: Playback, renderer: Any?) {
                            super.setupRenderer(playback, renderer)
                            if(renderer is PlayerView){
                                renderer.setOnClickListener(this@NewsWithRedditVideoHolder)
                                renderer.useController=true
                            }
                        }

                        override fun teardownRenderer(playback: Playback, renderer: Any?) {
                            super.teardownRenderer(playback, renderer)
                            if(renderer is PlayerView){
                                renderer.useController=false
                            }
                        }
                    }
                    repeatMode= Player.REPEAT_MODE_ALL
                    threshold=1f
                }

                reb
            }

        fun onRecycled() {
            binding?.thumbnail?.isVisible = false
            this.videoData=null
            thumbnail?.isVisible = true
        }

        fun clearTransientStates() {
            binding?.thumbnail?.clearAnimation()
        }

        override fun onArtworkHint(
            playback: Playback,
            shouldShow: Boolean,
            position: Long,
            state: Int
        ) {
            if (!shouldShow) {
                binding!!.thumbnail.animate()
                    .alpha(0F)
                    .setDuration(200)
                    .withEndAction {
                        binding!!.thumbnail.isVisible = false
                    }
                    .start()
            } else {
                binding!!.thumbnail.alpha = 1F
                binding!!.thumbnail.animate()
                    .alpha(1F)
                    .setDuration(100)
                    .withEndAction {
                        binding!!.thumbnail.isVisible = true
                    }
                    .start()
            }
        }

        override fun onClick(p0: View?) {
            ViewCompat.setTransitionName(binding?.playerView!!,"item_view")
            binding?.listener?.onVideoClick(absoluteAdapterPosition,rebinder!!,binding?.playerView!!)
           // binding.listener.onItemClick(v!!, null, adapterPosition, itemId, rebinder)
        }

    }

    class WithPictRedditViewHolder(itemView: View) : MViewHolder(itemView) {
        var binding: SubredditPictLayoutViewBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }
    class WithLinkRedditViewHolder(itemView: View):MViewHolder(itemView){
        var binding:WithOutLinkItemBinding?=null
        init {
            binding=DataBindingUtil.bind(itemView)
        }
    }

    class YoutubeViewHolder(containerView: View) : MViewHolder(containerView),YouTubePlayerListener {
        var seconds=AtomicLong(0)
        var binding: SubredditLayoutYoutubeBinding? = null
        var callBack:MCallBack?=null
        var ready:Boolean=true
        var videoPlayer:YouTubePlayerView?=null

        init {
            binding = DataBindingUtil.bind(containerView)
        }
        fun initYoutube(videoPlayer:YouTubePlayerView,transit:(NavDirections)->Unit){
            if(ready){
                ready=false
                this.videoPlayer=videoPlayer
                val view=binding!!.youtubePlayer
                view.addView(videoPlayer)
                videoPlayer.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        val defaultPlayerUiController =
                            DefaultPlayerUiController(videoPlayer, youTubePlayer)
                        val muteButton = MButton(videoPlayer.context)
                        muteButton.setOnClickListener {
                            if (muteButton.isOff) {
                                youTubePlayer.unMute()
                                muteButton.isOff = false
                            } else {
                                muteButton.isOff = true
                                youTubePlayer.mute()
                            }
                        }
                        val fullScreenButton =
                            defaultPlayerUiController.rootView.findViewById<ImageView>(R.id.fullscreen_button)
                        fullScreenButton.setOnClickListener {
                                val direction= binding?.item?.permalink?.let { it1 ->
                                    binding!!.item!!.youtubeId?.let { it2 ->
                                        NewsFragmentDirections.actionNewsFragmentToYoutubeFragment(
                                            it1,seconds.toLong(), it2
                                        )
                                    }
                                }
                                if (direction != null) {
                                    transit.invoke(direction)
                                }
                                fullScreenButton.setImageDrawable(
                                    AppCompatResources.getDrawable(
                                        fullScreenButton.context,
                                        R.drawable.ic_baseline_fullscreen_24
                                    )
                                )
                        }
                        binding?.commentButton?.setOnClickListener {
                            val direction= binding?.item?.permalink?.let { it1 ->
                                binding!!.item!!.youtubeId?.let { it2 ->
                                    NewsFragmentDirections.actionNewsFragmentToYoutubeFragment(
                                        it1,seconds.toLong(), it2
                                    )
                                }
                            }
                            if (direction != null) {
                                transit.invoke(direction)
                            }
                            fullScreenButton.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    fullScreenButton.context,
                                    R.drawable.ic_baseline_fullscreen_24
                                )
                            )
                        }
                        val newId = View.generateViewId()
                        muteButton.id = newId
                        defaultPlayerUiController.addView(muteButton)
                        videoPlayer.setCustomPlayerUi(defaultPlayerUiController.rootView)
                        val item=binding?.item
                        youTubePlayer.addListener(this@YoutubeViewHolder)
                        if (item?.youtubeId != null) {
                            youTubePlayer.loadVideo(item.youtubeId!!, 0f)
                            binding!!.tumbtail.visibility=View.GONE
                        }
                    }
                }, IFramePlayerOptions.Builder().controls(0).build())
            }
        }
        fun onDetached(){
            binding!!.tumbtail.visibility=View.VISIBLE
            videoPlayer?.release()
            binding!!.youtubePlayer.removeView(videoPlayer)

            videoPlayer=null
            ready=true
        }

        override fun onApiChange(youTubePlayer: YouTubePlayer) {

        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            seconds.getAndSet(second.toLong())
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {

        }

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlayerConstants.PlaybackQuality
        ) {

        }

        override fun onPlaybackRateChange(
            youTubePlayer: YouTubePlayer,
            playbackRate: PlayerConstants.PlaybackRate
        ) {

        }

        override fun onReady(youTubePlayer: YouTubePlayer) {

        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState
        ) {

        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {

        }

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {

        }

        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {

        }

    }
    class LoadingViewHolder(containerView: View):MViewHolder(containerView)

}