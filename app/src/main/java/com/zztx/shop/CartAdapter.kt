package com.zztx.shop

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.gson.Gson

class CartAdapter(
    private val onCheckedChanged: (CartItem, Boolean) -> Unit,
    private val onPlus: (CartItem) -> Unit,
    private val onMinus: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit,
    private val onClickItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cb: CheckBox = itemView.findViewById(R.id.cbCartItem)
        private val ivImg: ImageView = itemView.findViewById(R.id.ivCartItemImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvCartItemTitle)
        private val tvTag: TextView = itemView.findViewById(R.id.tvCartItemTag)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnCartItemDelete)
        private val btnMinus: ImageView = itemView.findViewById(R.id.btnCartItemMinus)
        private val btnPlus: ImageView = itemView.findViewById(R.id.btnCartItemPlus)
        private val tvQty: TextView = itemView.findViewById(R.id.tvCartItemQty)

        fun bind(item: CartItem) {
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = item.checked
            cb.setOnCheckedChangeListener { _, isChecked ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onCheckedChanged(item, isChecked)
                }
            }
            tvTitle.text = item.product.title
            tvTag.text = item.product.tag
            tvTag.setBackgroundResource(if (item.product.tag == "秒杀") R.drawable.bg_badge_blue else R.drawable.bg_badge_yellow)
            tvPrice.text = item.product.price
            tvQty.text = item.quantity.toString()
            ivImg.load(item.product.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.stat_notify_error)
            }
            btnPlus.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onPlus(item)
            }
            btnMinus.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onMinus(item)
            }
            btnDelete.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onDelete(item)
            }
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onClickItem(item)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
