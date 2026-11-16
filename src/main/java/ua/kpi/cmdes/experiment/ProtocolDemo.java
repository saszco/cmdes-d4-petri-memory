package ua.kpi.cmdes.experiment;

import ua.kpi.cmdes.model.EventProtocol;
import ua.kpi.cmdes.model.MemorySystemModel;
import ua.kpi.cmdes.model.SystemParameters;

/**
 * Протокол подій для верифікації. Малий обсяг пам'яті та високе навантаження обрано, щоб у
 * короткому фрагменті траплялися відмови й блокування процесорів.
 */
public final class ProtocolDemo {

    public static void main(String[] args) {
        double untilTime = args.length > 0 ? Double.parseDouble(args[0]) : 40;
        MemorySystemModel model = new MemorySystemModel(new SystemParameters(3, 2, 6), 5);
        model.addListener(new EventProtocol(model.net(), System.out, untilTime));
        model.run(untilTime, 0);
    }
}
