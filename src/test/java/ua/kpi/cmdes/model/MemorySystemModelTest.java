package ua.kpi.cmdes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ua.kpi.cmdes.petri.SimulationListener;
import ua.kpi.cmdes.petri.Transition;

class MemorySystemModelTest {

    @Test
    void memoryInvariantHoldsAfterEveryEvent() {
        MemorySystemModel model = new MemorySystemModel(new SystemParameters(3.0, 2.0, 12), 7);
        MemorySystemNet net = model.net();
        model.addListener(new SimulationListener() {
            @Override
            public void onEvent(EventType type, Transition transition, double time) {
                assertTrue(net.memoryInvariantHolds(), () -> "Invariant broken at t=" + time);
                assertTrue(net.occupiedPages() <= net.memoryPages());
            }
        });
        model.run(50_000, 0);
    }

    @Test
    void smallMemoryUnderOverloadNeverDeadlocks() {
        double endTime = 100_000;
        for (int pages = PageThresholds.DEADLOCK_FREE.admission(); pages <= 8; pages++) {
            MemorySystemModel model = new MemorySystemModel(new SystemParameters(2.0, 2.0, pages), pages);
            double[] lastCompletion = {0};
            model.addListener(new SimulationListener() {
                @Override
                public void onEvent(EventType type, Transition transition, double time) {
                    if (transition == model.net().processing && type == EventType.EXIT) {
                        lastCompletion[0] = time;
                    }
                }
            });
            model.run(endTime, 0);
            assertTrue(endTime - lastCompletion[0] < 100, "Jobs stopped completing with M=" + pages);
        }
    }

    @Test
    void jobsAreConservedAndRefusalsAppearOnlyUnderLoad() {
        SimulationResult light = new MemorySystemModel(new SystemParameters(50.0, 2.0, 30), 3).run(200_000, 0);
        assertEquals(0, light.refused());
        assertEquals(light.arrivals(), light.admitted() + light.refused());

        SimulationResult heavy = new MemorySystemModel(new SystemParameters(3.0, 2.0, 30), 3).run(200_000, 0);
        assertTrue(heavy.refusalProbability() > 0.3);
    }

    /** За малого навантаження черг немає, тож середня зайнята пам'ять = λ·(1·5 + 2·5 + 3·4,5). */
    @Test
    void lightLoadMatchesLittleLawForPages() {
        double interval = 50.0;
        SimulationResult result = new MemorySystemModel(new SystemParameters(interval, 2.0, 30), 11)
                .run(2_000_000, 0);
        double expected = (1 * 5.0 + 2 * 5.0 + 3 * 4.5) / interval;
        assertEquals(expected, result.meanOccupiedPages(), 0.01);
    }
}
