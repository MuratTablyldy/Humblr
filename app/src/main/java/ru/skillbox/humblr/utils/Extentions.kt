package ru.skillbox.humblr.utils

import android.content.res.Resources.getSystem
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

val Int.dp: Int get() = (this * getSystem().displayMetrics.density).toInt()
/*fun <T,R> Flow<T>.toPair(second: Flow<R>): Flow<Pair<T, R>> =
    this.combine(second) { firstC: T, secondC: R -> firstC to secondC }*/

@ExperimentalCoroutinesApi
fun EditText.subscribeFlow(): Flow<String> {
    return callbackFlow {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                trySendBlocking(s.toString()).onFailure { exception-> Log.d(this.javaClass.toString(),"$exception") }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }
        this@subscribeFlow.addTextChangedListener(watcher)
        awaitClose {
            this@subscribeFlow.removeTextChangedListener(watcher)
        }
    }
}
fun SearchView.subscribeFlow():Flow<String>{
    return callbackFlow {
        val listener=object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                trySendBlocking(query.toString()).onFailure { exception-> Log.d(this.javaClass.toString(),"$exception") }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                trySendBlocking(query.toString()).onFailure { exception-> Log.d(this.javaClass.toString(),"$exception") }
                return false
            }
        }
        this@subscribeFlow.setOnQueryTextListener(listener)
        awaitClose {
            }
    }
}
