package ru.skillbox.humblr.mainPackage

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.Rebinder
import kotlinx.coroutines.InternalCoroutinesApi
import ru.skillbox.humblr.databinding.MainActivityBinding
import ru.skillbox.humblr.news.NewsFragmentDirections
import ru.skillbox.humblr.ui.login.LoginActivity
import ru.skillbox.humblr.utils.MNetworkCallBack
import ru.skillbox.humblr.utils.dp
import ru.skillbox.humblr.R
import ru.skillbox.humblr.utils.CallBack

@InternalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: MainActivityBinding? = null
    private val binding: MainActivityBinding
        get() = _binding!!
    val viewModel: MainViewModel by viewModels()
    lateinit var networkCallback:MNetworkCallBack
    private var currentCallback:CallBack?=null
    companion object{
        var currentPosition=0
    }

    private var activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.fetchAuthToken()
    }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(layoutInflater)
        val sharedPreff=getSharedPreferences("night",0)
        val isNight=sharedPreff?.getBoolean("night_mode",false)
        if(isNight==true){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        viewModel.fetchAuthToken()
        setContentView(binding.root)
        bind()
        networkCallback = MNetworkCallBack(this) {
            Handler(Looper.getMainLooper()).post {
                if(viewModel.internetAvailable.value!=it)
                    viewModel.internetAvailable.postValue(it)
            }
        }
        lifecycle.addObserver(networkCallback)

        val navController = supportFragmentManager.findFragmentById(R.id.fragment_main)?.findNavController()
        NavigationUI.setupWithNavController(binding.navBottomMenu,navController!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    fun internetStatus():Boolean {
        return viewModel.internetAvailable.value ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    fun navigateToLinkFragment(link:String){
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        val direction=NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(link,null)
        findNavController(R.id.fragment_main).navigate(direction)
    }
    fun navigateToTextFragment(link:String){
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        val direction=NewsFragmentDirections.actionNewsFragmentToDetailTextFragment(link,null,null)
        findNavController(R.id.fragment_main).navigate(direction)
    }
    fun navigateToPictFragment(link:String){
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        val direction=NewsFragmentDirections.actionNewsFragmentToDetainFragment(link,null)
        findNavController(R.id.fragment_main).navigate(direction)
    }
    fun navigateToYoutubeFragment(direction: NavDirections){
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        findNavController(R.id.fragment_main).navigate(direction)
    }
    fun navigateToYoutubeFragment(link: String, id: String, time: Long) {
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        val direction = NewsFragmentDirections.actionNewsFragmentToYoutubeFragment(
            link = link,
            time = time,
            id = id
        )
        findNavController(R.id.fragment_main).navigate(direction)
    }
    fun navigateToRedditVideoFragment(rebinder: Rebinder,pos:Int, link:String){
        findNavController(R.id.fragment_main).navigate(R.id.main_nav_graph)
        val direction=NewsFragmentDirections.actionNewsFragmentToFullScreenFragment(rebinder,pos,link)
        findNavController(R.id.fragment_main).navigate(direction)
    }

    @InternalCoroutinesApi
    fun bind() {
        viewModel.apply {
            tokenHold.observe(this@MainActivity) {
                if(it!=null){
                    if (!viewModel.isTokenValid(it)) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        activityResultLauncher.launch(intent)
                    }
                } else{
                    if (currentCallback!=null){
                        currentCallback!!.invoke()
                        currentCallback=null
                    }
                }
            }
            internetAvailable.observe(this@MainActivity) { available ->
                if (available) {
                    closeNoNetworkView()
                } else {
                    openNoNetworkView()
                }
            }
        }
    }
    fun onTokenExpired(callBack: CallBack){
        this.currentCallback=callBack
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    override fun onStart() {
        super.onStart()
    }
    fun goToLogin(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        activityResultLauncher.launch(intent)
    }


    private fun closeNoNetworkView() {
        val animator = ValueAnimator.ofFloat(-20.dp.toFloat(), 0f)
        binding.networkView.setBackgroundColor(resources.getColor(R.color.green, null))
        binding.connection.text = resources.getText(R.string.connection_restored)
        animator.apply {
            duration = 1000
            addUpdateListener {
                binding.networkView.translationY = (it.animatedValue as Float)
            }
        }
        animator.start()
    }

    private fun openNoNetworkView() {
        val animator = ValueAnimator.ofFloat(0f, -20.dp.toFloat())
        binding.networkView.setBackgroundColor(resources.getColor(R.color.red, null))
        binding.connection.text = resources.getText(R.string.network_not_available)
        animator.apply {
            duration = 1000
            addUpdateListener {
                binding.networkView.translationY = (it.animatedValue as Float)
            }
        }
        animator.start()
    }


    fun setBarNoVisibility() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.apply {
            systemUiVisibility=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        _binding?.navBottomMenu?.visibility = View.GONE
    }

    fun setBackGroundActBarColor(@ColorInt colorRes: Int) {
        window.statusBarColor = colorRes
    }

    fun setBarVisible() {
        _binding?.navBottomMenu?.visibility = View.VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility=View.VISIBLE
    }
}