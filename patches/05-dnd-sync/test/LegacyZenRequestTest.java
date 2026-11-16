import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class LegacyZenRequestTest {
    @Test void onUsesStockLegacyCommandInsteadOfUnsupportedRule() {
        t3u request = LegacyZenRequest.create(true);
        assertEquals(2, request.c);
        assertEquals(23, request.d);
        assertEquals(0, request.u().B().a);
    }

    @Test void offUsesDisabledValueTwo() {
        t3u request = LegacyZenRequest.create(false);
        assertEquals(2, request.c);
        assertEquals(23, request.d);
        assertEquals(2, request.u().B().a);
    }
}
