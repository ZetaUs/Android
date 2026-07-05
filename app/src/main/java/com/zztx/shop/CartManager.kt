package com.zztx.shop

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private const val SP_NAME = "shop_cart_v1"
    private const val KEY_ITEMS = "items"
    private val gson = Gson()
    private val type = object : TypeToken<MutableList<CartItem>>() {}.type

    private fun load(context: Context): MutableList<CartItem> {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_ITEMS, null) ?: return mutableListOf()
        return runCatching { gson.fromJson<MutableList<CartItem>>(raw, type) }
            .getOrDefault(mutableListOf())
    }

    private fun save(context: Context, items: List<CartItem>) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, gson.toJson(items))
            .apply()
    }

    fun getAll(context: Context): MutableList<CartItem> = load(context)

    fun add(context: Context, product: Product, quantity: Int = 1): Int {
        val items = load(context)
        val id = CartItem.buildId(product)
        val existing = items.find { it.id == id }
        val newQty: Int
        if (existing != null) {
            existing.quantity += quantity
            newQty = existing.quantity
        } else {
            items.add(0, CartItem(id, product, quantity, true))
            newQty = quantity
        }
        save(context, items)
        return newQty
    }

    fun remove(context: Context, id: String) {
        val items = load(context).filterNot { it.id == id }.toMutableList()
        save(context, items)
    }

    fun updateQuantity(context: Context, id: String, quantity: Int) {
        val items = load(context)
        if (quantity <= 0) {
            save(context, items.filterNot { it.id == id }.toMutableList())
        } else {
            items.find { it.id == id }?.quantity = quantity
            save(context, items)
        }
    }

    fun setChecked(context: Context, id: String, checked: Boolean) {
        val items = load(context)
        items.find { it.id == id }?.checked = checked
        save(context, items)
    }

    fun setAllChecked(context: Context, checked: Boolean) {
        val items = load(context)
        items.forEach { it.checked = checked }
        save(context, items)
    }

    fun clearChecked(context: Context) {
        save(context, load(context).filterNot { it.checked }.toMutableList())
    }

    fun clearAll(context: Context) = save(context, mutableListOf())

    fun getCheckedItems(context: Context): List<CartItem> = load(context).filter { it.checked }

    fun getTotalCount(context: Context): Int = load(context).sumOf { it.quantity }

    fun getCheckedCount(context: Context): Int = getCheckedItems(context).sumOf { it.quantity }

    fun isAllChecked(context: Context): Boolean {
        val items = load(context)
        return items.isNotEmpty() && items.all { it.checked }
    }

    fun getCheckedTotalYuan(context: Context): String {
        val totalFen = getCheckedItems(context).sumOf { WxPayHelper.parsePriceToFen(it.product.price) * it.quantity }
        val yuan = totalFen / 100L
        val fen = totalFen % 100L
        return "¥%d.%02d".format(yuan, fen)
    }
}
