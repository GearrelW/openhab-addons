package org.openhab.binding.enever.internal;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openhab.binding.enever.internal.payloads.EneVerPayload;
import org.openhab.binding.enever.internal.payloads.PayloadPriceItem;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EPricesTests {

    private Logger logger = Logger.getLogger(EPricesTests.class.getName());
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-03-17T00:00:00+01:00\",\"prijsZP\":\"0.267852\"},{\"datum\":\"2026-03-17T01:00:00+01:00\",\"prijsZP\":\"0.257658\"},{\"datum\":\"2026-03-17T02:00:00+01:00\",\"prijsZP\":\"0.258148\"},{\"datum\":\"2026-03-17T03:00:00+01:00\",\"prijsZP\":\"0.256781\"},{\"datum\":\"2026-03-17T04:00:00+01:00\",\"prijsZP\":\"0.261917\"},{\"datum\":\"2026-03-17T05:00:00+01:00\",\"prijsZP\":\"0.272287\"},{\"datum\":\"2026-03-17T06:00:00+01:00\",\"prijsZP\":\"0.304313\"},{\"datum\":\"2026-03-17T07:00:00+01:00\",\"prijsZP\":\"0.331904\"},{\"datum\":\"2026-03-17T08:00:00+01:00\",\"prijsZP\":\"0.314350\"},{\"datum\":\"2026-03-17T09:00:00+01:00\",\"prijsZP\":\"0.276879\"},{\"datum\":\"2026-03-17T10:00:00+01:00\",\"prijsZP\":\"0.255205\"},{\"datum\":\"2026-03-17T11:00:00+01:00\",\"prijsZP\":\"0.239154\"},{\"datum\":\"2026-03-17T12:00:00+01:00\",\"prijsZP\":\"0.231020\"},{\"datum\":\"2026-03-17T13:00:00+01:00\",\"prijsZP\":\"0.232390\"},{\"datum\":\"2026-03-17T14:00:00+01:00\",\"prijsZP\":\"0.236171\"},{\"datum\":\"2026-03-17T15:00:00+01:00\",\"prijsZP\":\"0.250619\"},{\"datum\":\"2026-03-17T16:00:00+01:00\",\"prijsZP\":\"0.273397\"},{\"datum\":\"2026-03-17T17:00:00+01:00\",\"prijsZP\":\"0.311597\"},{\"datum\":\"2026-03-17T18:00:00+01:00\",\"prijsZP\":\"0.383713\"},{\"datum\":\"2026-03-17T19:00:00+01:00\",\"prijsZP\":\"0.337055\"},{\"datum\":\"2026-03-17T20:00:00+01:00\",\"prijsZP\":\"0.302667\"},{\"datum\":\"2026-03-17T21:00:00+01:00\",\"prijsZP\":\"0.279184\"},{\"datum\":\"2026-03-17T22:00:00+01:00\",\"prijsZP\":\"0.267701\"},{\"datum\":\"2026-03-17T23:00:00+01:00\",\"prijsZP\":\"0.259815\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-03-17T00:00:00+01:00\",\"prijsZP\":\"0.267852\"},{\"datum\":\"2026-03-17T01:00:00+01:00\",\"prijsZP\":\"0.257658\"},{\"datum\":\"2026-03-17T02:00:00+01:00\",\"prijsZP\":\"0.258148\"},{\"datum\":\"2026-03-17T03:00:00+01:00\",\"prijsZP\":\"0.256781\"},{\"datum\":\"2026-03-17T04:00:00+01:00\",\"prijsZP\":\"0.261917\"},{\"datum\":\"2026-03-17T05:00:00+01:00\",\"prijsZP\":\"0.272287\"},{\"datum\":\"2026-03-17T06:00:00+01:00\",\"prijsZP\":\"0.304313\"},{\"datum\":\"2026-03-17T07:00:00+01:00\",\"prijsZP\":\"0.331904\"},{\"datum\":\"2026-03-17T08:00:00+01:00\",\"prijsZP\":\"0.314350\"},{\"datum\":\"2026-03-17T09:00:00+01:00\",\"prijsZP\":\"0.276879\"},{\"datum\":\"2026-03-17T10:00:00+01:00\",\"prijsZP\":\"0.255205\"},{\"datum\":\"2026-03-17T11:00:00+01:00\",\"prijsZP\":\"0.239154\"},{\"datum\":\"2026-03-17T12:00:00+01:00\",\"prijsZP\":\"0.231020\"},{\"datum\":\"2026-03-17T13:00:00+01:00\",\"prijsZP\":\"0.232390\"},{\"datum\":\"2026-03-17T14:00:00+01:00\",\"prijsZP\":\"0.236171\"},{\"datum\":\"2026-03-17T15:00:00+01:00\",\"prijsZP\":\"0.250619\"},{\"datum\":\"2026-03-17T16:00:00+01:00\",\"prijsZP\":\"0.273397\"},{\"datum\":\"2026-03-17T17:00:00+01:00\",\"prijsZP\":\"0.311597\"},{\"datum\":\"2026-03-17T18:00:00+01:00\",\"prijsZP\":\"0.383713\"},{\"datum\":\"2026-03-17T19:00:00+01:00\",\"prijsZP\":\"0.337055\"},{\"datum\":\"2026-03-17T20:00:00+01:00\",\"prijsZP\":\"0.302667\"},{\"datum\":\"2026-03-17T21:00:00+01:00\",\"prijsZP\":\"0.279184\"},{\"datum\":\"2026-03-17T22:00:00+01:00\",\"prijsZP\":\"0.267701\"},{\"datum\":\"2026-03-17T23:00:00+01:00\",\"prijsZP\":\"0.259815\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 4);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);
        prices.processPrices();

        logger.info("Strategy: " + prices.controlStrategy);
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }

    // @Test
    public void testProcessPricesSolar() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);

        logger.info("mode: " + prices.controlStrategy);
        logger.info("Prices: " + prices.getAllPrices().toString());
    }

    // @Test
    public void testSetMode() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);

        logger.info("mode: " + prices.controlStrategy);
        prices.controlStrategy = "prices";
        logger.info("set mode: " + prices.controlStrategy);
        logger.info("mode: " + prices.controlStrategy);
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }
}
