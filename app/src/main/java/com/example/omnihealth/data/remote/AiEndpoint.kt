package com.example.omnihealth.data.remote

/**
 * 自定义接口地址的校验。
 *
 * 健康摘要属于敏感数据，默认只允许 HTTPS；明文 HTTP 仅在回环/局域网调试地址上放行，
 * 避免用户把摘要发到公网明文端口上却毫无提示。
 */
object AiEndpoint {

    sealed interface Check {
        data object Ok : Check

        data object Blank : Check

        data object Insecure : Check

        data object Malformed : Check
    }

    private val LOOPBACK_HOSTS = setOf("localhost", "127.0.0.1", "10.0.2.2", "::1")

    fun check(rawUrl: String): Check {
        val url = rawUrl.trim()
        if (url.isEmpty()) return Check.Blank
        val lower = url.lowercase()
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return Check.Malformed
        if (lower.startsWith("https://")) return Check.Ok

        val host = lower.removePrefix("http://")
            .substringBefore('/')
            .substringBefore(':')
        val isLocal = host in LOOPBACK_HOSTS ||
            host.startsWith("192.168.") ||
            host.startsWith("10.") ||
            host.startsWith("172.16.")
        return if (isLocal) Check.Ok else Check.Insecure
    }

    fun isUsable(rawUrl: String): Boolean = when (check(rawUrl)) {
        Check.Ok -> true
        else -> false
    }
}
