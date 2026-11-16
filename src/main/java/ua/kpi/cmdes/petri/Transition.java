package ua.kpi.cmdes.petri;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Перехід мережі Петрі. Часовий перехід одноканальний: зберігає лише один момент виходу маркерів.
 * Миттєвий перехід (без затримки) виконує вхід і вихід маркерів в один момент.
 */
public final class Transition {

    private static final double NO_EXIT = Double.POSITIVE_INFINITY;

    private final String name;
    private final Delay delay;
    private final int priority;
    private final List<Arc> inputs = new ArrayList<>();
    private final List<Arc> outputs = new ArrayList<>();
    private double exitTime = NO_EXIT;

    private Transition(String name, Delay delay, int priority) {
        this.name = name;
        this.delay = delay;
        this.priority = priority;
    }

    public static Transition timed(String name, Delay delay) {
        return new Transition(name, delay, 0);
    }

    public static Transition immediate(String name, int priority) {
        return new Transition(name, null, priority);
    }

    public Transition in(Place place, int multiplicity) {
        inputs.add(new Arc(place, multiplicity));
        return this;
    }

    public Transition in(Place place) {
        return in(place, 1);
    }

    public Transition out(Place place, int multiplicity) {
        outputs.add(new Arc(place, multiplicity));
        return this;
    }

    public Transition out(Place place) {
        return out(place, 1);
    }

    public String name() {
        return name;
    }

    public int priority() {
        return priority;
    }

    public boolean isImmediate() {
        return delay == null;
    }

    public boolean isBusy() {
        return exitTime != NO_EXIT;
    }

    public double exitTime() {
        return exitTime;
    }

    public List<Arc> inputs() {
        return List.copyOf(inputs);
    }

    public List<Arc> outputs() {
        return List.copyOf(outputs);
    }

    /** Умова запуску: перехід вільний, а у вхідних позиціях достатньо маркерів. */
    boolean isEnabled() {
        if (isBusy()) {
            return false;
        }
        for (Arc arc : inputs) {
            if (arc.place().marking() < arc.multiplicity()) {
                return false;
            }
        }
        return true;
    }

    /** Вхід маркерів: для часового переходу планується момент виходу. */
    void enter(double currentTime, RandomGenerator random) {
        inputs.forEach(arc -> arc.place().remove(arc.multiplicity()));
        if (isImmediate()) {
            exit();
        } else {
            exitTime = currentTime + delay.next(random);
        }
    }

    /** Вихід маркерів у вихідні позиції. */
    void exit() {
        outputs.forEach(arc -> arc.place().add(arc.multiplicity()));
        exitTime = NO_EXIT;
    }

    @Override
    public String toString() {
        return name;
    }
}
