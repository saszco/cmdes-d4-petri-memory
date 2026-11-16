package ua.kpi.cmdes.experiment;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Запис результатів експериментів у текстові файли, з яких будуються графіки. */
final class DataFiles {

    /** Тека результатів: властивість {@code results.dir}, за замовчуванням {@code results}. */
    static final Path RESULTS = Path.of(System.getProperty("results.dir", "results"));

    private DataFiles() {
    }

    static void write(String fileName, String header, List<String> rows) {
        try {
            Files.createDirectories(RESULTS);
            Files.write(RESULTS.resolve(fileName), Stream.concat(Stream.of(header), rows.stream()).toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
