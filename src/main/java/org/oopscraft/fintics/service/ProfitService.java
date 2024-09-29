package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.DividendHistory;
import org.oopscraft.fintics.model.Profit;
import org.oopscraft.fintics.model.RealizedProfit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfitService {

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    /**
     * returns profit
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return profit
     */
    public Profit getProfit(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Optional.ofNullable(dateFrom).orElse(LocalDate.now().minusMonths(1));
        dateTo = Optional.ofNullable(dateTo).orElse(LocalDate.now());
        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        List<RealizedProfit> realizedProfits;
        List<DividendHistory> dividendHistories;
        try {
            realizedProfits = brokerClient.getRealizedProfits(dateFrom, dateTo);
            dividendHistories = brokerClient.getDividendHistories(dateFrom, dateTo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // profit amount
        BigDecimal realizedProfitAmount = realizedProfits.stream()
                .map(RealizedProfit::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dividendAmount = dividendHistories.stream()
                .map(DividendHistory::getDividendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfitAmount = realizedProfitAmount
                .add(dividendAmount);

        // returns
        return Profit.builder()
                .totalProfitAmount(totalProfitAmount)
                .realizedProfitAmount(realizedProfitAmount)
                .dividendAmount(dividendAmount)
                .realizedProfits(realizedProfits)
                .dividendHistories(dividendHistories)
                .build();
    }

}
