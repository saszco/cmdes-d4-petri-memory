package ua.kpi.cmdes.petri;

import java.util.ArrayList;
import java.util.List;

/** Мережа Петрі: множина позицій і переходів, з'єднаних дугами. */
public final class PetriNet {

    private final List<Place> places = new ArrayList<>();
    private final List<Transition> transitions = new ArrayList<>();

    public Place place(String name, int initialMarking) {
        Place place = new Place(name, initialMarking);
        places.add(place);
        return place;
    }

    public Transition add(Transition transition) {
        transitions.add(transition);
        return transition;
    }

    public List<Place> places() {
        return List.copyOf(places);
    }

    public List<Transition> transitions() {
        return List.copyOf(transitions);
    }
}
