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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-12-30T00:00:00+01:00\",\"prijsZP\":\"0.242600\"},{\"datum\":\"2026-12-30T01:00:00+01:00\",\"prijsZP\":\"0.242823\"},{\"datum\":\"2026-12-30T02:00:00+01:00\",\"prijsZP\":\"0.240246\"},{\"datum\":\"2026-12-30T03:00:00+01:00\",\"prijsZP\":\"0.241202\"},{\"datum\":\"2026-12-30T04:00:00+01:00\",\"prijsZP\":\"0.244726\"},{\"datum\":\"2026-12-30T05:00:00+01:00\",\"prijsZP\":\"0.244832\"},{\"datum\":\"2026-12-30T06:00:00+01:00\",\"prijsZP\":\"0.246807\"},{\"datum\":\"2026-12-30T07:00:00+01:00\",\"prijsZP\":\"0.317027\"},{\"datum\":\"2026-12-30T08:00:00+01:00\",\"prijsZP\":\"0.356709\"},{\"datum\":\"2026-12-30T09:00:00+01:00\",\"prijsZP\":\"0.330070\"},{\"datum\":\"2026-12-30T10:00:00+01:00\",\"prijsZP\":\"0.276395\"},{\"datum\":\"2026-12-30T11:00:00+01:00\",\"prijsZP\":\"0.246335\"},{\"datum\":\"2026-12-30T12:00:00+01:00\",\"prijsZP\":\"0.239266\"},{\"datum\":\"2026-12-30T13:00:00+01:00\",\"prijsZP\":\"0.241492\"},{\"datum\":\"2026-12-30T14:00:00+01:00\",\"prijsZP\":\"0.255625\"},{\"datum\":\"2026-12-30T15:00:00+01:00\",\"prijsZP\":\"0.285573\"},{\"datum\":\"2026-12-30T16:00:00+01:00\",\"prijsZP\":\"0.336550\"},{\"datum\":\"2026-12-30T17:00:00+01:00\",\"prijsZP\":\"0.413887\"},{\"datum\":\"2026-12-30T18:00:00+01:00\",\"prijsZP\":\"0.347443\"},{\"datum\":\"2026-12-30T19:00:00+01:00\",\"prijsZP\":\"0.293338\"},{\"datum\":\"2026-12-30T20:00:00+01:00\",\"prijsZP\":\"0.282100\"},{\"datum\":\"2026-12-30T21:00:00+01:00\",\"prijsZP\":\"0.268669\"},{\"datum\":\"2026-12-30T22:00:00+01:00\",\"prijsZP\":\"0.261309\"},{\"datum\":\"2026-12-30T23:00:00+01:00\",\"prijsZP\":\"0.250310\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-01-31T00:00:00+01:00\",\"prijsZP\":\"0.242600\"},{\"datum\":\"2026-01-31T01:00:00+01:00\",\"prijsZP\":\"0.242823\"},{\"datum\":\"2026-01-31T02:00:00+01:00\",\"prijsZP\":\"0.240246\"},{\"datum\":\"2026-01-31T03:00:00+01:00\",\"prijsZP\":\"0.241202\"},{\"datum\":\"2026-01-31T04:00:00+01:00\",\"prijsZP\":\"0.244726\"},{\"datum\":\"2026-01-31T05:00:00+01:00\",\"prijsZP\":\"0.244832\"},{\"datum\":\"2026-01-31T06:00:00+01:00\",\"prijsZP\":\"0.246807\"},{\"datum\":\"2026-01-31T07:00:00+01:00\",\"prijsZP\":\"0.317027\"},{\"datum\":\"2026-01-31T08:00:00+01:00\",\"prijsZP\":\"0.356709\"},{\"datum\":\"2026-01-31T09:00:00+01:00\",\"prijsZP\":\"0.330070\"},{\"datum\":\"2026-01-31T10:00:00+01:00\",\"prijsZP\":\"0.276395\"},{\"datum\":\"2026-01-31T11:00:00+01:00\",\"prijsZP\":\"0.246335\"},{\"datum\":\"2026-01-31T12:00:00+01:00\",\"prijsZP\":\"0.239266\"},{\"datum\":\"2026-01-31T13:00:00+01:00\",\"prijsZP\":\"0.241492\"},{\"datum\":\"2026-01-31T14:00:00+01:00\",\"prijsZP\":\"0.255625\"},{\"datum\":\"2026-01-31T15:00:00+01:00\",\"prijsZP\":\"0.285573\"},{\"datum\":\"2026-01-31T16:00:00+01:00\",\"prijsZP\":\"0.336550\"},{\"datum\":\"2026-01-31T17:00:00+01:00\",\"prijsZP\":\"0.413887\"},{\"datum\":\"2026-01-31T18:00:00+01:00\",\"prijsZP\":\"0.347443\"},{\"datum\":\"2026-01-31T19:00:00+01:00\",\"prijsZP\":\"0.293338\"},{\"datum\":\"2026-01-31T20:00:00+01:00\",\"prijsZP\":\"0.282100\"},{\"datum\":\"2026-01-31T21:00:00+01:00\",\"prijsZP\":\"0.268669\"},{\"datum\":\"2026-01-31T22:00:00+01:00\",\"prijsZP\":\"0.261309\"},{\"datum\":\"2026-01-31T23:00:00+01:00\",\"prijsZP\":\"0.250310\"}],\"code\":\"5\"}";

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
