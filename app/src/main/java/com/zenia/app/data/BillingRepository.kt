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
                    // Al conectar, verificamos compras para actualizar el estado en Firestore si es necesario.
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
     * Consulta las compras existentes para asegurar que el estado en Firestore esté actualizado.
     */
    private fun checkSubscriptionStatus() {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSubscription = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                // Actualiza Firestore. La UI reaccionará al Flow de AuthRepository.
                repositoryScope.launch {
                    authRepository.updateUserSubscription(hasActiveSubscription)
                }
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
                                productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let {
                                    token -> setOfferToken(token)
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
            // Para fines de prueba, consumimos todo para poder comprar de nuevo.
            // En producción, distinguirías entre suscripción y donación.
            consumePurchase(purchase)

            // **MEJORA CLAVE**: Actualizamos Firestore, nuestra fuente de verdad.
            repositoryScope.launch {
                authRepository.updateUserSubscription(true)
            }
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

    // acknowledgePurchase se mantiene por si se usa para suscripciones reales en el futuro
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
                }
            }
        }
    }
}
