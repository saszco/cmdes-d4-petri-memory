package ua.kpi.cmdes.model;

import ua.kpi.cmdes.petri.Delay;
import ua.kpi.cmdes.petri.PetriNet;
import ua.kpi.cmdes.petri.Place;
import ua.kpi.cmdes.petri.Transition;

/**
 * Стохастична мережа Петрі обчислювальної системи з трьома процесорами та спільною пам'яттю.
 * Вільні сторінки пам'яті -- маркери позиції «Вільна ОП». Поріг k вільних сторінок реалізовано
 * вхідною дугою кратності k з «Вільна ОП» та вихідною дугою кратності k - 1 назад до неї:
 * перехід запускається лише за наявності k вільних сторінок, а займає рівно одну.
 */
public final class MemorySystemNet {

    static final int PAGES_AFTER_EDITING = 3;

    private final PetriNet net = new PetriNet();
    private final int memoryPages;

    final Place generator;
    final Place arrived;
    final Place freeMemory;
    final Place queue1;
    final Place cpu1Free;
    final Place translated;
    final Place queue2;
    final Place cpu2Free;
    final Place edited;
    final Place queue3;
    final Place cpu3Free;
    final Place admitted;
    final Place refused;
    final Place completed;

    final Transition arrival;
    final Transition admission;
    final Transition refusal;
    final Transition translation;
    final Transition translationGrowth;
    final Transition editing;
    final Transition editingGrowth;
    final Transition processing;

    public MemorySystemNet(SystemParameters parameters) {
        memoryPages = parameters.memoryPages();
        PageThresholds thresholds = parameters.thresholds();

        generator = net.place("Генератор", 1);
        arrived = net.place("Надійшло", 0);
        freeMemory = net.place("Вільна ОП", memoryPages);
        queue1 = net.place("Черга P1", 0);
        cpu1Free = net.place("P1 вільний", 1);
        translated = net.place("Трансльовано", 0);
        queue2 = net.place("Черга P2", 0);
        cpu2Free = net.place("P2 вільний", 1);
        edited = net.place("Відредаговано", 0);
        queue3 = net.place("Черга P3", 0);
        cpu3Free = net.place("P3 вільний", 1);
        admitted = net.place("Допущено", 0);
        refused = net.place("Відмовлено", 0);
        completed = net.place("Оброблено", 0);

        double mean = parameters.meanArrivalInterval();
        double halfWidth = parameters.arrivalHalfWidth();
        ProcessingScale scale = parameters.processingScale();
        boolean fixed = parameters.deterministic();
        arrival = net.add(Transition.timed("Надходження", delay(1, mean - halfWidth, mean + halfWidth, fixed))
                .in(generator).out(generator).out(arrived));
        admission = net.add(Transition.immediate("Допуск", 1)
                .in(arrived).in(freeMemory, thresholds.admission()).out(admitted));
        returnPages(admission.out(queue1), thresholds.admission());
        refusal = net.add(Transition.immediate("Відмова", 0)
                .in(arrived).out(refused));
        double s1 = scale.translation();
        translation = net.add(Transition.timed("Трансляція", delay(1, 4.0 * s1, 6.0 * s1, fixed))
                .in(queue1).in(cpu1Free).out(translated));
        translationGrowth = net.add(Transition.immediate("Зростання до 2 стор.", 2)
                .in(translated).in(freeMemory, thresholds.afterTranslation()));
        returnPages(translationGrowth.out(queue2).out(cpu1Free), thresholds.afterTranslation());
        double s2 = scale.editing();
        editing = net.add(Transition.timed("Редагування", delay(2, 2.0 * s2, 3.0 * s2, fixed))
                .in(queue2).in(cpu2Free).out(edited));
        editingGrowth = net.add(Transition.immediate("Зростання до 3 стор.", 3)
                .in(edited).in(freeMemory, thresholds.afterEditing()));
        returnPages(editingGrowth.out(queue3).out(cpu2Free), thresholds.afterEditing());
        double s3 = scale.processing();
        processing = net.add(Transition.timed("Обробка", delay(PAGES_AFTER_EDITING, 1.1 * s3, 1.9 * s3, fixed))
                .in(queue3).in(cpu3Free).out(cpu3Free).out(freeMemory, PAGES_AFTER_EDITING).out(completed));
    }

    /**
     * Затримка як сума {@code pages} рівномірних величин на (min; max), по одній на сторінку;
     * детермінована затримка дорівнює середньому значенню.
     */
    private static Delay delay(int pages, double min, double max, boolean deterministic) {
        if (deterministic || min == max) {
            return Delay.constant(pages * (min + max) / 2);
        }
        return pages == 1 ? Delay.uniform(min, max) : Delay.uniformSum(pages, min, max);
    }

    /** Вихідна дуга кратності threshold - 1 повертає до «Вільна ОП» сторінки, лише перевірені порогом. */
    private void returnPages(Transition transition, int threshold) {
        if (threshold > 1) {
            transition.out(freeMemory, threshold - 1);
        }
    }

    public PetriNet net() {
        return net;
    }

    public int memoryPages() {
        return memoryPages;
    }

    public int occupiedPages() {
        return memoryPages - freeMemory.marking();
    }

    /** Кількість завдань, що перебувають у пам'яті (у чергах і на процесорах). */
    public int jobsInSystem() {
        return queue1.marking() + queue2.marking() + queue3.marking()
                + busy(cpu1Free) + busy(cpu2Free) + busy(cpu3Free);
    }

    /**
     * Інваріант позицій: сторінки, зайняті завданнями на кожному етапі, разом із вільними
     * дорівнюють обсягу пам'яті.
     */
    public boolean memoryInvariantHolds() {
        int pagesOfJobs = queue1.marking() + busy(cpu1Free)
                + 2 * (queue2.marking() + busy(cpu2Free))
                + 3 * (queue3.marking() + busy(cpu3Free));
        return pagesOfJobs + freeMemory.marking() == memoryPages;
    }

    static int busy(Place processorFree) {
        return 1 - processorFree.marking();
    }
}
