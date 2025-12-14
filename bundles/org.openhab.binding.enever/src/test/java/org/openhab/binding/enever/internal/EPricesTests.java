package org.openhab.binding.enever.internal;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EPricesTests {

    private Logger logger = Logger.getLogger(EPricesTests.class.getName());
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-14T00:00:00+01:00\",\"prijsZP\":\"0.251052\"},{\"datum\":\"2025-12-14T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-14T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-14T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-14T04:00:00+01:00\",\"prijsZP\":\"0.137875\"},{\"datum\":\"2025-12-14T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-14T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-14T07:00:00+01:00\",\"prijsZP\":\"0.175960\"},{\"datum\":\"2025-12-14T08:00:00+01:00\",\"prijsZP\":\"0.296155\"},{\"datum\":\"2025-12-14T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-14T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-14T11:00:00+01:00\",\"prijsZP\":\"0.254183\"},{\"datum\":\"2025-12-14T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-14T13:00:00+01:00\",\"prijsZP\":\"0.252643\"},{\"datum\":\"2025-12-14T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-14T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-14T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-14T17:00:00+01:00\",\"prijsZP\":\"0.598418\"},{\"datum\":\"2025-12-14T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-14T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-14T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-14T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-14T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-14T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-15T00:00:00+01:00\",\"prijsZP\":\"0.151052\"},{\"datum\":\"2025-12-15T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-15T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-15T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-15T04:00:00+01:00\",\"prijsZP\":\"0.237875\"},{\"datum\":\"2025-12-15T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-15T06:00:00+01:00\",\"prijsZP\":\"0.274820\"},{\"datum\":\"2025-12-15T07:00:00+01:00\",\"prijsZP\":\"0.275960\"},{\"datum\":\"2025-12-15T08:00:00+01:00\",\"prijsZP\":\"0.196155\"},{\"datum\":\"2025-12-15T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-15T10:00:00+01:00\",\"prijsZP\":\"0.154749\"},{\"datum\":\"2025-12-15T11:00:00+01:00\",\"prijsZP\":\"0.294183\"},{\"datum\":\"2025-12-15T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-15T13:00:00+01:00\",\"prijsZP\":\"0.452643\"},{\"datum\":\"2025-12-15T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-15T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-15T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-15T17:00:00+01:00\",\"prijsZP\":\"0.198418\"},{\"datum\":\"2025-12-15T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-15T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-15T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-15T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-15T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-15T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(EPrices.PRICES_MODE, 0.50, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, Payload.class);
        var pr2 = gson.fromJson(testDataE2, Payload.class);

        var prices1 = pr1.getPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);

        logger.info("Prices: " + prices.getAllPrices().toString());
    }

    @Test
    public void testProcessPricesSolar() {
        var prices = new EPrices(EPrices.SOLAR_MODE, 0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, Payload.class);
        var pr2 = gson.fromJson(testDataE2, Payload.class);

        var prices1 = pr1.getPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);

        logger.info("mode: " + prices.statusMode);
        logger.info("Prices: " + prices.getAllPrices().toString());
    }
}
