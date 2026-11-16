package ua.kpi.cmdes.model;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import ua.kpi.cmdes.petri.Place;
import ua.kpi.cmdes.petri.SimulationListener;
import ua.kpi.cmdes.petri.Transition;

/**
 * Протокол подій у вигляді таблиці: момент часу, подія, перехід, запланований момент виходу
 * та маркірування позицій після події.
 */
public final class EventProtocol implements SimulationListener {

    private static final String ROW_START = "%8s  %-6s %-20s %7s";
    private static final String ROW_END = "%5s%4s%3s";
    private static final List<String> COLUMN_NAMES =
            List.of("Нд", "ВОП", "Ч1", "С1", "Тр", "Ч2", "С2", "Вр", "Ч3", "С3");

    private final MemorySystemNet net;
    private final List<Place> columns;
    private final PrintStream out;
    private final double untilTime;
    private boolean headerPrinted;

    public EventProtocol(MemorySystemNet net, PrintStream out, double untilTime) {
        this.net = net;
        this.columns = List.of(net.arrived, net.freeMemory, net.queue1, net.cpu1Free, net.translated,
                net.queue2, net.cpu2Free, net.edited, net.queue3, net.cpu3Free);
        this.out = out;
        this.untilTime = untilTime;
    }

    @Override
    public void onEvent(EventType type, Transition transition, double time) {
        if (time > untilTime) {
            return;
        }
        if (!headerPrinted) {
            printHeader();
            headerPrinted = true;
        }
        String event = switch (type) {
            case ENTER -> "вхід";
            case EXIT -> "вихід";
            case FIRE -> "запуск";
        };
        String exit = transition.isBusy() ? String.format(Locale.US, "%.3f", transition.exitTime()) : "";
        StringBuilder row = new StringBuilder(String.format(Locale.US, ROW_START,
                String.format(Locale.US, "%.3f", time), event, transition.name(), exit));
        for (int i = 0; i < columns.size(); i++) {
            row.append(String.format(columnFormat(i, "d"), columns.get(i).marking()));
        }
        row.append(String.format(ROW_END, net.refused.marking(), net.completed.marking(), net.occupiedPages()));
        out.println(row);
    }

    private void printHeader() {
        StringBuilder header = new StringBuilder(String.format(ROW_START, "t", "подія", "перехід", "вихід о"));
        for (int i = 0; i < COLUMN_NAMES.size(); i++) {
            header.append(String.format(columnFormat(i, "s"), COLUMN_NAMES.get(i)));
        }
        header.append(String.format(ROW_END, "Відм", "Обр", "L"));
        out.println(header);
    }

    /** Колонка «ВОП» ширша за інші, бо її назва має три літери. */
    private static String columnFormat(int index, String conversion) {
        return (index == 1 ? "%4" : "%3") + conversion;
    }
}
