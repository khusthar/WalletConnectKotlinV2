package com.walletconnect.android.api.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.android.api.AndroidApiDITags
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.android.api.ConnectionType
import com.walletconnect.foundation.network.data.ManualConnectionLifecycle
import com.walletconnect.foundation.di.networkModule
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.network.data.service.RelayService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

fun androidApiNetworkModule(serverUrl: String, jwt: String, connectionType: ConnectionType, sdkVersion: String) = module {

//    includes(networkModule(serverUrl, sdkVersion, jwt))

    val DEFAULT_BACKOFF_SECONDS = 5L
    val TIMEOUT_TIME = 5000L

    println("kobe; androidApiNetworkModule")

    single(named(AndroidApiDITags.INTERCEPTOR)) {

        println("kobe; Interceptor")

        Interceptor { chain ->
            val updatedRequest = chain.request().newBuilder()
                .addHeader("User-Agent", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
                .build()

            chain.proceed(updatedRequest)
        }
    }

    single(named(AndroidApiDITags.OK_HTTP)) {
        println("kobe; OkHTTP")

        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>(named(AndroidApiDITags.INTERCEPTOR)))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single(named(AndroidApiDITags.MSG_ADAPTER)) { MoshiMessageAdapter.Factory(get(named(AndroidApiDITags.MOSHI))) }

//        single { relay } //?: RelayClient(get(), get(), get(), scope) }

    single(named(AndroidApiDITags.CONNECTION_CONTROLLER)) {
        if (connectionType == ConnectionType.MANUAL) {

            println("kobe; Manual controller")

            ConnectionController.Manual()
        } else {

            println("kobe; Automatic controller")

            ConnectionController.Automatic
        }
    }

    single(named(AndroidApiDITags.LIFECYCLE)) {
        if (connectionType == ConnectionType.MANUAL) {

            println("kobe; Manual cycle")

            ManualConnectionLifecycle(get(named(AndroidApiDITags.CONNECTION_CONTROLLER)), LifecycleRegistry())
        } else {

            println("kobe; Automatic cycle")

            AndroidLifecycle.ofApplicationForeground(androidApplication())
        }
    }

    single { LinearBackoffStrategy(TimeUnit.SECONDS.toMillis(DEFAULT_BACKOFF_SECONDS)) }

    single { FlowStreamAdapter.Factory() }

    single(named(AndroidApiDITags.SCARLET)) {

        println("kobe; Client Scarlet")

        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>(named(AndroidApiDITags.OK_HTTP)).newWebSocketFactory("$serverUrl&auth=$jwt"))
            .lifecycle(get(named(AndroidApiDITags.LIFECYCLE)))
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>(named(AndroidApiDITags.MSG_ADAPTER)))
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single<RelayService>(named(AndroidApiDITags.RELAY_SERVICE)) {

        println("kobe; RelayService")

        get<Scarlet>(named(AndroidApiDITags.SCARLET)).create(RelayService::class.java)
    }
}