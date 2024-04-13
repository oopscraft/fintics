package org.oopscraft.fintics.indicator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.oopscraft.fintics.model.Ohlcv;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCalculatorTest {

    protected final List<Map<String,String>> readTsv(String filePath, String[] columnNames) {
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
                            String columnValue = record.get(columnName);
                            map.put(columnName, columnValue);
                        }
                        list.add(map);
                    });
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    protected final List<Ohlcv> convertOhlcvs(List<Map<String,String>> rows, String dateTimeNameAndPattern, String openPriceName, String highPriceName, String lowPriceName, String closePriceName, String volumeName) {
        String dateTimeName = null;
        String dateTimePattern = null;
        if(dateTimeNameAndPattern != null && dateTimeNameAndPattern.contains("^")) {
            String[] array = dateTimeNameAndPattern.split("\\^");
            dateTimeName = array[0];
            dateTimePattern = array[1];
        }
        String finalDateTimeName = dateTimeName;
        String finalDateTimePattern = dateTimePattern;
        return rows.stream()
                .map(row -> {
                    LocalDateTime dateTime = null;
                    if(finalDateTimeName != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(finalDateTimePattern);
                            Date date = sdf.parse(row.get(finalDateTimeName));
                            dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        } catch (ParseException ignored) {}
                    }
                    return Ohlcv.builder()
                            .dateTime(dateTime)
                            .openPrice(new BigDecimal(row.get(openPriceName).replaceAll(",","")))
                            .highPrice(new BigDecimal(row.get(highPriceName).replaceAll(",","")))
                            .lowPrice(new BigDecimal(row.get(lowPriceName).replaceAll(",","")))
                            .closePrice(new BigDecimal(row.get(closePriceName).replaceAll(",","")))
                            .volume(Optional.ofNullable(row.get(volumeName))
                                    .map(value -> new BigDecimal(value.replaceAll(",","")))
                                    .orElse(BigDecimal.ZERO))
                            .build();
                })
                .collect(Collectors.toList());
    }

}
