package com.walletconnect.android.sync.engine.domain

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.common.json_rpc.JsonRpcMethod
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.android.sync.engine.use_case.calls.*
import com.walletconnect.android.sync.engine.use_case.requests.OnDeleteRequestUseCase
import com.walletconnect.android.sync.engine.use_case.requests.OnSetRequestUseCase
import com.walletconnect.android.sync.engine.use_case.subscriptions.SubscribeToAllStoresUpdatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class SyncEngine(
    private val getStoresUseCase: GetStoresUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val isAccountRegisteredUseCase: IsAccountRegisteredUseCase,
    private val createStoreUseCase: CreateStoreUseCase,
    private val deleteStoreValueUseCase: DeleteStoreValueUseCase,
    private val setStoreValueUseCase: SetStoreValueUseCase,
    private val pairingHandler: PairingControllerInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val onSetRequestUseCase: OnSetRequestUseCase,
    private val onDeleteRequestUseCase: OnDeleteRequestUseCase,
    private val subscribeToAllStoresUpdatesUseCase: SubscribeToAllStoresUpdatesUseCase,
) : GetMessageUseCaseInterface by GetMessageUseCase,
    CreateUseCaseInterface by createStoreUseCase,
    GetStoresUseCaseInterface by getStoresUseCase,
    RegisterAccountUseCaseInterface by registerAccountUseCase,
    IsAccountRegisteredUseCaseInterface by isAccountRegisteredUseCase,
    DeleteUseCaseInterface by deleteStoreValueUseCase,
    SetUseCaseInterface by setStoreValueUseCase {

    private var jsonRpcRequestsJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var internalUseCaseJob: Job? = null

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_SYNC_SET,
            JsonRpcMethod.WC_SYNC_DELETE,
        )
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _events.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) {
                        subscribeToAllStoresUpdatesUseCase(onError = { error -> scope.launch { _events.emit(SDKError(error)) } })
                    }
                }
                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
                if (internalUseCaseJob == null) {
                    internalUseCaseJob = collectUseCaseEvents()
                }
            }
            .launchIn(scope)
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is SyncParams }
            .onEach { request ->
                when (val params = request.params) {
                    is SyncParams.SetParams -> onSetRequestUseCase(params, request)
                    is SyncParams.DeleteParams -> onDeleteRequestUseCase(params, request)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _events.emit(exception) }
            .launchIn(scope)

    private fun collectUseCaseEvents(): Job =
        merge(onSetRequestUseCase.events, onDeleteRequestUseCase.events)
            .onEach { event -> _events.emit(event) }
            .launchIn(scope)
}