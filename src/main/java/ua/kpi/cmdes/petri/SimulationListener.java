package ua.kpi.cmdes.petri;

/** Спостерігач за імітацією: збір статистики та протокол подій. */
public interface SimulationListener {

    /** Тип події переходу. */
    enum EventType { ENTER, EXIT, FIRE }

    /** Стан мережі не змінюється на інтервалі [from; to). */
    default void onTimeAdvance(double from, double to) {
    }

    /** ENTER і EXIT -- вхід і вихід маркерів часового переходу, FIRE -- запуск миттєвого. */
    default void onEvent(EventType type, Transition transition, double time) {
    }
}
