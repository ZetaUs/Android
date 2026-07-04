package com.zztx.shop

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WxPayHelper {
    private const val TAG = "WxPayHelper"
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    private val gson = Gson()

    private class PayReqBody(
        val appId: String,
        val goodsId: String,
        val goodsTitle: String,
        val amountFen: Int,
        val clientType: String = "ANDROID_APP",
        val notifyUrl: String?
    )

    private data class PrepayEnvelope(
        val success: Boolean = false,
        val message: String? = null,
        @SerializedName("data") val data: WxPrepayParams? = null
    )

    fun parsePriceToFen(priceText: String): Int {
        if (priceText.isBlank()) return 0
        val cleaned = priceText
            .replace("¥", "")
            .replace("￥", "")
            .replace(",", "")
            .replace(" ", "")
            .trim()
        if (cleaned.isBlank()) return 0
        return try {
            val yuan = cleaned.toDouble()
            val fen = Math.round(yuan * 100.0)
            if (fen < 0) 0 else fen.toInt()
        } catch (_: NumberFormatException) {
            val intPart = cleaned.trimEnd { !it.isDigit() }
            if (intPart.isBlank()) 0 else (intPart.toIntOrNull() ?: 0) * 100
        }
    }

    fun requestPrepay(
        prepayUrl: String,
        appId: String,
        goods: Product,
        amountFen: Int,
        notifyUrl: String?
    ): Pair<WxPrepayParams?, String?> {
        if (prepayUrl.isBlank() || !prepayUrl.startsWith("https://")) {
            return null to "PREPAY_URL 未配置为 https://…，无法发起预下单"
        }
        if (amountFen <= 0) {
            return null to "商品金额解析失败（${amountFen}分），无法发起支付"
        }
        val body = PayReqBody(
            appId = appId,
            goodsId = goods.title,
            goodsTitle = goods.title,
            amountFen = amountFen,
            notifyUrl = notifyUrl?.ifBlank { null }
        )
        return try {
            val jsonReq = gson.toJson(body)
            Log.d(TAG, "预下单请求 url=$prepayUrl body=$jsonReq")
            val request = Request.Builder()
                .url(prepayUrl)
                .post(jsonReq.toRequestBody(JSON_MEDIA))
                .header("Accept", "application/json")
                .build()
            okHttpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                Log.d(TAG, "预下单响应 code=${resp.code} raw[前400]=${raw.take(400)}")
                if (!resp.isSuccessful) {
                    return null to "预下单 HTTP ${resp.code}：${raw.ifBlank { resp.message }}"
                }
                val envelope = runCatching { gson.fromJson(raw, PrepayEnvelope::class.java) }.getOrNull()
                val params = envelope?.data
                if (envelope?.success == true && params != null &&
                    params.partnerId.isNotBlank() && params.prepayId.isNotBlank() &&
                    params.nonceStr.isNotBlank() && params.timeStamp.isNotBlank() && params.sign.isNotBlank()
                ) {
                    params to null
                } else {
                    val msg = envelope?.message
                        ?: runCatching { JSONObject(raw).optString("message", "") }.getOrNull()
                        ?: raw.take(120)
                    null to "预下单失败：${msg.ifBlank { "返回参数不完整" }}"
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "预下单异常", t)
            null to "预下单异常：${t.message ?: t.javaClass.simpleName}"
        }
    }
}
