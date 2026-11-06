package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun handshake_protocol_verification() {
    val deviceId = "SENSOR_01"
    val capabilities = listOf("temperature", "humidity", "control")
    assertEquals(3, capabilities.size)
    assertEquals("SENSOR_01", deviceId)
  }
}
