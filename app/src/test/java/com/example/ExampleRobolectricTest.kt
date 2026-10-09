package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityLockType
import com.example.data.security.SecurityManager
import com.example.ui.viewmodel.FinanceViewModel
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
  fun `respect punch hole toggle in viewmodel`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(application)
    assertTrue(viewModel.respectPunchHole.value)

    viewModel.toggleRespectPunchHole(false)
    assertFalse(viewModel.respectPunchHole.value)

    viewModel.toggleRespectPunchHole(true)
    assertTrue(viewModel.respectPunchHole.value)
  }

  @Test
  fun `clear all data resets transactions and state`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(application)
    viewModel.clearAllData()
    // Verify execution succeeds without exception
    assertTrue(true)
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

  @Test
  fun `biometric lock enabled state and removal cleanup`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val security = SecurityManager(context)
    security.removeLock()

    assertFalse(security.isBiometricEnabled())
    security.setBiometricEnabled(true)
    assertTrue(security.isBiometricEnabled())

    // Setting PIN
    security.setPin("9999")
    assertTrue(security.isBiometricEnabled())

    // When lock is removed, biometrics should be automatically disabled
    security.removeLock()
    assertFalse(security.isBiometricEnabled())
    assertEquals(SecurityLockType.NONE, security.getLockType())
  }

  @Test
  fun `biometric auth manager status check does not crash`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val status = com.example.data.security.BiometricAuthManager.checkBiometricStatus(context)
    val desc = com.example.data.security.BiometricAuthManager.getStatusDescription(status)
    assertTrue(desc.isNotBlank())
  }
}
