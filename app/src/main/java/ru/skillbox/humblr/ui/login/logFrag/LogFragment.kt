package ru.skillbox.humblr.ui.login.logFrag

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.skillbox.humblr.R
import ru.skillbox.humblr.ui.login.LoginActivity

class LogFragment:Fragment(R.layout.login_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button=view.findViewById<Button>(R.id.login)
        val activity = activity as LoginActivity
        if(activity.isFirstTimeLoad()){
            activity.setFirstTimeLoad()
            findNavController().navigate(LogFragmentDirections.actionLogFragmentToStarterFragment())
        }
        button.setOnClickListener {
            activity.openLoginPage()
        }
    }

}