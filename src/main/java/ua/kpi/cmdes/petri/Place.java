package ua.kpi.cmdes.petri;

/** Позиція мережі Петрі з кількістю однакових маркерів. */
public final class Place {

    private final String name;
    private int marking;

    public Place(String name, int initialMarking) {
        if (initialMarking < 0) {
            throw new IllegalArgumentException("Negative marking of place " + name);
        }
        this.name = name;
        this.marking = initialMarking;
    }

    public String name() {
        return name;
    }

    public int marking() {
        return marking;
    }

    void add(int tokens) {
        marking += tokens;
    }

    void remove(int tokens) {
        if (tokens > marking) {
            throw new IllegalStateException("Not enough tokens in place " + name);
        }
        marking -= tokens;
    }

    @Override
    public String toString() {
        return name + "=" + marking;
    }
}
