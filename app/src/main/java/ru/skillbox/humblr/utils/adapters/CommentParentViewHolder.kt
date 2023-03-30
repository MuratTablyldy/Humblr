package ru.skillbox.humblr.utils.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil

import androidx.recyclerview.widget.RecyclerView
import ru.skillbox.humblr.databinding.CommentViewBinding
import ru.skillbox.humblr.databinding.CommentViewReplyBinding
import ru.skillbox.humblr.databinding.EmptyViewBinding
import ru.skillbox.humblr.databinding.LoadingViewBinding

sealed class CommentParentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    class CommentViewHolderViewHolder(itemView: View):CommentParentViewHolder(itemView){
        var binding: CommentViewBinding? = null
        init {
            binding = CommentViewBinding.bind(itemView)
        }
    }
    class EmptyEndView(itemView: View):CommentParentViewHolder(itemView){
        var binding:EmptyViewBinding?=null
        init {
            binding= EmptyViewBinding.bind(itemView)
        }
    }
    class LoadingViewHolder(itemView: View):CommentParentViewHolder(itemView){
        var binding:LoadingViewBinding?=null
        init{
            binding=LoadingViewBinding.bind(itemView)
        }
    }
}