package com.zztx.shop

import com.tencent.mm.opensdk.modelpay.PayReq

fun PayReq.fillFrom(params: WxPrepayParams, appId: String) {
    this.appId = appId
    this.partnerId = params.partnerId
    this.prepayId = params.prepayId
    this.packageValue = if (params.packageValue.isBlank()) "Sign=WXPay" else params.packageValue
    this.nonceStr = params.nonceStr
    this.timeStamp = params.timeStamp
    this.sign = params.sign
}
