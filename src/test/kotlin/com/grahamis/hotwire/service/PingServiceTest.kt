package com.grahamis.hotwire.service

import kotlinx.coroutines.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Future
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExtendWith(SpringExtension::class)
class PingServiceTest {
    @Mock
    private lateinit var socket: AsynchronousSocketChannel

    @Mock
    private lateinit var futureVoid: Future<Void>

    private val hostname = "localhost"
    private val port = 0

    @Test
    fun `should ping`() {
        mockSocketConnects()
        Assertions.assertThat(
            runBlocking { PingService(socket).ping(hostname, port).value }
        ).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `should timeout`() {
        mockSocketTimeout()
        Assertions.assertThat(
            runBlocking { PingService(socket).ping(hostname, port).value }
        ).isEqualTo(-1)
    }

    private fun mockSocketTimeout() {
        `when`(socket.connect(any(SocketAddress::class.java))).thenThrow(RuntimeException())
    }

    private fun mockSocketConnects() {
        `when`(socket.connect(any(SocketAddress::class.java))).thenReturn(futureVoid)
    }
}
