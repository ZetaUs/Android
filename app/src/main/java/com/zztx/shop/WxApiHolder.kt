package com.zztx.shop

import android.content.Context
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

object WxApiHolder {
    @Volatile
    private var instance: IWXAPI? = null

    @Volatile
    private var currentAppId: String = ""

    fun get(context: Context, appId: String): IWXAPI {
        val cur = instance
        if (cur != null && currentAppId == appId) return cur
        return synchronized(this) {
            val c2 = instance
            if (c2 != null && currentAppId == appId) c2 else {
                val fresh = WXAPIFactory.createWXAPI(context.applicationContext, appId, true)
                fresh.registerApp(appId)
                instance = fresh
                currentAppId = appId
                fresh
            }
        }
    }

    fun getIfReadyOrNull(context: Context, appId: String): IWXAPI? =
        if (appId.isBlank() || !appId.startsWith("wx")) null else get(context, appId)
}
