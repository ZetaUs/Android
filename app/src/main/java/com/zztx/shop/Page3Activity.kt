package com.zztx.shop

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class Page3Activity : AppCompatActivity() {
    private val TAG = "SHOP_API_DEBUG"
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val currentProducts = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_3)

        // 绑定页面控件，id和main_2保持一致 rvProducts
        val productsStatus = findViewById<TextView>(R.id.tvProductsStatus)
        val productsList = findViewById<RecyclerView>(R.id.rvProducts)
        adapter = ProductAdapter()

        // 两列商品网格，和商城首页统一
        productsList.layoutManager = GridLayoutManager(this, 2)
        productsList.adapter = adapter

        // 加载云端商品
        loadProducts(productsStatus)
    }

    @SuppressLint("SetTextI18n")
    private fun loadProducts(statusView: TextView) {
        statusView.text = "云端商品正在加载..."
        networkExecutor.execute {
            try {
                val products = fetchProductsFromCloudflareKv()
                Log.d(TAG, "Page3商品加载成功，数量：${products.size}")
                mainHandler.post {
                    adapter.submitList(products)
                    currentProducts.clear()
                    currentProducts.addAll(products)
                    statusView.text = "已加载 ${products.size} 件云端商品"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Page3请求异常", e)
                mainHandler.post {
                    statusView.text = "商品加载失败，请重试"
                    adapter.submitList(
                        mutableListOf<Product>(
                            Product("加载失败", "接口请求异常", "--", "错误")
                        )
                    )
                }
            }
        }
    }

    // 复用同一个Worker商品接口
    private fun fetchProductsFromCloudflareKv(): List<Product> {
        val endpoint = getString(R.string.cloudflare_kv_products_url)
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", "Mozilla/5.0 Android Client")
        }
        return try {
            val code = connection.responseCode
            if (code != 200) throw Exception("响应码 $code")
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            parseProducts(responseText)
        } finally {
            connection.disconnect()
        }
    }

    // 和Page2完全相同的JSON解析逻辑，兼容img图片字段
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