package com.walletconnect.web3.wallet.domain

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object PushWalletDelegate: PushWalletClient.Delegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcPushEventModels: MutableSharedFlow<Push.Wallet.Event?> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wcPushEventModels: SharedFlow<Push.Wallet.Event?> = _wcPushEventModels

    init {
        PushWalletClient.setDelegate(this)
    }

    override fun onPushRequest(pushRequest: Push.Wallet.Event.Request) {
        scope.launch { _wcPushEventModels.emit(pushRequest) }
    }

    override fun onPushMessage(message: Push.Wallet.Event.Message) {
        scope.launch { _wcPushEventModels.emit(message) }
    }

    override fun onPushDelete(deletedTopic: Push.Wallet.Event.Delete) {
    }

    override fun onError(error: Push.Model.Error) {
    }
}