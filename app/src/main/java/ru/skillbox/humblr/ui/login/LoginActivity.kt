package ru.skillbox.humblr.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.repositories.modules.TokenHolder
import ru.skillbox.humblr.databinding.ActivityLoginBinding

import kotlin.system.exitProcess


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityLoginBinding

    private var activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            AlertDialog.Builder(this).setMessage(R.string.alert_title).setPositiveButton(R.string.pos_button) { dialog,_ ->
                dialog.cancel()
            }.setNegativeButton(R.string.neg_button){
                _,_->
                exitProcess(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()
    }
    fun openLoginPage(){
        viewModel.openLoginPage()
    }
    fun isFirstTimeLoad():Boolean{
        return viewModel.isFirstTime()
    }
    fun setFirstTimeLoad(){
        viewModel.setFirst()
    }


    private fun bind() {
        viewModel.openAuthPageEvent.observe(this, ::openAuthPage)
        viewModel.isRegistered.observe(this, ::onRegistered)
    }

    fun onRegistered(tokenHolder: TokenHolder) {
        viewModel.startWorker(
            tokenHolder.access_token!!,
            tokenHolder.expires_in!!.toLong(),
            tokenHolder.token_type!!,
            this
        )
        setResult(RESULT_OK)
        finish()
    }

    private fun openAuthPage(uri: Uri) {
        viewModel.hasToken=false
        val intent = Intent(this, CustomTabActivity::class.java)
        intent.putExtra(CustomTabActivity.URI, uri.toString())
        activityResultLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.hasToken) {
            viewModel.hasToken=true
            val fragment = intent.data?.fragment ?: return
            viewModel.handleFragment(fragment)
        }
    }
}