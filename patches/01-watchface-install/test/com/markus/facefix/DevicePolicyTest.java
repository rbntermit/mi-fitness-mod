package com.markus.facefix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DevicePolicyTest {
    @Test void wearAndDualStayStock() {
        assertFalse(DevicePolicy.localFaces(1)); assertTrue(DevicePolicy.localFaces(6));
        assertTrue(DevicePolicy.localFaces(7)); assertFalse(DevicePolicy.localFaces(null));
        assertFalse(DevicePolicy.localFaces(0)); assertTrue(DevicePolicy.localFaces(3));
    }
    @Test void serviceNeedsCompatibleDeviceAndUsableTarget() {
        String stock = "com.xiaomi.mi_connect_service";
        assertEquals(stock, DevicePolicy.corePackage(stock, false, true));
        assertEquals(stock, DevicePolicy.corePackage(stock, true, false));
        assertEquals("com.xiaomi.lyra", DevicePolicy.corePackage(stock, true, true));
        assertEquals("custom.service", DevicePolicy.corePackage("custom.service", true, true));
    }
}