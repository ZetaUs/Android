package com.zztx.shop

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_1)
        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main2)
        val logoView = findViewById<ImageView>(R.id.imageView3)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rootView.alpha = 0f
        logoView.alpha = 0f

        rootView.post {
            rootView.animate()
                .alpha(1f)
                .setDuration(500)
                .start()

            logoView.animate()
                .alpha(1f)
                .setStartDelay(120)
                .setDuration(700)
                .start()

            rootView.postDelayed({
                val intent = Intent(this@MainActivity, Page2Activity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this@MainActivity,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                startActivity(intent, options.toBundle())
                finish()
            }, 1800)
        }
    }
}