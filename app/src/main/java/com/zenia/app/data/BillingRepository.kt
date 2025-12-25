package com.zenia.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val _billingConnectionState = MutableStateFlow(false)
    val billingConnectionState = _billingConnectionState.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val TEST_PRODUCT_ID = "android.test.purchased"

    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(pendingPurchasesParams)
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingRepo", "Conectado a Google Play Billing")
                    _billingConnectionState.value = true
                    // Al conectar, verificamos si ya tiene suscripciones activas
                    checkSubscriptionStatus()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("BillingRepo", "Desconectado. Reintentando...")
                _billingConnectionState.value = false
                startConnection()
            }
        })
    }

    /**
     * Consulta las compras existentes para restaurar el estado Premium si el usuario
     * cierra y abre la app.
     */
    private fun checkSubscriptionStatus() {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Si hay alguna compra de suscripción válida y no consumida/cancelada
                val hasActiveSubscription = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isPremium.value = hasActiveSubscription

                // Nota para Tesis: Como usamos IDs de prueba que a veces no persisten bien en emuladores,
                // _isPremium iniciará en false al reiniciar la app.
                // En una app real, esto restauraría la compra real.
            }
        }
    }

    suspend fun launchBillingFlow(activity: Activity, isSubscription: Boolean) {
        if (!_billingConnectionState.value) {
            Log.e("BillingRepo", "No hay conexión con Google Play")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PRODUCT_ID)
                .setProductType(
                    if (isSubscription) BillingClient.ProductType.SUBS
                    else BillingClient.ProductType.INAPP
                )
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
            productDetailsResult.productDetailsList != null
        ) {
            val productDetails = productDetailsResult.productDetailsList!!.firstOrNull()

            if (productDetails != null) {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .apply {
                            if (isSubscription) {
                                productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { token ->
                                    setOfferToken(token)
                                }
                            }
                        }
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingRepo", "El usuario canceló la compra.")
        } else {
            Log.e("BillingRepo", "Error en compra: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            // LÓGICA IMPORTANTE: Distinguir Donación vs Suscripción
            // Como usamos el mismo ID de prueba, usaremos una lógica simple:
            // Si NO está reconocido (acknowledged), asumimos que es suscripción nueva y la activamos.
            // Si ya está reconocido o queremos simular donación repetitiva, consumimos.

            // Para fines de TU TESIS y pruebas fáciles:
            // Vamos a consumir TODO para que siempre puedas volver a comprar y probar el flujo.
            // Pero simularemos la activación de Premium visualmente.

            consumePurchase(purchase)

            // Simulación: Si acabamos de comprar, activamos Premium en la UI temporalmente
            _isPremium.value = true

            /* CÓDIGO REAL PARA PRODUCCIÓN (Descomentar cuando tengas IDs reales):
            if (purchase.products.contains("id_suscripcion_real")) {
                acknowledgePurchase(purchase)
                _isPremium.value = true
            } else if (purchase.products.contains("id_donacion_real")) {
                consumePurchase(purchase)
            }
            */
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingRepo", "¡Producto consumido (Donación/Prueba)!")
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingRepo", "¡Suscripción reconocida y activa!")
                    _isPremium.value = true
                }
            }
        }
    }
}