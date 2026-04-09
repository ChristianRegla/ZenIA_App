package com.zenia.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : PurchasesUpdatedListener {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _billingConnectionState = MutableStateFlow(false)
    val billingConnectionState = _billingConnectionState.asStateFlow()

    companion object {
        const val PREMIUM_SUB_ID = "zenia_premium"

        const val DONATION_CAFE = "donacion_cafe"
        const val DONATION_PIZZA = "donacion_pizza"
        const val DONATION_AMOR = "donacion_amor"
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        if (billingClient.isReady) {
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingRepo", "Conectado a Google Play Billing")
                    _billingConnectionState.value = true
                    checkSubscriptionStatus()
                } else {
                    Log.e("BillingRepo", "Error al conectar con Billing: ${billingResult.debugMessage}")
                    _billingConnectionState.value = false
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
     * Consulta las compras de suscripciones existentes para asegurar que el estado en Firestore esté actualizado.
     * También procesa compras no reconocidas que pudieron quedar pendientes (ej. si la app cerró).
     */
    fun checkSubscriptionStatus() {
        if (!billingClient.isReady) return

        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSubscription = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                repositoryScope.launch {
                    authRepository.updateUserSubscription(hasActiveSubscription)
                }
                // Procesar también las compras que necesitan ser reconocidas.
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                    .forEach { handlePurchase(it) }

            } else {
                 Log.e("BillingRepo", "Error al consultar suscripciones: ${billingResult.debugMessage}")
            }
        }
    }

    suspend fun launchSubscription(activity: Activity, planId: String) {
        if (!_billingConnectionState.value) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_SUB_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        val productDetails = result.productDetailsList?.firstOrNull() ?: return

        val offer = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == planId }

        val offerToken = offer?.offerToken ?: return

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    suspend fun launchDonation(activity: Activity, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        val productDetails = result.productDetailsList?.firstOrNull() ?: return

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    suspend fun getSubscriptionPrices(): Map<String, String> {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_SUB_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)

        val productDetails = result.productDetailsList?.firstOrNull()
            ?: return emptyMap()

        val prices = mutableMapOf<String, String>()

        productDetails.subscriptionOfferDetails?.forEach { offer ->
            val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
            val formattedPrice = pricingPhase?.formattedPrice ?: return@forEach

            prices[offer.basePlanId] = formattedPrice
        }

        return prices
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingRepo", "El usuario canceló la compra.")
        } else {
            Log.e("BillingRepo", "Error en la compra: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(PREMIUM_SUB_ID)) {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            } else if (
                purchase.products.contains(DONATION_CAFE) ||
                purchase.products.contains(DONATION_PIZZA) ||
                purchase.products.contains(DONATION_AMOR)
            ) {
                consumePurchase(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d("BillingRepo", "La compra está pendiente. Se procesará cuando se complete.")
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingRepo", "¡Donación consumida con éxito!")
            } else {
                Log.e("BillingRepo", "Error al consumir donación: ${billingResult.debugMessage}")
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
                    repositoryScope.launch {
                        authRepository.updateUserSubscription(true)
                    }
                } else {
                    Log.e("BillingRepo", "Error al reconocer suscripción: ${billingResult.debugMessage}")
                }
            }
        }
    }
}