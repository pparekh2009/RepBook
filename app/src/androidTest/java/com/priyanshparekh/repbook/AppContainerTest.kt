package com.priyanshparekh.repbook

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContainerTest {

    @Test
    fun container_isNotNull_onAppStartup() {
        val app = ApplicationProvider.getApplicationContext<RepBookApplication>()
        assertNotNull(app.container)
    }
}
