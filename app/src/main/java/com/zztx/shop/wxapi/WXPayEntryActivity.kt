package com.zztx.shop.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.zztx.shop.WxApiHolder
import com.zztx.shop.WxPayConstants

class WXPayEntryActivity : Activity(), IWXAPIEventHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appId = WxPayConstants.WX_APP_ID
        val api = WxApiHolder.getIfReadyOrNull(this, appId)
        if (api == null) {
            Toast.makeText(this, "微信支付未配置", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        try {
            api.handleIntent(intent, this)
        } catch (t: Throwable) {
            Toast.makeText(this, "回调处理失败：${t.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appId = WxPayConstants.WX_APP_ID
        WxApiHolder.getIfReadyOrNull(this, appId)?.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq?) = finish()

    override fun onResp(resp: BaseResp?) {
        if (resp == null) {
            Toast.makeText(this, "微信回调为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (resp.type != ConstantsAPI.COMMAND_PAY_BY_WX) {
            finish()
            return
        }
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                Toast.makeText(this, "支付成功", Toast.LENGTH_LONG).show()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                Toast.makeText(this, "已取消支付", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val reason = when (resp.errCode) {
                    BaseResp.ErrCode.ERR_COMM -> "ERR_COMM 常见原因：签名/包名/AppID 不匹配或商户号未绑 APP"
                    BaseResp.ErrCode.ERR_AUTH_DENIED -> "ERR_AUTH_DENIED 支付权限未开通或 AppID 未绑定商户号"
                    BaseResp.ErrCode.ERR_UNSUPPORT -> "ERR_UNSUPPORT 微信版本过低或未安装微信"
                    BaseResp.ErrCode.ERR_SENT_FAILED -> "ERR_SENT_FAILED 请求发送失败"
                    else -> "支付失败（errCode=${resp.errCode}）：${resp.errStr ?: ""}"
                }
                Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}
