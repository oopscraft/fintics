package org.oopscraft.fintics.calculator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.oopscraft.arch4j.core.support.ValueMap;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractCalculatorTest {

    public final List<Map<String,String>> readTsv(String filePath, String[] columnNames) {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader(columnNames)
                .setSkipHeaderRecord(true)
                .build();
        List<Map<String,String>> list = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        Map<String,String> map = new LinkedHashMap<>();
                        for(String columnName : columnNames) {
                            map.put(columnName, record.get(columnName));
                        }
                        list.add(map);
                    });
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }

}
