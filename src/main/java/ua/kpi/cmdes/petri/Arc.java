package ua.kpi.cmdes.petri;

/** Дуга між позицією та переходом із кратністю {@code multiplicity}. */
public record Arc(Place place, int multiplicity) {

    public Arc {
        if (multiplicity < 1) {
            throw new IllegalArgumentException("Arc multiplicity must be positive");
        }
    }
}
