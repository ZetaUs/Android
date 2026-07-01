package com.zztx.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val title: String,
    val subtitle: String,
    val price: String,
    val tag: String,
    val accent: String = "#FEF3C7",
    val imageUrl: String? = null
) : Parcelable