package com.grahamis.hotwire.controller

import com.grahamis.CustomMediaType
import com.grahamis.hotwire.TemplateSelectorModifier
import com.grahamis.hotwire.service.PingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import kotlin.time.ExperimentalTime

@RequestMapping("/pinger")
@Controller
@ExperimentalTime
class PingController {
    @Autowired
    private lateinit var pingService: PingService

    @Value("\${ping.hostname:127.0.0.1}")
    private val hostname: String = "127.0.0.1"

    @Value("\${ping.port:8080}")
    private val port: Int = 8080

    @RequestMapping(produces = [CustomMediaType.TURBO_STREAM_VALUE])
    suspend fun pingerStream(model: Model) = view(model, TemplateSelectorModifier.TurboStream)

    @RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
    suspend fun pingerPage(model: Model) = view(model)

    private suspend fun view(
        model: Model,
        templateSelectorModifier: TemplateSelectorModifier = TemplateSelectorModifier.Default
    ): String {
        model.addAttribute("pingTime", pingService.ping(hostname, port))
        return templateSelectorModifier.modifyName("ping")
    }
}
