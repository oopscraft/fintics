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

    public BigDecimal slope(List<BigDecimal> values) {
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        // check empty
        if(series.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // sum
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            BigDecimal change = series.get(i)
                    .subtract(series.get(Math.max(i-1,0)));
            sum = sum.add(change);
        }

        // average
        return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
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
