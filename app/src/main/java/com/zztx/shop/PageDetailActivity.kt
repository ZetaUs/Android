package com.zztx.shop

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
// 必须导入Gson
import com.google.gson.Gson

class PageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_detail)

        val jsonStr = intent.getStringExtra("goods_json")
        val goods: Product? = Gson().fromJson(jsonStr, Product::class.java)

        goods?.let {
            val ivImg = findViewById<ImageView>(R.id.ivDetailImg)
            val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
            val tvTag = findViewById<TextView>(R.id.tvDetailTag)
            val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
            val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)

            ivImg.load(it.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.stat_notify_error)
            }
            tvTitle.text = it.title
            tvTag.text = it.tag
            tvPrice.text = it.price
            tvDesc.text = it.subtitle
        }
    }
}