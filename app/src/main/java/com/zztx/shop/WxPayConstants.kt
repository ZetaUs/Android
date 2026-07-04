package com.zztx.shop

object WxPayConstants {
    const val WX_APP_ID = ""

    const val PREPAY_URL = ""

    const val PAY_NOTIFY_URL = ""

    const val DEBUG_PAY_TOAST = true

    private const val TAG = "WxPayConfig"

    fun isAppIdConfigured(): Boolean = WX_APP_ID.startsWith("wx") && WX_APP_ID.length >= 16

    fun isPrepayUrlConfigured(): Boolean = PREPAY_URL.startsWith("https://")

    fun checkOrGetReason(): String? = when {
        !isAppIdConfigured() -> "请先在 WxPayConstants.WX_APP_ID 填入开放平台 AppID（wx 开头 18 位）"
        !isPrepayUrlConfigured() -> "请先在 WxPayConstants.PREPAY_URL 填入后端预下单接口地址（https://…）"
        else -> null
    }
}

data class WxPrepayParams(
    val partnerId: String,
    val prepayId: String,
    val packageValue: String,
    val nonceStr: String,
    val timeStamp: String,
    val sign: String
)
