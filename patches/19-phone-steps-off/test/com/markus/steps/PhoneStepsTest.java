package com.markus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneStepsTest {
    @Test
    void switchIsOffByDefault() {
        // the stub of com.markus.Settings returns the default, as the real one does for a key never written
        assertFalse(PhoneSteps.ignored());
    }

    @Test
    void switchLivesInTheModSettings() {
        assertTrue(PhoneSteps.isModSettings(0x7000));
        assertFalse(PhoneSteps.isModSettings(11));
        assertFalse(PhoneSteps.isModSettings(0));
    }

    @Test
    void keyIsStable() {
        // stored in com.markus.Settings; renaming it would reset the person's choice
        assertEquals("phone_steps_off", PhoneSteps.KEY);
    }

    @Test
    void insertIgnoresOtherMenus() {
        PhoneSteps.insertSwitch(new androidx.databinding.ObservableArrayList<Object>(), 11);
        PhoneSteps.insertSwitch(null, 0x7000);
    }
}
