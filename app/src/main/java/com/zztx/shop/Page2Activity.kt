package com.zztx.shop

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class Page2Activity : AppCompatActivity() {
    private val TAG = "SHOP_API_DEBUG"
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val currentProducts = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    @SuppressLint("MissingInflatedId", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_2)

        val root = findViewById<LinearLayout>(R.id.main2)
        val productsStatus = findViewById<TextView>(R.id.tvProductsStatus)
        val productsList = findViewById<RecyclerView>(R.id.rvProducts)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        adapter = ProductAdapter()

        productsList.layoutManager = GridLayoutManager(this, 2)
        productsList.adapter = adapter
        root.alpha = 0f
        root.translationY = 24f
        val defaultPaddingLeft = root.paddingLeft
        val defaultPaddingTop = root.paddingTop
        val defaultPaddingRight = root.paddingRight
        val defaultPaddingBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                defaultPaddingLeft + systemBar.left,
                defaultPaddingTop + systemBar.top,
                defaultPaddingRight + systemBar.right,
                defaultPaddingBottom + systemBar.bottom
            )
            insets
        }

        root.post {
            root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(420)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                val filterList: List<Product> = if (keyword.isEmpty()) {
                    currentProducts
                } else {
                    currentProducts.filter { item ->
                        item.title.contains(keyword, ignoreCase = true) ||
                                item.subtitle.contains(keyword, ignoreCase = true) ||
                                item.tag.contains(keyword, ignoreCase = true)
                    }
                }
                adapter.submitList(filterList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadProducts(productsStatus)
    }

    @SuppressLint("SetTextI18n")
    private fun loadProducts(statusView: TextView) {
        statusView.text = getString(R.string.loading_products)
        networkExecutor.execute {
            try {
                val products = fetchProductsFromCloudflareKv()
                Log.d(TAG, "商品加载成功，数量：${products.size}")
                mainHandler.post {
                    adapter.submitList(products)
                    currentProducts.clear()
                    currentProducts.addAll(products)
                    statusView.text = "已加载 ${products.size} 件云端商品"
                }
            } catch (e: Exception) {
                Log.e(TAG, "请求/解析异常", e)
                mainHandler.post {
                    statusView.text = getString(R.string.load_products_failed)
                    adapter.submitList(
                        mutableListOf<Product>(
                            Product("云端商品", "接口加载失败", "--", "错误"),
                            Product("检查项", "确认Worker正常部署", "--", "提示")
                        )
                    )
                }
            }
        }
    }

    private fun fetchProductsFromCloudflareKv(): List<Product> {
        val endpoint = getString(R.string.cloudflare_kv_products_url)
        Log.d(TAG, "请求地址：$endpoint")
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", "Mozilla/5.0 Android Client")
        }

        return try {
            val code = connection.responseCode
            Log.d(TAG, "接口响应码：$code")
            if (code != 200) throw Exception("响应码异常 $code")

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "返回JSON：$responseText")
            parseProducts(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProducts(jsonText: String): List<Product> {
        val products = mutableListOf<Product>()
        val jsonArr = JSONArray(jsonText.trim())

        for (i in 0 until jsonArr.length()) {
            val item = jsonArr.optJSONObject(i) ?: continue
            products.add(
                Product(
                    title = item.optString("title", "未命名商品"),
                    subtitle = item.optString("desc", "云端精选商品"),
                    price = "¥" + item.optString("price", "0"),
                    tag = item.optString("category", "推荐"),
                    accent = item.optString("accent", "#FEF3C7"),
                    imageUrl = item.optString("imageUrl", item.optString("img", ""))
                )
            )
        }
        return products
    }
}