package com.zztx.shop
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val titleView: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.tvProductSubtitle)
        private val priceView: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tagView: TextView = itemView.findViewById(R.id.tvProductTag)
        fun bind(product: Product) {
            titleView.text = product.title
            subtitleView.text = product.subtitle
            priceView.text = product.price
            tagView.text = product.tag
            tagView.setBackgroundResource(if (product.tag == "秒杀") R.drawable.bg_badge_blue else R.drawable.bg_badge_yellow)
            productImage.load(product.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            // 新增：点击商品跳转详情
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PageDetailActivity::class.java)
                intent.putExtra("goods_data", product)
                itemView.context.startActivity(intent)
            }
        }
    }
    private object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.title == newItem.title
        }
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}