package com.zztx.shop

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.tencent.mm.opensdk.modelpay.PayReq
import java.util.concurrent.Executors

class PageDetailActivity : AppCompatActivity() {
    private val payExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentGoods: Product? = null

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
        findViewById<View>(R.id.btnBuyNow).setOnClickListener { v ->
            val goods = currentGoods ?: run {
                Toast.makeText(this, "商品数据异常，无法发起支付", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val reason = WxPayConstants.checkOrGetReason()
            if (reason != null) {
                Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val appId = WxPayConstants.WX_APP_ID
            val amountFen = WxPayHelper.parsePriceToFen(goods.price)
            if (amountFen <= 0) {
                Toast.makeText(this, "商品金额解析失败（${goods.price}），无法发起支付", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "正在请求支付参数…", Toast.LENGTH_SHORT).show()
            payExecutor.execute {
                val (params, err) = WxPayHelper.requestPrepay(
                    prepayUrl = WxPayConstants.PREPAY_URL,
                    appId = appId,
                    goods = goods,
                    amountFen = amountFen,
                    notifyUrl = WxPayConstants.PAY_NOTIFY_URL.ifBlank { null }
                )
                mainHandler.post {
                    if (params == null) {
                        Toast.makeText(this, err ?: "预下单失败", Toast.LENGTH_LONG).show()
                        return@post
                    }
                    val api = WxApiHolder.get(this, appId)
                    val payReq = PayReq()
                    payReq.fillFrom(params, appId)
                    val sent = runCatching { api.sendReq(payReq) }.getOrDefault(false)
                    if (!sent) {
                        Toast.makeText(this, "拉起微信失败，请检查是否安装微信或 AppID 是否匹配", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val jsonStr = intent.getStringExtra("goods_json")
        val goods: Product? = Gson().fromJson(jsonStr, Product::class.java)
        currentGoods = goods

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
