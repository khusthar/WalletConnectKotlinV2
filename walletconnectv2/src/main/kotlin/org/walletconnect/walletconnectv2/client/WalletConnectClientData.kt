package org.walletconnect.walletconnectv2.client

import java.net.URI

sealed class WalletConnectClientData {

    data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        var methods: List<String>,
        val topic: String,
        val proposerPublicKey: String,
        val ttl: Long
    ) : WalletConnectClientData() {
        val icon: String = icons.first().toString()
    }

    data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequest
    ) : WalletConnectClientData() {

        data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String
        ) : WalletConnectClientData()
    }

    // Review if we need defaults
    data class SettledSession(
        var icon: String? = "",
        var name: String = "",
        var uri: String = "",
        val topic: String
    ) : WalletConnectClientData()

    data class SettledPairing(val topic: String) : WalletConnectClientData()

    data class RejectedSession(val topic: String) : WalletConnectClientData()

    data class DeletedSession(val topic: String) : WalletConnectClientData()

    data class Response(val topic: String) : WalletConnectClientData()

    sealed class JsonRpcResponse : WalletConnectClientData() {
        abstract val id: Long
        val jsonrpc: String = "2.0"

        data class JsonRpcResult<T>(
            override val id: Long,
            val result: T,
        ) : JsonRpcResponse()

        data class JsonRpcError(
            override val id: Long,
            val error: Error,
        ) : JsonRpcResponse()

        data class Error(
            val code: Long,
            val message: String,
        )
    }
}