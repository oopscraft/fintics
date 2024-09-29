package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.RealizedProfit;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealizedProfitService {

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    /**
     * returns realized profits
     * @param brokerId trade id
     * @param dateFrom date from
     * @param dateTo date to
     * @return list of realized profit
     */
    public RealizedProfit getRealizedProfit(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Optional.ofNullable(dateFrom).orElse(LocalDate.now().minusMonths(1));
        dateTo = Optional.ofNullable(dateTo).orElse(LocalDate.now());
        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        try {
            return brokerClient.getRealizedProfit(dateFrom, dateTo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
