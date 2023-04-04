package ru.skillbox.humblr.data.interfaces

import android.view.View
import androidx.navigation.fragment.FragmentNavigator
import kohii.v1.core.Rebinder
import kohii.v1.exoplayer.Kohii
import kotlinx.coroutines.CoroutineScope
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.MImageView
import ru.skillbox.humblr.utils.MTextView
import ru.skillbox.humblr.utils.adapters.MViewHolder

interface MListener {
  fun onClick(view: View, item: Link)
  fun onPict(view: View, link: String)
  fun onText(view: View, link: String)
  fun getKohii(): Kohii
  fun shouldRebindVideo(rebinder: Rebinder?): Boolean
  fun onVideoClick(position: Int, rebinder: Rebinder, view: View)
  fun onMute(imageView: MImageView)
  fun isMuted(): Boolean
  fun subscribe(holder: MViewHolder.YoutubeViewHolder)
  fun removeSubscription(index: Int)
  fun getScope(): CoroutineScope
  fun onJoin(view: MControllerView, subredditName: String, textView: MTextView)
  fun navigateToUser(user: String)
  fun onPict2(extras: FragmentNavigator.Extras, link: String)
  fun onText2(view: View, link: Link.LinkText)
  fun onYoutube(link:String,id:String,second:Long)

}