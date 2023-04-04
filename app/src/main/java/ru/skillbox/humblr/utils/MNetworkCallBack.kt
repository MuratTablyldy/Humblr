package ru.skillbox.humblr.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

class MNetworkCallBack constructor(
    private val context: Context,
    private val onState: (Boolean) -> Unit
) : ConnectivityManager.NetworkCallback(), DefaultLifecycleObserver {
    private lateinit var networkRequest: NetworkRequest

    val isAval = AtomicBoolean(true)
    fun init() {
        networkRequest =
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        init()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        register()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        unregister()
    }

    fun register() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)

    }

    fun unregister() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)

    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onState.invoke(true)
        isAval.set(true)
    }

    fun isAvailable(): Boolean {
        return isAval.get()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onState.invoke(false)
        isAval.set(false)
    }
}