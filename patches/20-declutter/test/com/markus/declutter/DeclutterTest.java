package com.markus.declutter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeclutterTest {
    /** Stands for MineItemListModel.MineItem: only getType() matters. */
    public static final class Item {
        private final int type;

        Item(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    @Test
    void reportRowIsDroppedOthersStay() {
        List<Object> items = new ArrayList<>(Arrays.asList(new Item(1), new Item(3), new Item(9), new Item(6),
                new Item(8), new Item(7), new Item(10), new Item(11)));
        Declutter.dropMineReport(items);
        assertEquals(7, items.size());
        for (Object o : items) {
            assertFalse(((Item) o).getType() == 9);
        }
        // «Разрешения управления устройством» (6) is not on the list of things to hide
        assertTrue(items.stream().anyMatch(o -> ((Item) o).getType() == 6));
    }

    @Test
    void unknownItemsAreKept() {
        List<Object> items = new ArrayList<>(Arrays.asList("text", null, 9, new Item(9)));
        Declutter.dropMineReport(items);
        assertEquals(3, items.size());
        Declutter.dropMineReport(null);
    }

    @Test
    void builtinModulesComeFromTheBuildList() {
        assertTrue(Declutter.listed("mine,sport", "mine"));
        assertTrue(Declutter.listed("mine, sport", "sport"));
        assertFalse(Declutter.listed("mine,sport", "dial"));
        assertFalse(Declutter.listed("mine,sport", "min"));
        assertFalse(Declutter.listed("", "mine"));
        assertFalse(Declutter.listed(null, "mine"));
        assertFalse(Declutter.listed("mine", null));
    }

    @Test
    void outsideTheAppNothingIsForced() {
        // no Android application in a unit test: the resource list is empty
        assertFalse(Declutter.builtinOnly("mine"));
    }
}
