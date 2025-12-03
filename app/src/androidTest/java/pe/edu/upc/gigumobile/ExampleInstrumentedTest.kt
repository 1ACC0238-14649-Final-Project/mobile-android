package pe.edu.upc.gigumobile

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device or emulator.
 *
 * These tests run within the Android runtime, allowing access to system services
 * and the application context. In this example, the test checks that the app’s
 * package name matches the expected value.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pe.edu.upc.tpblueprint", appContext.packageName)
    }
}
