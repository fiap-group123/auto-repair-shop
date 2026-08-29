package br.com.autorepairshop.api

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

fun withHttpRequest(
    method: String = "POST",
    requestUri: String = "/resource",
    block: () -> Unit,
) {
    val request = MockHttpServletRequest(method, requestUri)
    request.scheme = "http"
    request.serverName = "localhost"
    request.serverPort = 8080
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    try {
        block()
    } finally {
        RequestContextHolder.resetRequestAttributes()
    }
}
