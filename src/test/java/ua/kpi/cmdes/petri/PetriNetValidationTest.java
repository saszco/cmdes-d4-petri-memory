package ua.kpi.cmdes.petri;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

class PetriNetValidationTest {

    @Test
    void netWithoutTransitionsIsRejected() {
        PetriNet net = new PetriNet();
        net.place("P", 1);
        assertThrows(IllegalStateException.class, () -> new PetriSimulator(net, new SplittableRandom(1)));
    }

    @Test
    void transitionWithoutInputIsRejected() {
        PetriNet net = new PetriNet();
        Place place = net.place("P", 0);
        net.add(Transition.timed("T", Delay.constant(1)).out(place));
        assertThrows(IllegalStateException.class, () -> new PetriSimulator(net, new SplittableRandom(1)));
    }

    @Test
    void arcToForeignPlaceIsRejected() {
        PetriNet net = new PetriNet();
        Place place = net.place("P", 1);
        net.add(Transition.timed("T", Delay.constant(1)).in(place).out(new Place("Q", 0)));
        assertThrows(IllegalStateException.class, () -> new PetriSimulator(net, new SplittableRandom(1)));
    }

    @Test
    void negativeDelayStopsSimulation() {
        PetriNet net = new PetriNet();
        Place place = net.place("P", 1);
        net.add(Transition.timed("T", random -> -0.5).in(place).out(place));
        PetriSimulator simulator = new PetriSimulator(net, new SplittableRandom(1));
        assertThrows(IllegalStateException.class, () -> simulator.run(10));
    }
}
