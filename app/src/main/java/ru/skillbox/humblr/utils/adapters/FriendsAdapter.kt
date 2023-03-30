package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import ru.skillbox.humblr.data.entities.Account
import ru.skillbox.humblr.databinding.FriendItemBinding

class FriendsAdapter(val onClick: (accountName: String) -> Unit) :
    Adapter<FriendsAdapter.FriendsViewHolder>() {

    var friends = mutableListOf<Account>()

    fun addFriend(friend: Account) {
        friends.add(0, friend)
        notifyItemInserted(0)
    }

    fun addFriends(list: List<Account>) {
        friends.addAll(0, list)
        notifyItemRangeChanged(0, list.lastIndex)
    }

    fun replace(account: Account) {
        val index = friends.indexOf(account)
        friends.remove(account)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FriendItemBinding.inflate(inflater, parent, false)
        return FriendsViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = friends[position]
        holder.binding?.item = friend
        holder.binding!!.root.setOnClickListener {
            onClick.invoke(friend.name!!)
        }
        val avatarView = holder.binding!!.avatar
        Glide.with(avatarView).load(friend.icon).into(avatarView)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: FriendItemBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }
}