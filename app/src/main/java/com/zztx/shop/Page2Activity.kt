package com.zztx.shop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
    private val TAG = "SHOP_API_DEBUG"
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val currentProducts = mutableListOf<Product>()
    // 修复1：lateinit显式标注类型 ProductAdapter
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
        val btnFilter = findViewById<TextView>(R.id.btnFilter)
        val tvMore = findViewById<TextView>(R.id.tvMore)
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

        // 搜索过滤
        fun hideKeyboard() {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        }
        fun applySearchFilter() {
            val keyword = etSearch.text.toString().trim()
            val filterList: List<Product> = if (keyword.isEmpty()) {
                currentProducts.toList()
            } else {
                currentProducts.filter { item ->
                    item.title.contains(keyword, ignoreCase = true) ||
                            item.subtitle.contains(keyword, ignoreCase = true) ||
                            item.tag.contains(keyword, ignoreCase = true)
                }
            }
            adapter.submitList(filterList)
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applySearchFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etSearch.setOnEditorActionListener { v, actionId, event ->
            val isActionSearch = when (actionId) {
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_UNSPECIFIED -> true
                else -> false
            }
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    (event.action == KeyEvent.ACTION_UP || event.action == KeyEvent.ACTION_DOWN)
            if (isActionSearch || isEnterKey) {
                applySearchFilter()
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // 筛选按钮占位
        btnFilter.setOnClickListener {
            Toast.makeText(this, "筛选功能开发中…", Toast.LENGTH_SHORT).show()
        }

        // 查看更多跳转（修复：先创建Page3Activity再使用）
        tvMore.setOnClickListener {
            val jumpIntent = Intent(this, Page3Activity::class.java)
            startActivity(jumpIntent)
        }

        loadProducts(productsStatus, ::applySearchFilter)
    }

    @SuppressLint("SetTextI18n")
    private fun loadProducts(statusView: TextView, onDataReady: () -> Unit) {
        statusView.text = getString(R.string.loading_products)
        networkExecutor.execute {
            try {
                val products = fetchProductsFromCloudflareKv()
                Log.d(TAG, "商品加载成功，数量：${products.size}")
                mainHandler.post {
                    currentProducts.clear()
                    currentProducts.addAll(products)
                    onDataReady()
                    statusView.text = "已加载 ${products.size} 件云端商品"
                }
            } catch (e: Exception) {
                Log.e(TAG, "请求/解析异常", e)
                mainHandler.post {
                    statusView.text = getString(R.string.load_products_failed)
                    val fallback = mutableListOf(
                        Product("未知商品", "用于验证搜索是否命中（接口加载失败兜底）", "--", "未知"),
                        Product("云端商品", "接口加载失败", "--", "错误"),
                        Product("检查项", "确认Worker正常部署", "--", "提示")
                    )
                    currentProducts.clear()
                    currentProducts.addAll(fallback)
                    adapter.submitList(fallback.toList())
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
        val trimmed = jsonText.trim()

        fun collectFrom(jsonArr: JSONArray) {
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
        }

        // 优先按 JSONObject 解包装：goods / products / items / shops / data / list / result
        if (trimmed.startsWith("{")) {
            try {
                val root = JSONObject(trimmed)
                val wrapperKeys = listOf(
                    "goods", "products", "items", "shops",
                    "data", "list", "result"
                )
                Log.d(TAG, "parseProducts 顶层为 JSONObject，keys=${root.keys().asSequence().toList()}")
                var picked: JSONArray? = null
                for (key in wrapperKeys) {
                    val candidate = root.opt(key)
                    if (candidate is JSONArray) { picked = candidate; break }
                }
                // data 下再套一层 goods/products 的情况
                if (picked == null) {
                    val data = root.opt("data")
                    if (data is JSONObject) {
                        for (key in wrapperKeys) {
                            val candidate = data.opt(key)
                            if (candidate is JSONArray) { picked = candidate; break }
                        }
                    }
                }
                if (picked != null) {
                    collectFrom(picked)
                    Log.d(TAG, "解析命中包装数组，数量=${products.size}")
                    return products
                }
                // 实在找不到数组，抛出异常走 fallback
                throw Exception("JSONObject 中未找到商品数组字段（仅包含：${root.keys().asSequence().toList()}）")
            } catch (e: Exception) {
                throw e
            }
        }

        // 顶层直接是数组
        val jsonArr = JSONArray(trimmed)
        collectFrom(jsonArr)
        return products
    }
}