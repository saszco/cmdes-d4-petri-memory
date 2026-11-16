package ua.kpi.cmdes.petri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

import ua.kpi.cmdes.petri.SimulationListener.EventType;

/**
 * Алгоритм імітації стохастичної мережі Петрі з просуванням часу до найближчої події:
 * вихід маркерів з переходів, момент виходу яких настав, потім вхід маркерів у переходи,
 * доки існують переходи з виконаною умовою запуску.
 */
public final class PetriSimulator {

    private final List<Transition> transitions;
    private final RandomGenerator random;
    private final List<SimulationListener> listeners = new ArrayList<>();
    private double currentTime;

    public PetriSimulator(PetriNet net, RandomGenerator random) {
        this.transitions = net.transitions();
        this.random = random;
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public double currentTime() {
        return currentTime;
    }

    /** Імітує функціонування мережі на інтервалі [0; endTime]. */
    public void run(double endTime) {
        enterEnabledTransitions();
        while (true) {
            double nextEvent = nearestEventTime();
            double until = Math.min(nextEvent, endTime);
            for (SimulationListener listener : listeners) {
                listener.onTimeAdvance(currentTime, until);
            }
            currentTime = until;
            if (nextEvent > endTime) {
                return;
            }
            exitTransitionsAt(nextEvent);
            enterEnabledTransitions();
        }
    }

    private double nearestEventTime() {
        double nearest = Double.POSITIVE_INFINITY;
        for (Transition transition : transitions) {
            nearest = Math.min(nearest, transition.exitTime());
        }
        return nearest;
    }

    /** Вихід маркерів з усіх переходів, момент виходу яких збігся з поточним, у випадковому порядку. */
    private void exitTransitionsAt(double time) {
        List<Transition> due = new ArrayList<>();
        for (Transition transition : transitions) {
            if (transition.exitTime() == time) {
                due.add(transition);
            }
        }
        Collections.shuffle(due, random);
        for (Transition transition : due) {
            transition.exit();
            notify(EventType.EXIT, transition);
        }
    }

    /** Вхід маркерів у переходи, доки умова запуску виконана хоча б для одного з них. */
    private void enterEnabledTransitions() {
        Transition winner;
        while ((winner = resolveConflict()) != null) {
            winner.enter(currentTime, random);
            notify(winner.isImmediate() ? EventType.FIRE : EventType.ENTER, winner);
        }
    }

    /** Серед переходів з виконаною умовою запуску обирає перехід з найвищим пріоритетом. */
    private Transition resolveConflict() {
        List<Transition> best = new ArrayList<>();
        for (Transition transition : transitions) {
            if (!transition.isEnabled()) {
                continue;
            }
            if (!best.isEmpty() && transition.priority() > best.getFirst().priority()) {
                best.clear();
            }
            if (best.isEmpty() || transition.priority() == best.getFirst().priority()) {
                best.add(transition);
            }
        }
        if (best.isEmpty()) {
            return null;
        }
        return best.size() == 1 ? best.getFirst() : best.get(random.nextInt(best.size()));
    }

    private void notify(EventType type, Transition transition) {
        for (SimulationListener listener : listeners) {
            listener.onEvent(type, transition, currentTime);
        }
    }
}
