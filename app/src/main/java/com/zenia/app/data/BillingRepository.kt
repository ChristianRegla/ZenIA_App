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
    private val authRepository: AuthRepository // Inyectar AuthRepository
) : PurchasesUpdatedListener {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _billingConnectionState = MutableStateFlow(false)
    val billingConnectionState = _billingConnectionState.asStateFlow()

    // --- IDs DE PRODUCTO ---
    // IMPORTANTE: Estos IDs deben coincidir EXACTAMENTE con los IDs creados en la Google Play Console.
    companion object {
        const val PREMIUM_SUB_ID = "premium_annual" // ID de la suscripción
        const val DONATION_PRODUCT_ID = "donation_basic" // ID de la donación (ejemplo)
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases() // Habilitar compras pendientes es la práctica recomendada.
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
                    // Al conectar, verificamos compras para actualizar el estado en Firestore si es necesario.
                    checkSubscriptionStatus()
                } else {
                    Log.e("BillingRepo", "Error al conectar con Billing: ${billingResult.debugMessage}")
                    _billingConnectionState.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("BillingRepo", "Desconectado. Reintentando...")
                _billingConnectionState.value = false
                startConnection() // Reintentar conexión
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

    suspend fun launchBillingFlow(activity: Activity, isSubscription: Boolean) {
        if (!_billingConnectionState.value) {
            Log.e("BillingRepo", "No hay conexión con Google Play")
            return
        }

        val productId = if (isSubscription) PREMIUM_SUB_ID else DONATION_PRODUCT_ID
        val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
            !productDetailsResult.productDetailsList.isNullOrEmpty()
        ) {
            val productDetails = productDetailsResult.productDetailsList!!.first()

            val offerToken = if (isSubscription) {
                productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            } else null

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .apply {
                        offerToken?.let { setOfferToken(it) }
                    }
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, billingFlowParams)
        } else {
            Log.e("BillingRepo", "Producto no encontrado o error: ${productDetailsResult.billingResult.debugMessage}")
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
            Log.e("BillingRepo", "Error en la compra: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Distinguir entre suscripción y donación por el ID del producto
            if (purchase.products.contains(PREMIUM_SUB_ID)) {
                // Es una suscripción. Debe ser reconocida.
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            } else if (purchase.products.contains(DONATION_PRODUCT_ID)) {
                // Es una donación (producto de un solo uso). Debe ser consumida.
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
