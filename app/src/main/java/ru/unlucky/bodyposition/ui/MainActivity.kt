package ru.unlucky.bodyposition.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import org.koin.android.ext.android.get
import ru.kpfu.health.body_position.HumanActivityEnum
import ru.unlucky.bodyposition.R
import ru.unlucky.bodyposition.databinding.ActivityMainBinding
import ru.unlucky.bodyposition.service.AppService

class MainActivity: MvpAppCompatActivity(), IMainActivity {
companion object {
    private val TAG = MainActivity::class.java.name
}
    private lateinit var binding: ActivityMainBinding

    private val presenter by moxyPresenter { get<MainPresenter>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lottieView.setAnimation(R.raw.standing)
    }

    override fun showBodyPosition(position: HumanActivityEnum) {
        binding.lottieView.cancelAnimation()
        val animRes = when (position) {
            HumanActivityEnum.BIKING, HumanActivityEnum.DOWNSTAIRS, HumanActivityEnum.UPSTAIRS, HumanActivityEnum.WALKING -> {
                Log.d(TAG, "showBodyPosition: walk")
                R.raw.walk
            }
            HumanActivityEnum.JOGGING ->{
                Log.d(TAG, "showBodyPosition: run")

                R.raw.run
            }
            HumanActivityEnum.SITTING -> {
                Log.d(TAG, "showBodyPosition: sitting")
                R.raw.sitting
            }
            HumanActivityEnum.STANDING ->{
                Log.d(TAG, "showBodyPosition: standing")
                R.raw.standing

            }
        }

        binding.lottieView.setAnimation(animRes)
        binding.lottieView.playAnimation()
    }

    override fun startAppService() {
        Log.d("MainActivity", "startAppService: ")
        startService(Intent(this, AppService::class.java))
    }
}