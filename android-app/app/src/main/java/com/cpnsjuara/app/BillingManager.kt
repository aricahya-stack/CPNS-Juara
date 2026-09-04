package com.cpnsjuara.app

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.auth.FirebaseAuth

class BillingManager(context: Context, private val listener: Listener) : PurchasesUpdatedListener {
    interface Listener {
        fun onBillingReady()
        fun onProductsLoaded(products: List<ProductDetails>)
        fun onPurchaseNeedsVerification(purchase: Purchase, productId: String)
        fun onBillingMessage(message: String)
    }

    companion object {
        const val MONTHLY = "cpns_juara_premium_monthly"
        const val YEARLY = "cpns_juara_premium_yearly"
        val PRODUCT_IDS = listOf(MONTHLY, YEARLY)
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()
    private var loadedProducts: List<ProductDetails> = emptyList()

    fun start() {
        if (billingClient.isReady) { listener.onBillingReady(); loadProducts(); return }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    listener.onBillingReady(); loadProducts()
                } else listener.onBillingMessage("Billing belum siap: ${result.debugMessage}")
            }
            override fun onBillingServiceDisconnected() {
                listener.onBillingMessage("Koneksi Google Play Billing terputus sementara.")
            }
        })
    }

    fun loadProducts() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            PRODUCT_IDS.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it).setProductType(ProductType.SUBS).build()
            }
        ).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode != BillingResponseCode.OK) {
                listener.onBillingMessage("Gagal memuat produk: ${result.debugMessage}")
                return@queryProductDetailsAsync
            }
            loadedProducts = queryResult.productDetailsList
            listener.onProductsLoaded(loadedProducts)
        }
    }

    fun launch(activity: Activity, productId: String) {
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
        if (user == null) { listener.onBillingMessage("Premium memerlukan Firebase + Login Google yang sudah dikonfigurasi."); return }
        val product = loadedProducts.firstOrNull { it.productId == productId }
        if (product == null) { listener.onBillingMessage("Produk belum tersedia di Play Console."); return }
        val offer = product.subscriptionOfferDetails?.firstOrNull()
        if (offer == null) { listener.onBillingMessage("Base plan/offer belum aktif."); return }
        val pdp = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product).setOfferToken(offer.offerToken).build()
        val builder = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(pdp))
        builder.setObfuscatedAccountId(user.uid)
        val result = billingClient.launchBillingFlow(activity, builder.build())
        if (result.responseCode != BillingResponseCode.OK) {
            listener.onBillingMessage("Tidak dapat membuka pembayaran: ${result.debugMessage}")
        }
    }

    fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingResponseCode.OK) {
                listener.onBillingMessage("Restore gagal: ${result.debugMessage}"); return@queryPurchasesAsync
            }
            if (purchases.isEmpty()) listener.onBillingMessage("Tidak ada subscription ditemukan.")
            else processPurchases(purchases)
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                listener.onBillingMessage("Pembayaran masih pending. Premium belum diaktifkan.")
                return@forEach
            }
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return@forEach
            val productId = purchase.products.firstOrNull() ?: return@forEach
            listener.onPurchaseNeedsVerification(purchase, productId)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> if (purchases != null) processPurchases(purchases)
            BillingResponseCode.USER_CANCELED -> listener.onBillingMessage("Pembelian dibatalkan.")
            BillingResponseCode.ITEM_ALREADY_OWNED -> restorePurchases()
            else -> listener.onBillingMessage("Billing: ${result.debugMessage}")
        }
    }

    fun close() { if (billingClient.isReady) billingClient.endConnection() }
}
