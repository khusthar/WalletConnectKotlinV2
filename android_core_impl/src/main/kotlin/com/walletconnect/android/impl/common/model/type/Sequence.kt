package com.walletconnect.android.impl.common.model.type

import com.walletconnect.android.api.Expiry
import com.walletconnect.foundation.common.model.Topic

interface Sequence {
    val topic: Topic
    val expiry: Expiry
}