package ru.skillbox.humblr.data.interfaces

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kohii.v1.core.Rebinder
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.CoroutineScope
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.entities.SubReddit
import ru.skillbox.humblr.utils.MButton
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.MImageView
import ru.skillbox.humblr.utils.MTextView
import ru.skillbox.humblr.utils.adapters.MViewHolder

interface MListener {
    fun onClick(view: View,item: Link)
  /*  fun onLongClick(item: Link):Boolean
    fun onFullScreenMode(view :View)*/
    fun onPict(view:View,link:String)
    fun onText(view:View,link:String)
    fun getKohii():Kohii
    fun shouldRebindVideo(rebinder: Rebinder?):Boolean
    fun onVideoClick(position:Int,rebinder: Rebinder,view:View)
    fun onMute(imageView: MImageView)
    fun isMuted():Boolean
    fun subscribe(holder: MViewHolder.YoutubeViewHolder)
    fun removeSubscription(index:Int)
    fun getScope():CoroutineScope
    fun onJoin(view:MControllerView,subredditName:String,textView:MTextView)
    fun navigateToUser(user:String)

}