package com.zztx.shop

data class CartItem(
    val id: String,
    val product: Product,
    var quantity: Int = 1,
    var checked: Boolean = true
) {
    companion object {
        fun buildId(p: Product): String = "${p.title}__${p.tag}__${p.price}"
    }
}
