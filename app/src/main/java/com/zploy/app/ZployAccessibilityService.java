package com.zploy.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

/** Captures stylus MotionEvents and reinjects them as single-finger touchscreen events. */
public final class ZployAccessibilityService extends AccessibilityService {
    private static volatile ZployAccessibilityService instance;
    public static ZployAccessibilityService getInstance() { return instance; }

    private long touchDownTime;
    private boolean penDown;

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        ShizukuBridge.get().init(this);
        captureStylus(true);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }

    @Override public void onMotionEvent(MotionEvent event) {
        if (event == null || (event.getSource() & InputDevice.SOURCE_STYLUS) != InputDevice.SOURCE_STYLUS) return;
        if (!ShizukuBridge.get().isReady()) return;

        final int masked = event.getActionMasked();
        int action;
        switch (masked) {
            case MotionEvent.ACTION_DOWN:
                penDown = true;
                touchDownTime = event.getDownTime();
                action = MotionEvent.ACTION_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!penDown) return;
                action = MotionEvent.ACTION_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                if (!penDown) return;
                action = MotionEvent.ACTION_UP;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!penDown) return;
                action = MotionEvent.ACTION_CANCEL;
                break;
            default:
                return;
        }

        long down = touchDownTime > 0 ? touchDownTime : event.getDownTime();
        ShizukuBridge.get().injectMotion(action, down, event.getEventTime(),
                new int[]{0}, new float[]{event.getX()}, new float[]{event.getY()});

        if (masked == MotionEvent.ACTION_UP || masked == MotionEvent.ACTION_CANCEL) {
            penDown = false;
            touchDownTime = 0;
        }
    }

    private void captureStylus(boolean enabled) {
        try {
            AccessibilityServiceInfo info = getServiceInfo();
            if (info == null) return;
            info.setMotionEventSources(enabled ? InputDevice.SOURCE_STYLUS : 0);
            setServiceInfo(info);
        } catch (Throwable ignored) { }
    }

    @Override public void onDestroy() {
        captureStylus(false);
        instance = null;
        super.onDestroy();
    }
}
