@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallPushSubscriptionJsonAdapter
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallPushUpdateJsonAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName


@JvmSynthetic
internal fun pushJsonRpcModule() = module {

    addSerializerEntry(Web3InboxRPC.Request.Push.GetActiveSubscriptions::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.Approve::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.Reject::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.Subscribe::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.Update::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.DeleteSubscription::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.GetMessageHistory::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.DeletePushMessage::class)

    addSerializerEntry(Web3InboxRPC.Call.Push.Request::class)
    addSerializerEntry(Web3InboxRPC.Call.Push.Message::class)
    addSerializerEntry(Web3InboxRPC.Call.Push.Subscription::class)
    addSerializerEntry(Web3InboxRPC.Call.Push.Update::class)
    addSerializerEntry(Web3InboxRPC.Call.Push.Delete::class)

    addDeserializerEntry(Web3InboxMethods.Request.Push.GET_ACTIVE_SUBSCRIPTIONS, Web3InboxRPC.Request.Push.GetActiveSubscriptions::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.APPROVE, Web3InboxRPC.Request.Push.Approve::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.REJECT, Web3InboxRPC.Request.Push.Reject::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.SUBSCRIBE, Web3InboxRPC.Request.Push.Subscribe::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.UPDATE, Web3InboxRPC.Request.Push.Update::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.DELETE_SUBSCRIPTION, Web3InboxRPC.Request.Push.DeleteSubscription::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.GET_MESSAGE_HISTORY, Web3InboxRPC.Request.Push.GetMessageHistory::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.DELETE_PUSH_MESSAGE, Web3InboxRPC.Request.Push.DeletePushMessage::class)

    addDeserializerEntry(Web3InboxMethods.Call.Push.REQUEST, Web3InboxRPC.Call.Push.Request::class)
    addDeserializerEntry(Web3InboxMethods.Call.Push.MESSAGE, Web3InboxRPC.Call.Push.Message::class)
    addDeserializerEntry(Web3InboxMethods.Call.Push.SUBSCRIPTION, Web3InboxRPC.Call.Push.Subscription::class)
    addDeserializerEntry(Web3InboxMethods.Call.Push.UPDATE, Web3InboxRPC.Call.Push.Update::class)
    addDeserializerEntry(Web3InboxMethods.Call.Push.DELETE, Web3InboxRPC.Call.Push.Delete::class)

    single {
        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    Web3InboxRPC.Call.Push.Subscription::class.jvmName -> Web3InboxRPCCallPushSubscriptionJsonAdapter(moshi)
                    Web3InboxRPC.Call.Push.Update::class.jvmName -> Web3InboxRPCCallPushUpdateJsonAdapter(moshi)
                    else -> null
                }
            }
    }
}