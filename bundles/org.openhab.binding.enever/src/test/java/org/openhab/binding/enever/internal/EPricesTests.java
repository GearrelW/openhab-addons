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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-12T00:00:00+01:00\",\"prijsZP\":\"0.251052\"},{\"datum\":\"2025-12-12T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-12T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-12T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-12T04:00:00+01:00\",\"prijsZP\":\"0.137875\"},{\"datum\":\"2025-12-12T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-12T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-12T07:00:00+01:00\",\"prijsZP\":\"0.175960\"},{\"datum\":\"2025-12-12T08:00:00+01:00\",\"prijsZP\":\"0.296155\"},{\"datum\":\"2025-12-12T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-12T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-12T11:00:00+01:00\",\"prijsZP\":\"0.254183\"},{\"datum\":\"2025-12-12T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-12T13:00:00+01:00\",\"prijsZP\":\"0.252643\"},{\"datum\":\"2025-12-12T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-12T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-12T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-12T17:00:00+01:00\",\"prijsZP\":\"0.598418\"},{\"datum\":\"2025-12-12T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-12T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-12T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-12T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-12T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-12T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-13T00:00:00+01:00\",\"prijsZP\":\"0.151052\"},{\"datum\":\"2025-12-13T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-13T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-13T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-13T04:00:00+01:00\",\"prijsZP\":\"0.137875\"},{\"datum\":\"2025-12-13T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-13T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-13T07:00:00+01:00\",\"prijsZP\":\"0.175960\"},{\"datum\":\"2025-12-13T08:00:00+01:00\",\"prijsZP\":\"0.396155\"},{\"datum\":\"2025-12-13T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-13T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-13T11:00:00+01:00\",\"prijsZP\":\"0.294183\"},{\"datum\":\"2025-12-13T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-13T13:00:00+01:00\",\"prijsZP\":\"0.452643\"},{\"datum\":\"2025-12-13T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-13T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-13T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-13T17:00:00+01:00\",\"prijsZP\":\"0.198418\"},{\"datum\":\"2025-12-13T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-13T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-13T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-13T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-13T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-13T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    


    @Test
    public void testProcessPrices() {
        var prices = new EPrices(0.15, 3);
        var pr1 = gson.fromJson(testDataE1, Payload.class);
        var pr2 = gson.fromJson(testDataE2, Payload.class);

        var prices1 = pr1.getPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getPrices().stream()
                    .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);
        
        prices.getAllPrices().forEach((price) -> {
            //logger.info(price.toString());
        });
    }
}