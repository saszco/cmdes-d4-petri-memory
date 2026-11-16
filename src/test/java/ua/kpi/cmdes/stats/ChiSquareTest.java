package ua.kpi.cmdes.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChiSquareTest {

    /** Табличні критичні значення chi^2 для alpha = 0,05. */
    @Test
    void criticalValuesMatchTables() {
        assertEquals(3.841, ChiSquare.criticalValue(0.05, 1), 1e-3);
        assertEquals(11.070, ChiSquare.criticalValue(0.05, 5), 1e-3);
        assertEquals(18.307, ChiSquare.criticalValue(0.05, 10), 1e-3);
        assertEquals(31.410, ChiSquare.criticalValue(0.05, 20), 1e-3);
    }
}
