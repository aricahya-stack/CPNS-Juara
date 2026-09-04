package com.cpnsjuara.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import java.util.concurrent.Executors

class PremiumActivity : AppCompatActivity(), BillingManager.Listener {
    private lateinit var billing: BillingManager
    private val executor=Executors.newSingleThreadExecutor()
    private lateinit var status:TextView
    private lateinit var monthly:Button
    private lateinit var yearly:Button

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_premium)
        status=findViewById(R.id.txtPremiumStatus); monthly=findViewById(R.id.btnMonthly); yearly=findViewById(R.id.btnYearly)
        monthly.isEnabled=false; yearly.isEnabled=false
        monthly.setOnClickListener{billing.launch(this,BillingManager.MONTHLY)}
        yearly.setOnClickListener{billing.launch(this,BillingManager.YEARLY)}
        findViewById<Button>(R.id.btnRestore).setOnClickListener{status.text="Memulihkan pembelian...";billing.restorePurchases()}
        findViewById<Button>(R.id.btnCheckPremium).setOnClickListener{refreshServerStatus()}
        billing=BillingManager(this,this);billing.start();refreshServerStatus()
    }
    override fun onBillingReady(){runOnUiThread{status.text="Google Play Billing siap."}}
    override fun onProductsLoaded(products:List<ProductDetails>){runOnUiThread{
        products.forEach{p->val phase=p.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull();val price=phase?.formattedPrice?:"Lihat di Google Play";when(p.productId){BillingManager.MONTHLY->{monthly.text="Premium Bulanan • $price";monthly.isEnabled=true};BillingManager.YEARLY->{yearly.text="Premium Tahunan • $price";yearly.isEnabled=true}}}
    }}
    override fun onPurchaseNeedsVerification(purchase:Purchase,productId:String){
        status.text="Memverifikasi pembelian dengan server..."
        AuthToken.get{token->if(token==null){runOnUiThread{status.text="Login Google diperlukan."};return@get};executor.execute{try{val r=ApiClient.verifySubscription(Prefs.getBaseUrl(this),token,productId,purchase.purchaseToken);val premium=r.optBoolean("premium",false);val expiry=r.optLong("expiry_time_ms",0L);PremiumPrefs.save(this,premium,r.optString("product_id",productId),expiry.takeIf{it>0});runOnUiThread{status.text=if(premium)"🏆 Premium aktif. Terima kasih!" else "Pembelian belum memberi entitlement."}}catch(e:Exception){runOnUiThread{status.text="Verifikasi gagal: ${e.message}"}}}}
    }
    override fun onBillingMessage(message:String){runOnUiThread{status.text=message}}
    private fun refreshServerStatus(){AuthToken.get{token->if(token==null){runOnUiThread{status.text="Masuk dengan Google untuk Premium."};return@get};executor.execute{try{val r=ApiClient.billingStatus(Prefs.getBaseUrl(this),token);val premium=r.optBoolean("premium",false);val exp=r.optLong("expiry_time_ms",0L);PremiumPrefs.save(this,premium,r.optString("product_id",""),exp.takeIf{it>0});runOnUiThread{status.text=if(premium)"🏆 CPNS JUARA PREMIUM aktif." else "Status akun: FREE"}}catch(e:Exception){runOnUiThread{status.text=if(PremiumPrefs.isPremium(this))"Premium tersimpan lokal; server belum terhubung." else "Status server belum tersedia: ${e.message}"}}}}}
    override fun onDestroy(){billing.close();executor.shutdown();super.onDestroy()}
}
