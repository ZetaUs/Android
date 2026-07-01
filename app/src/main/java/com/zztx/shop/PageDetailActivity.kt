package com.zztx.shop

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load

class PageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_detail)

        // 获取传递过来的商品数据
        val goods = intent.getParcelableExtra<Product>("goods_data")
        goods?.run {
            val ivImg = findViewById<ImageView>(R.id.ivDetailImg)
            val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
            val tvTag = findViewById<TextView>(R.id.tvDetailTag)
            val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
            val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)

            ivImg.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            tvTitle.text = title
            tvTag.text = tag
            tvPrice.text = price
            tvDesc.text = subtitle
        }
    }
}