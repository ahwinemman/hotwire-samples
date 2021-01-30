package com.grahamis.hotwire.service

import com.grahamis.hotwire.domain.PingResult
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Service
class PingService() {
    // TestOnly. A new Socket() is created each time in production.
    private var testSocket: Socket? = null

    @TestOnly
    @Autowired(required = false)  // Test will provide a `@MockBean`
    constructor(socket: Socket) : this() {
        this.testSocket = socket
    }

    @ExperimentalTime
    suspend fun ping(hostname: String, port: Int): PingResult = PingResult(
        withContext(Dispatchers.IO) {
            val socket = testSocket ?: Socket()
            runCatching {
                measureTime {
                    socket.use {
                        it.connect(InetSocketAddress(hostname, port))
                        fakeLatency(it::class == Socket::class)
                    }
                }.toLongMilliseconds()
            }.getOrDefault(-1)
        })

    /**
     * The following is purely for some randomness to simulate ping times.
     * The randomness doesn't occur for unit tests (as it uses a mocked Socket).
     * None of this sort of code would appear in a real app.
     */
    private suspend fun fakeLatency(fake: Boolean) {
        if (fake) {
            val sleep = Random.nextLong().absoluteValue % 10
            if (sleep % 5 == 0L)
                throw IOException() // force some timeouts now and then
            delay(sleep)
        }
    }
}
