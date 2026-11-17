package ua.kpi.cmdes.petri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Мережа Петрі: множина позицій і переходів, з'єднаних дугами. */
public final class PetriNet {

    private final Set<Place> places = new HashSet<>();
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

    public List<Transition> transitions() {
        return List.copyOf(transitions);
    }

    /**
     * Перевірка правильності конструювання: мережа має хоча б один перехід, кожен перехід має
     * хоча б одну вхідну та одну вихідну позицію, а дуги з'єднують переходи лише з позиціями цієї мережі.
     */
    public void validate() {
        if (transitions.isEmpty()) {
            throw new IllegalStateException("Мережа не має жодного переходу");
        }
        for (Transition transition : transitions) {
            if (transition.inputs().isEmpty() || transition.outputs().isEmpty()) {
                throw new IllegalStateException("Перехід «" + transition + "» не має вхідної або вихідної позиції");
            }
            for (List<Arc> arcs : List.of(transition.inputs(), transition.outputs())) {
                for (Arc arc : arcs) {
                    if (!places.contains(arc.place())) {
                        throw new IllegalStateException("Дуга переходу «" + transition
                                + "» веде до позиції поза мережею: " + arc.place());
                    }
                }
            }
        }
    }
}
