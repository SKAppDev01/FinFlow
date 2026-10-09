package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityLockType
import com.example.data.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FinFlow", appName)
  }

  @Test
  fun `security manager pin setup and verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val security = SecurityManager(context)
    security.removeLock()

    assertEquals(SecurityLockType.NONE, security.getLockType())
    assertFalse(security.hasCredentialSet())

    val setSuccess = security.setPin("2580")
    assertTrue(setSuccess)
    assertEquals(SecurityLockType.PIN, security.getLockType())
    assertTrue(security.hasCredentialSet())

    assertTrue(security.verifyCredential("2580"))
    assertFalse(security.verifyCredential("1234"))

    security.removeLock()
    assertEquals(SecurityLockType.NONE, security.getLockType())
    assertFalse(security.hasCredentialSet())
  }

  @Test
  fun `security manager password setup and verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val security = SecurityManager(context)
    security.removeLock()

    val setSuccess = security.setPassword("FinFlow2026!")
    assertTrue(setSuccess)
    assertEquals(SecurityLockType.PASSWORD, security.getLockType())

    assertTrue(security.verifyCredential("FinFlow2026!"))
    assertFalse(security.verifyCredential("wrongPassword"))

    security.removeLock()
    assertEquals(SecurityLockType.NONE, security.getLockType())
  }
}
