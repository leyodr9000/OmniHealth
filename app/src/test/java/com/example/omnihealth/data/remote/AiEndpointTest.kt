package com.example.omnihealth.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class AiEndpointTest {

    @Test
    fun https_is_always_allowed() {
        assertEquals(AiEndpoint.Check.Ok, AiEndpoint.check("https://api.example.com/v1"))
    }

    @Test
    fun plain_http_to_public_host_is_rejected() {
        assertEquals(AiEndpoint.Check.Insecure, AiEndpoint.check("http://api.example.com/v1"))
        assertEquals(AiEndpoint.Check.Insecure, AiEndpoint.check("HTTP://192.0.2.10:8080"))
    }

    @Test
    fun loopback_and_lan_are_allowed_for_debugging() {
        assertEquals(AiEndpoint.Check.Ok, AiEndpoint.check("http://localhost:11434/v1"))
        assertEquals(AiEndpoint.Check.Ok, AiEndpoint.check("http://10.0.2.2:8080"))
        assertEquals(AiEndpoint.Check.Ok, AiEndpoint.check("http://192.168.1.10:8080"))
    }

    @Test
    fun blank_and_malformed_are_reported() {
        assertEquals(AiEndpoint.Check.Blank, AiEndpoint.check("   "))
        assertEquals(AiEndpoint.Check.Malformed, AiEndpoint.check("api.example.com"))
        assertEquals(AiEndpoint.Check.Malformed, AiEndpoint.check("ftp://x"))
    }
}
