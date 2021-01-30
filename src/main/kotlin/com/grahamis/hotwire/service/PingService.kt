package com.grahamis.hotwire.service

import com.grahamis.hotwire.domain.PingResult
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Service
class PingService() {
    // TestOnly. A new AsynchronousSocketChannel is opened each time in production.
    @Autowired(required = false)
    private var testOnlyChannel: AsynchronousSocketChannel? = null

    @TestOnly
    constructor(channel: AsynchronousSocketChannel) : this() {
        this.testOnlyChannel = channel
    }

    @ExperimentalTime
    suspend fun ping(hostname: String, port: Int): PingResult = PingResult(
        runCatching {
            val channel = testOnlyChannel ?: AsynchronousSocketChannel.open()
            measureTime {
                channel.use {
                    it.connect(InetSocketAddress(hostname, port)).get(10, TimeUnit.SECONDS)
                    fakeLatency(it::class.simpleName?.contains("Mock") != true)
                }
            }.toLongMilliseconds()
        }.getOrDefault(-1)
    )

    /**
     * The following is purely for some randomness to simulate ping times.
     * The randomness doesn't occur for unit tests (as it uses a mocked AsynchronousSocketChannel).
     * None of this sort of code would appear in a real app.
     */
    private suspend fun fakeLatency(fakeIt: Boolean) {
        if (fakeIt) {
            val sleep = Random.nextLong().absoluteValue % 10
            if (sleep % 5 == 0L)
                throw IOException() // force some timeouts now and then
            delay(sleep)
        }
    }
}
