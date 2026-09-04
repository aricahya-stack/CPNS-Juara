package com.cpnsjuara.app

import android.content.Context
import java.util.concurrent.Executors

object EntitlementSync {
    private val executor=Executors.newSingleThreadExecutor()
    fun run(context:Context,onResult:((Boolean,String)->Unit)?=null){
        AuthToken.get{token->
            if(token==null){onResult?.invoke(false,"Guest");return@get}
            executor.execute{
                try{
                    val r=ApiClient.billingStatus(Prefs.getBaseUrl(context),token)
                    val premium=r.optBoolean("premium",false);val expiry=r.optLong("expiry_time_ms",0L)
                    PremiumPrefs.save(context,premium,r.optString("product_id",""),expiry.takeIf{it>0})
                    onResult?.invoke(true,if(premium)"Premium aktif" else "Free")
                }catch(e:Exception){onResult?.invoke(false,e.message?:"Entitlement sync gagal")}
            }
        }
    }
}
