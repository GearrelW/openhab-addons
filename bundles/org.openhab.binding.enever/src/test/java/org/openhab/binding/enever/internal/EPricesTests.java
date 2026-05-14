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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2036-04-11T00:00:00+02:00\",\"prijsZP\":\"0.142472\"},{\"datum\":\"2036-04-11T01:00:00+02:00\",\"prijsZP\":\"0.135617\"},{\"datum\":\"2036-04-11T02:00:00+02:00\",\"prijsZP\":\"0.136349\"},{\"datum\":\"2036-04-11T03:00:00+02:00\",\"prijsZP\":\"0.134652\"},{\"datum\":\"2036-04-11T04:00:00+02:00\",\"prijsZP\":\"0.135194\"},{\"datum\":\"2036-04-11T05:00:00+02:00\",\"prijsZP\":\"0.151347\"},{\"datum\":\"2036-04-11T06:00:00+02:00\",\"prijsZP\":\"0.207712\"},{\"datum\":\"2036-04-11T07:00:00+02:00\",\"prijsZP\":\"0.243371\"},{\"datum\":\"2036-04-11T08:00:00+02:00\",\"prijsZP\":\"0.266370\"},{\"datum\":\"2036-04-11T09:00:00+02:00\",\"prijsZP\":\"0.250459\"},{\"datum\":\"2036-04-11T10:00:00+02:00\",\"prijsZP\":\"0.221700\"},{\"datum\":\"2036-04-11T11:00:00+02:00\",\"prijsZP\":\"0.186507\"},{\"datum\":\"2036-04-11T12:00:00+02:00\",\"prijsZP\":\"0.160350\"},{\"datum\":\"2036-04-11T13:00:00+02:00\",\"prijsZP\":\"0.143721\"},{\"datum\":\"2036-04-11T14:00:00+02:00\",\"prijsZP\":\"0.139989\"},{\"datum\":\"2036-04-11T15:00:00+02:00\",\"prijsZP\":\"0.146504\"},{\"datum\":\"2036-04-11T16:00:00+02:00\",\"prijsZP\":\"0.171612\"},{\"datum\":\"2036-04-11T17:00:00+02:00\",\"prijsZP\":\"0.215381\"},{\"datum\":\"2036-04-11T18:00:00+02:00\",\"prijsZP\":\"0.251850\"},{\"datum\":\"2036-04-11T19:00:00+02:00\",\"prijsZP\":\"0.282185\"},{\"datum\":\"2036-04-11T20:00:00+02:00\",\"prijsZP\":\"0.287566\"},{\"datum\":\"2036-04-11T21:00:00+02:00\",\"prijsZP\":\"0.288029\"},{\"datum\":\"2036-04-11T22:00:00+02:00\",\"prijsZP\":\"0.295138\"},{\"datum\":\"2036-04-11T23:00:00+02:00\",\"prijsZP\":\"0.270911\"}],\"code\":\"5\"}";
    // private String testDataE2 =
    // "{\"status\":\"true\",\"data\":[{\"datum\":\"2036-03-28T00:00:00+01:00\",\"prijsZP\":\"0.292442\"},{\"datum\":\"2036-03-28T01:00:00+01:00\",\"prijsZP\":\"0.286526\"},{\"datum\":\"2036-03-28T02:00:00+01:00\",\"prijsZP\":\"0.252466\"},{\"datum\":\"2036-03-28T03:00:00+01:00\",\"prijsZP\":\"0.285621\"},{\"datum\":\"2036-03-28T04:00:00+01:00\",\"prijsZP\":\"0.283216\"},{\"datum\":\"2036-03-28T05:00:00+01:00\",\"prijsZP\":\"0.302259\"},{\"datum\":\"2036-03-28T06:00:00+01:00\",\"prijsZP\":\"0.343756\"},{\"datum\":\"2036-03-28T07:00:00+01:00\",\"prijsZP\":\"0.358935\"},{\"datum\":\"2036-03-28T08:00:00+01:00\",\"prijsZP\":\"0.319084\"},{\"datum\":\"2036-03-28T09:00:00+01:00\",\"prijsZP\":\"0.282330\"},{\"datum\":\"2036-03-28T10:00:00+01:00\",\"prijsZP\":\"0.241498\"},{\"datum\":\"2036-03-28T11:00:00+01:00\",\"prijsZP\":\"0.214437\"},{\"datum\":\"2036-03-28T12:00:00+01:00\",\"prijsZP\":\"0.201045\"},{\"datum\":\"2036-03-28T13:00:00+01:00\",\"prijsZP\":\"0.199832\"},{\"datum\":\"2036-03-28T14:00:00+01:00\",\"prijsZP\":\"0.222970\"},{\"datum\":\"2036-03-28T15:00:00+01:00\",\"prijsZP\":\"0.254990\"},{\"datum\":\"2036-03-28T16:00:00+01:00\",\"prijsZP\":\"0.284901\"},{\"datum\":\"2036-03-28T17:00:00+01:00\",\"prijsZP\":\"0.317662\"},{\"datum\":\"2036-03-28T18:00:00+01:00\",\"prijsZP\":\"0.367130\"},{\"datum\":\"2036-03-28T19:00:00+01:00\",\"prijsZP\":\"0.372278\"},{\"datum\":\"2036-03-28T20:00:00+01:00\",\"prijsZP\":\"0.331825\"},{\"datum\":\"2036-03-28T21:00:00+01:00\",\"prijsZP\":\"0.312181\"},{\"datum\":\"2036-03-28T22:00:00+01:00\",\"prijsZP\":\"0.301378\"},{\"datum\":\"2036-03-28T23:00:00+01:00\",\"prijsZP\":\"0.296575\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 4);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        // var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // var prices2 = pr2.getElectricityPrices().stream()
        // .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        // prices.addPrices(prices2);
        prices.processPrices();

        logger.info("Strategy: " + prices.controlStrategy);
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }

    // @Test
    public void testProcessPricesSolar() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        // var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // var prices2 = pr2.getElectricityPrices().stream()
        // .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        // prices.addPrices(prices2);

        logger.info("mode: " + prices.controlStrategy);
        logger.info("Prices: " + prices.getAllPrices().toString());
    }

    // @Test
    public void testSetMode() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        // var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // var prices2 = pr2.getElectricityPrices().stream()
        // .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        // prices.addPrices(prices2);

        logger.info("mode: " + prices.controlStrategy);
        prices.controlStrategy = "prices";
        logger.info("set mode: " + prices.controlStrategy);
        logger.info("mode: " + prices.controlStrategy);
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }
}
