package ru.skillbox.humblr.utils.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import kotlinx.coroutines.CoroutineScope
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.Created

class CommentFavoritesAdapter( coroutineScope: CoroutineScope, val commentHandler: CommentAdapter.CommentHandler):RecyclerView.Adapter<CommentParentViewHolder>() {
    private val differ = AsyncListDiffer(this, CommentDiffUtil())
    private val delegate = AdapterDelegatesManager<List<Created>>()
    init {
        delegate.addDelegate(CommentAdapter(coroutineScope, commentHandler))
            .addDelegate(LoadingCommentAdapter())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentParentViewHolder {
        return delegate.onCreateViewHolder(parent, viewType) as CommentParentViewHolder
    }

    override fun onBindViewHolder(holder: CommentParentViewHolder, position: Int) {
        delegate.onBindViewHolder(differ.currentList, position, holder)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    fun addComments(list:List<Comment>){
        differ.submitList(list)
    }
    fun getItem(index:Int):Created{
        return differ.currentList[index]
    }
    val list:List<Created>
    get() = differ.currentList
    fun removeLoad(){
        val mList = ArrayList(differ.currentList)
        mList.remove(Comment.LoadingComment)
        differ.submitList(mList)
    }


}