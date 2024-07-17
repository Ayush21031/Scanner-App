package com.example.postapi

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.KeyEvent

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        serviceInfo = info
        Log.d("AccessibilityService", "Service connected with flags: ${info.flags}")
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Log the event information for debugging purposes
        Log.i("AccessibilityEvent", "Event Type: ${AccessibilityEvent.eventTypeToString(event.eventType)}")
        Log.i("AccessibilityEvent", "Event Text: ${event.text}")
        Log.i("AccessibilityEvent", "Event Source: ${event.source}")
        Log.i("AccessibilityEvent", "Event Window ID: ${event.windowId}")
    }

    override fun onInterrupt() {
        // Not used in this example
        Log.i("AccessibilityService", "Service Interrupted")

    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.i("AccessibilityService", "KeyEvent received: ${KeyEvent.keyCodeToString(event.keyCode)}")
        return if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    performClick("select_image_button")
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    performClick("capture_image_button")
                    true
                }
                else -> super.onKeyEvent(event)
            }
        } else {
            super.onKeyEvent(event)
        }
    }


    private fun performClick(contentDescription: String): Boolean {

        val rootInActiveWindow = rootInActiveWindow ?: return false
        val nodes = rootInActiveWindow.findAccessibilityNodeInfosByText(contentDescription)
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        return false
    }
}
