package com.zztx.shop

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Bundle
import android.os.Looper

import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class Page2Activity : AppCompatActivity() {

    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val currentProducts = mutableListOf<Product>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_2)

        val root = findViewById<LinearLayout>(R.id.main2)
        val productsStatus = findViewById<TextView>(R.id.tvProductsStatus)
        val productsList = findViewById<RecyclerView>(R.id.rvProducts)
        val adapter = ProductAdapter()

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

        loadProducts(adapter, productsStatus)
    }

    @SuppressLint("SetTextI18n")
    private fun loadProducts(adapter: ProductAdapter, statusView: TextView) {
        statusView.text = getString(R.string.loading_products)
        networkExecutor.execute {
            try {
                val products = fetchProductsFromCloudflareKv()
                mainHandler.post {
                    adapter.submitList(products)
                    currentProducts.clear()
                    currentProducts.addAll(products)
                    statusView.text = "已加载 ${products.size} 件云端商品"
                }
            } catch (_: Exception) {
                mainHandler.post {
                    statusView.text = getString(R.string.load_products_failed)
                    adapter.submitList(
                        listOf(
                            Product("云端商品", "请检查 KV 接口是否可访问", "--", "错误"),
                            Product("接口配置", "将 URL 替换为你的 Worker 地址", "--", "提示")
                        )
                    )
                }
            }
        }
    }

    private fun fetchProductsFromCloudflareKv(): List<Product> {
        val endpoint = getString(R.string.cloudflare_kv_products_url)
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }

        return try {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            parseProducts(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProducts(jsonText: String): List<Product> {
        val trimmed = jsonText.trim()
        val array = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> JSONObject(trimmed).optJSONArray("products") ?: JSONArray()
        }
        // 支持两种返回格式：
        // 1) JSON 数组：[{"title":"..","subtitle":"..",...}, ...]
        // 2) JSON 对象的 products 数组：{"products":[...]} （上面已经处理）
        // 3) KV 的 key->value 映射：{"<imageUrl>": "<productName>", ...}
        val products = mutableListOf<Product>()

        // 如果 array 有内容，按数组解析每项对象
        if (array.length() > 0) {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                products.add(
                    Product(
                        title = item.optString("title", "未命名商品"),
                        subtitle = item.optString("subtitle", item.optString("description", "云端精选商品")),
                        price = item.optString("price", "¥0"),
                        tag = item.optString("tag", "推荐"),
                        accent = item.optString("accent", "#FEF3C7")
                    )
                )
            }
            return products
        }

        // 如果不是数组，尝试把根对象当成 key->value 映射解析（Cloudflare KV 常见格式）
        try {
            val obj = JSONObject(trimmed)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = obj.optString(key)
                // 把 value 当作商品名 (title)，把 key（通常为图片 URL）放到 subtitle 暂存
                products.add(Product(title = value.ifEmpty { "未命名商品" }, subtitle = key, price = "--", tag = "推荐", accent = "#FEF3C7"))
            }
        } catch (_: Exception) {
            // 忽略解析错误，返回空列表
        }

        return products
    }
}