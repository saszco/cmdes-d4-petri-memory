package ua.kpi.cmdes.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class FisherTest {

    /** Табличні критичні значення для рівня значущості 0,05. */
    @Test
    void criticalValuesMatchTables() {
        assertEquals(3.326, Fisher.criticalValue(0.05, 5, 10), 1e-3);
        assertEquals(3.074, Fisher.criticalValue(0.05, 2, 117), 1e-3);
        assertEquals(2.228, Fisher.studentCritical(0.05, 10), 1e-3);
        assertEquals(0.5441, Fisher.cochranCritical(0.05, 4, 5), 1e-3);
    }

    @Test
    void anovaDetectsShiftedLevel() {
        List<Double> a = List.of(1.0, 2.0, 3.0, 2.0);
        List<Double> b = List.of(2.0, 1.0, 2.0, 3.0);
        assertFalse(Anova.oneWay(List.of(a, b), 0.05).significant());
        List<Double> c = List.of(11.0, 12.0, 13.0, 12.0);
        Anova shifted = Anova.oneWay(List.of(a, c), 0.05);
        assertTrue(shifted.significant());
        assertEquals(1, shifted.factorDf());
        assertEquals(6, shifted.residualDf());
    }
}
