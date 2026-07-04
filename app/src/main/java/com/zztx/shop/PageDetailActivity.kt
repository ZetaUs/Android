package com.zztx.shop

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.google.gson.Gson

class PageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_detail)

        val coordinator = findViewById<CoordinatorLayout>(R.id.detailCoordinator)
        val contentRoot = findViewById<LinearLayout>(R.id.nestedContentRoot) ?: coordinator
        val defaultCoordPaddingLeft = coordinator.paddingLeft
        val defaultCoordPaddingTop = coordinator.paddingTop
        val defaultCoordPaddingRight = coordinator.paddingRight
        val defaultCoordPaddingBottom = coordinator.paddingBottom
        val contentDefaultPaddingBottom = contentRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(coordinator) { v, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = systemBar.bottom.coerceAtLeast(ime.bottom)
            v.setPadding(
                defaultCoordPaddingLeft + systemBar.left,
                defaultCoordPaddingTop + systemBar.top,
                defaultCoordPaddingRight + systemBar.right,
                defaultCoordPaddingBottom + bottomInset
            )
            if (contentRoot !== coordinator) {
                contentRoot.setPadding(
                    contentRoot.paddingLeft,
                    contentRoot.paddingTop,
                    contentRoot.paddingRight,
                    contentDefaultPaddingBottom + bottomInset
                )
            }
            insets
        }

        // 顶栏返回 / 分享 / 更多
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShare).setOnClickListener {
            Toast.makeText(this, "分享功能开发中…", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnMore).setOnClickListener {
            Toast.makeText(this, "更多菜单开发中…", Toast.LENGTH_SHORT).show()
        }

        // 底部：客服 / 收藏 / 分享 + 加入购物车 / 立即购买
        findViewById<View>(R.id.btnService).setOnClickListener {
            Toast.makeText(this, "客服功能开发中…", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnFavorite).setOnClickListener {
            Toast.makeText(this, "收藏功能开发中…", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnBottomShare).setOnClickListener {
            Toast.makeText(this, "分享功能开发中…", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnAddCart).setOnClickListener {
            Toast.makeText(this, "已加入购物车（占位）", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnBuyNow).setOnClickListener {
            Toast.makeText(this, "正在跳转支付…（占位）", Toast.LENGTH_SHORT).show()
        }

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
