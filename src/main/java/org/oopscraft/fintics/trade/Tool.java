package org.oopscraft.fintics.trade;

import com.mitchtalmadge.asciidata.graph.ASCIIGraph;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Tool {

    public List<BigDecimal> pctChanges(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        List<BigDecimal> pctChanges = new ArrayList<>();
        pctChanges.add(BigDecimal.ZERO);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal current = series.get(i);
            BigDecimal previous = series.get(i - 1);
            if(previous.compareTo(BigDecimal.ZERO) == 0) {
                if(current.compareTo(BigDecimal.ZERO) == 0) {
                    pctChanges.add(BigDecimal.ZERO);
                }else{
                    pctChanges.add(BigDecimal.valueOf(100));
                }
                continue;
            }
            BigDecimal pctChange = current.subtract(previous)
                    .divide(previous, MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100));
            pctChanges.add(pctChange);
        }

        Collections.reverse(pctChanges);
        return pctChanges;
    }

    public List<BigDecimal> zScores(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<BigDecimal>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        BigDecimal mean = mean(series);
        BigDecimal std = std(series);

        List<BigDecimal> zScores = new ArrayList<>();
        for(BigDecimal value : values) {
            if(std.compareTo(BigDecimal.ZERO) == 0) {
                zScores.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal zScore = value
                    .subtract(mean)
                    .divide(std, MathContext.DECIMAL32);
            zScores.add(zScore);
        }

        Collections.reverse(zScores);
        return zScores;
    }

    public BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal mean(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(values);
        return series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
    }

    public BigDecimal min(List<BigDecimal> values) {
        return values.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal max(List<BigDecimal> values) {
        return values.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal median(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        int size = sortedValues.size();
        if (size % 2 == 1) {
            return sortedValues.get(size / 2);
        } else {
            BigDecimal leftMiddle = sortedValues.get(size / 2 - 1);
            BigDecimal rightMiddle = sortedValues.get(size / 2);
            return leftMiddle.add(rightMiddle)
                    .divide(BigDecimal.valueOf(2), MathContext.DECIMAL32);
        }
    }

    public BigDecimal std(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);
        BigDecimal mean = series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
        BigDecimal sumSquaredDeviations = series.stream()
                .map(x -> x.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = sumSquaredDeviations
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    /**
     * creates simple ascii line chart
     * @param title chart title
     * @param values data point
     * @return ascii chart data
     */
    public Supplier<String> graph(String title, List<BigDecimal> values) {
        return new Supplier<String>() {
            public String get() {
                // make series
                List<BigDecimal> series = new ArrayList<>(values);
                Collections.reverse(series);

                // data to double array
                double[] doubles = series.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray();

                // create ascii graph
                String graph = ASCIIGraph.fromSeries(doubles).withNumRows(10).plot();
                return String.format("\n%s\n%s", title, graph);
            }

            @Override
            public String toString() {
                return get();
            }
        };
    }


}
