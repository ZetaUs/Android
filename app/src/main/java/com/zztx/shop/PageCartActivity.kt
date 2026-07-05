package com.zztx.shop

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class PageCartActivity : AppCompatActivity() {
    private lateinit var adapter: CartAdapter

    @SuppressLint("SourceLockedOrientationActivity", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_cart)

        val coordinator = findViewById<CoordinatorLayout>(R.id.cartCoordinator)
        val rv = findViewById<RecyclerView>(R.id.rvCart)
        val empty = findViewById<View>(R.id.layoutCartEmpty)
        val cbAll = findViewById<CheckBox>(R.id.cbCartSelectAll)
        val tvTotal = findViewById<TextView>(R.id.tvCartTotal)
        val tvHint = findViewById<TextView>(R.id.tvCartSelectedHint)
        val btnCheckout = findViewById<TextView>(R.id.btnCheckout)
        val btnClear = findViewById<TextView>(R.id.btnCartClear)

        val defaultCoordPL = coordinator.paddingLeft
        val defaultCoordPT = coordinator.paddingTop
        val defaultCoordPR = coordinator.paddingRight
        val defaultCoordPB = coordinator.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(coordinator) { v, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                defaultCoordPL + systemBar.left,
                defaultCoordPT + systemBar.top,
                defaultCoordPR + systemBar.right,
                defaultCoordPB + systemBar.bottom
            )
            insets
        }

        findViewById<View>(R.id.btnCartBack).setOnClickListener { finish() }

        adapter = CartAdapter(
            onCheckedChanged = { item, checked ->
                CartManager.setChecked(this, item.id, checked)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
            },
            onPlus = { item ->
                CartManager.updateQuantity(this, item.id, item.quantity + 1)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
            },
            onMinus = { item ->
                CartManager.updateQuantity(this, item.id, item.quantity - 1)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
            },
            onDelete = { item ->
                CartManager.remove(this, item.id)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
            },
            onClickItem = { item ->
                val json = Gson().toJson(item.product)
                val intent = Intent(this, PageDetailActivity::class.java)
                intent.putExtra("goods_json", json)
                startActivity(intent)
            }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        cbAll.setOnCheckedChangeListener { _, isChecked ->
            if (cbAll.isPressed) {
                CartManager.setAllChecked(this, isChecked)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
            }
        }

        btnClear.setOnClickListener {
            val checked = CartManager.getCheckedItems(this)
            if (checked.isEmpty()) {
                Toast.makeText(this, "暂无已选商品", Toast.LENGTH_SHORT).show()
            } else {
                CartManager.clearChecked(this)
                renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
                Toast.makeText(this, "已移除 ${checked.size} 件已选商品", Toast.LENGTH_SHORT).show()
            }
        }

        btnCheckout.setOnClickListener {
            val items = CartManager.getCheckedItems(this)
            if (items.isEmpty()) {
                Toast.makeText(this, "请先选择要结算的商品", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(
                this,
                "购物车批量结算开发中…（请从商品详情页单独下单）",
                Toast.LENGTH_LONG
            ).show()
        }

        renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
    }

    override fun onResume() {
        super.onResume()
        val rv = findViewById<RecyclerView>(R.id.rvCart)
        val empty = findViewById<View>(R.id.layoutCartEmpty)
        val cbAll = findViewById<CheckBox>(R.id.cbCartSelectAll)
        val tvTotal = findViewById<TextView>(R.id.tvCartTotal)
        val tvHint = findViewById<TextView>(R.id.tvCartSelectedHint)
        val btnCheckout = findViewById<TextView>(R.id.btnCheckout)
        renderAll(rv, empty, cbAll, tvTotal, tvHint, btnCheckout)
    }

    @SuppressLint("SetTextI18n")
    private fun renderAll(
        rv: RecyclerView,
        empty: View,
        cbAll: CheckBox,
        tvTotal: TextView,
        tvHint: TextView,
        btnCheckout: TextView
    ) {
        val items = CartManager.getAll(this)
        adapter.submitList(items.toList())
        empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        rv.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        cbAll.isChecked = CartManager.isAllChecked(this)
        tvTotal.text = CartManager.getCheckedTotalYuan(this)
        val checkedCount = CartManager.getCheckedCount(this)
        tvHint.text = "已选 $checkedCount 件"
        btnCheckout.isEnabled = checkedCount > 0
        btnCheckout.alpha = if (checkedCount > 0) 1f else 0.55f
    }
}
