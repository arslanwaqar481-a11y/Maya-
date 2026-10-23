package com.example

import com.example.voice.JarvisAction
import com.example.voice.JarvisCommandParser
import com.example.voice.WakeWordOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testTorchCommands() {
        val onCmd = JarvisCommandParser.parse("torch on")
        assertTrue(onCmd.action is JarvisAction.ToggleTorch)
        assertEquals(true, (onCmd.action as JarvisAction.ToggleTorch).enable)

        val offCmd = JarvisCommandParser.parse("light band karo")
        assertTrue(offCmd.action is JarvisAction.ToggleTorch)
        assertEquals(false, (offCmd.action as JarvisAction.ToggleTorch).enable)
    }

    @Test
    fun testCallCommands() {
        val callCmd = JarvisCommandParser.parse("call Tony Stark")
        assertTrue(callCmd.action is JarvisAction.Call)
        assertEquals("tony stark", (callCmd.action as JarvisAction.Call).target)
    }

    @Test
    fun testAppLaunchCommands() {
        val appCmd = JarvisCommandParser.parse("open WhatsApp")
        assertTrue(appCmd.action is JarvisAction.LaunchApp)
        assertEquals("whatsapp", (appCmd.action as JarvisAction.LaunchApp).appName)
    }

    @Test
    fun testMorningBriefing() {
        val briefCmd = JarvisCommandParser.parse("good morning jarvis")
        assertTrue(briefCmd.action is JarvisAction.MorningBriefing)
    }

    @Test
    fun testScheduleCommands() {
        val schedCmd = JarvisCommandParser.parse("what is my schedule today")
        assertTrue(schedCmd.action is JarvisAction.ReadSchedule)
    }

    @Test
    fun testWakeWordDetectionAndStripping() {
        // Hey Jarvis sole trigger
        val jarvisOnly = JarvisCommandParser.parse("Hey Jarvis", WakeWordOption.HEY_JARVIS)
        assertTrue(jarvisOnly.action is JarvisAction.Conversation)
        assertEquals(WakeWordOption.HEY_JARVIS.voiceGreeting, jarvisOnly.spokenResponse)

        // MAYA sole trigger
        val mayaOnly = JarvisCommandParser.parse("Maya", WakeWordOption.MAYA)
        assertTrue(mayaOnly.action is JarvisAction.Conversation)
        assertEquals(WakeWordOption.MAYA.voiceGreeting, mayaOnly.spokenResponse)

        // Wake word prefix stripping
        val mayaTorch = JarvisCommandParser.parse("Maya torch on", WakeWordOption.MAYA)
        assertTrue(mayaTorch.action is JarvisAction.ToggleTorch)
        assertEquals(true, (mayaTorch.action as JarvisAction.ToggleTorch).enable)

        val jarvisCall = JarvisCommandParser.parse("Hey Jarvis call Peter", WakeWordOption.HEY_JARVIS)
        assertTrue(jarvisCall.action is JarvisAction.Call)
        assertEquals("peter", (jarvisCall.action as JarvisAction.Call).target)
    }
}
