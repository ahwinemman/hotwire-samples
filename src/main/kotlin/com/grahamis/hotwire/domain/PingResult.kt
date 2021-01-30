package com.grahamis.hotwire.domain

class PingResult(val value: Long) {
    override fun toString(): String = if (value < 0) "timeout" else "$value ms"
}
