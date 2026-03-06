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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-03-06T00:00:00+01:00\",\"prijsZP\":\"0.254500\"},{\"datum\":\"2026-03-06T01:00:00+01:00\",\"prijsZP\":\"0.259582\"},{\"datum\":\"2026-03-06T02:00:00+01:00\",\"prijsZP\":\"0.262159\"},{\"datum\":\"2026-03-06T03:00:00+01:00\",\"prijsZP\":\"0.263381\"},{\"datum\":\"2026-03-06T04:00:00+01:00\",\"prijsZP\":\"0.270605\"},{\"datum\":\"2026-03-06T05:00:00+01:00\",\"prijsZP\":\"0.276437\"},{\"datum\":\"2026-03-06T06:00:00+01:00\",\"prijsZP\":\"0.309700\"},{\"datum\":\"2026-03-06T07:00:00+01:00\",\"prijsZP\":\"0.343084\"},{\"datum\":\"2026-03-06T08:00:00+01:00\",\"prijsZP\":\"0.296535\"},{\"datum\":\"2026-03-06T09:00:00+01:00\",\"prijsZP\":\"0.266842\"},{\"datum\":\"2026-03-06T10:00:00+01:00\",\"prijsZP\":\"0.247615\"},{\"datum\":\"2026-03-06T11:00:00+01:00\",\"prijsZP\":\"0.217280\"},{\"datum\":\"2026-03-06T12:00:00+01:00\",\"prijsZP\":\"0.199772\"},{\"datum\":\"2026-03-06T13:00:00+01:00\",\"prijsZP\":\"0.192657\"},{\"datum\":\"2026-03-06T14:00:00+01:00\",\"prijsZP\":\"0.221225\"},{\"datum\":\"2026-03-06T15:00:00+01:00\",\"prijsZP\":\"0.255855\"},{\"datum\":\"2026-03-06T16:00:00+01:00\",\"prijsZP\":\"0.282015\"},{\"datum\":\"2026-03-06T17:00:00+01:00\",\"prijsZP\":\"0.344415\"},{\"datum\":\"2026-03-06T18:00:00+01:00\",\"prijsZP\":\"0.402471\"},{\"datum\":\"2026-03-06T19:00:00+01:00\",\"prijsZP\":\"0.356672\"},{\"datum\":\"2026-03-06T20:00:00+01:00\",\"prijsZP\":\"0.308478\"},{\"datum\":\"2026-03-06T21:00:00+01:00\",\"prijsZP\":\"0.288695\"},{\"datum\":\"2026-03-06T22:00:00+01:00\",\"prijsZP\":\"0.278918\"},{\"datum\":\"2026-03-06T23:00:00+01:00\",\"prijsZP\":\"0.276050\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-03-07T00:00:00+01:00\",\"prijsZP\":\"0.286565\"},{\"datum\":\"2026-03-07T01:00:00+01:00\",\"prijsZP\":\"0.280031\"},{\"datum\":\"2026-03-07T02:00:00+01:00\",\"prijsZP\":\"0.273678\"},{\"datum\":\"2026-03-07T03:00:00+01:00\",\"prijsZP\":\"0.270702\"},{\"datum\":\"2026-03-07T04:00:00+01:00\",\"prijsZP\":\"0.270641\"},{\"datum\":\"2026-03-07T05:00:00+01:00\",\"prijsZP\":\"0.276098\"},{\"datum\":\"2026-03-07T06:00:00+01:00\",\"prijsZP\":\"0.283407\"},{\"datum\":\"2026-03-07T07:00:00+01:00\",\"prijsZP\":\"0.280152\"},{\"datum\":\"2026-03-07T08:00:00+01:00\",\"prijsZP\":\"0.266527\"},{\"datum\":\"2026-03-07T09:00:00+01:00\",\"prijsZP\":\"0.256799\"},{\"datum\":\"2026-03-07T10:00:00+01:00\",\"prijsZP\":\"0.223609\"},{\"datum\":\"2026-03-07T11:00:00+01:00\",\"prijsZP\":\"0.170659\"},{\"datum\":\"2026-03-07T12:00:00+01:00\",\"prijsZP\":\"0.148504\"},{\"datum\":\"2026-03-07T13:00:00+01:00\",\"prijsZP\":\"0.132762\"},{\"datum\":\"2026-03-07T14:00:00+01:00\",\"prijsZP\":\"0.153235\"},{\"datum\":\"2026-03-07T15:00:00+01:00\",\"prijsZP\":\"0.223887\"},{\"datum\":\"2026-03-07T16:00:00+01:00\",\"prijsZP\":\"0.271379\"},{\"datum\":\"2026-03-07T17:00:00+01:00\",\"prijsZP\":\"0.298036\"},{\"datum\":\"2026-03-07T18:00:00+01:00\",\"prijsZP\":\"0.347682\"},{\"datum\":\"2026-03-07T19:00:00+01:00\",\"prijsZP\":\"0.334372\"},{\"datum\":\"2026-03-07T20:00:00+01:00\",\"prijsZP\":\"0.307728\"},{\"datum\":\"2026-03-07T21:00:00+01:00\",\"prijsZP\":\"0.289820\"},{\"datum\":\"2026-03-07T22:00:00+01:00\",\"prijsZP\":\"0.287351\"},{\"datum\":\"2026-03-07T23:00:00+01:00\",\"prijsZP\":\"0.282995\"}],\"code\":\"5\"}";

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
