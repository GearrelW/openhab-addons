package org.openhab.binding.enever.internal;

import java.time.LocalDateTime;
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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-05-31T00:00:00+02:00\",\"prijsZP\":\"0.288434\"},{\"datum\":\"2026-05-31T01:00:00+02:00\",\"prijsZP\":\"0.284408\"},{\"datum\":\"2026-05-31T02:00:00+02:00\",\"prijsZP\":\"0.280376\"},{\"datum\":\"2026-05-31T03:00:00+02:00\",\"prijsZP\":\"0.279042\"},{\"datum\":\"2026-05-31T04:00:00+02:00\",\"prijsZP\":\"0.284069\"},{\"datum\":\"2026-05-31T05:00:00+02:00\",\"prijsZP\":\"0.291744\"},{\"datum\":\"2026-05-31T06:00:00+02:00\",\"prijsZP\":\"0.307295\"},{\"datum\":\"2026-05-31T07:00:00+02:00\",\"prijsZP\":\"0.296215\"},{\"datum\":\"2026-05-31T08:00:00+02:00\",\"prijsZP\":\"0.278410\"},{\"datum\":\"2026-05-31T09:00:00+02:00\",\"prijsZP\":\"0.235025\"},{\"datum\":\"2026-05-31T10:00:00+02:00\",\"prijsZP\":\"0.154620\"},{\"datum\":\"2026-05-31T11:00:00+02:00\",\"prijsZP\":\"0.131537\"},{\"datum\":\"2026-05-31T12:00:00+02:00\",\"prijsZP\":\"0.130838\"},{\"datum\":\"2026-05-31T13:00:00+02:00\",\"prijsZP\":\"0.130814\"},{\"datum\":\"2026-05-31T14:00:00+02:00\",\"prijsZP\":\"0.130838\"},{\"datum\":\"2026-05-31T15:00:00+02:00\",\"prijsZP\":\"0.132420\"},{\"datum\":\"2026-05-31T16:00:00+02:00\",\"prijsZP\":\"0.176146\"},{\"datum\":\"2026-05-31T17:00:00+02:00\",\"prijsZP\":\"0.247143\"},{\"datum\":\"2026-05-31T18:00:00+02:00\",\"prijsZP\":\"0.295540\"},{\"datum\":\"2026-05-31T19:00:00+02:00\",\"prijsZP\":\"0.389000\"},{\"datum\":\"2026-05-31T20:00:00+02:00\",\"prijsZP\":\"0.584969\"},{\"datum\":\"2026-05-31T21:00:00+02:00\",\"prijsZP\":\"0.562629\"},{\"datum\":\"2026-05-31T22:00:00+02:00\",\"prijsZP\":\"0.390613\"},{\"datum\":\"2026-05-31T23:00:00+02:00\",\"prijsZP\":\"0.321555\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-06-01T00:00:00+02:00\",\"prijsZP\":\"0.304098\"},{\"datum\":\"2026-06-01T01:00:00+02:00\",\"prijsZP\":\"0.291850\"},{\"datum\":\"2026-06-01T02:00:00+02:00\",\"prijsZP\":\"0.284653\"},{\"datum\":\"2026-06-01T03:00:00+02:00\",\"prijsZP\":\"0.281855\"},{\"datum\":\"2026-06-01T04:00:00+02:00\",\"prijsZP\":\"0.280415\"},{\"datum\":\"2026-06-01T05:00:00+02:00\",\"prijsZP\":\"0.081613\"},{\"datum\":\"2026-06-01T06:00:00+02:00\",\"prijsZP\":\"0.289644\"},{\"datum\":\"2026-06-01T07:00:00+02:00\",\"prijsZP\":\"0.283304\"},{\"datum\":\"2026-06-01T08:00:00+02:00\",\"prijsZP\":\"0.270021\"},{\"datum\":\"2026-06-01T09:00:00+02:00\",\"prijsZP\":\"0.227880\"},{\"datum\":\"2026-06-01T10:00:00+02:00\",\"prijsZP\":\"0.155050\"},{\"datum\":\"2026-06-01T11:00:00+02:00\",\"prijsZP\":\"0.131806\"},{\"datum\":\"2026-06-01T12:00:00+02:00\",\"prijsZP\":\"0.130844\"},{\"datum\":\"2026-06-01T13:00:00+02:00\",\"prijsZP\":\"0.130898\"},{\"datum\":\"2026-06-01T14:00:00+02:00\",\"prijsZP\":\"0.131168\"},{\"datum\":\"2026-06-01T15:00:00+02:00\",\"prijsZP\":\"0.148540\"},{\"datum\":\"2026-06-01T16:00:00+02:00\",\"prijsZP\":\"0.210668\"},{\"datum\":\"2026-06-01T17:00:00+02:00\",\"prijsZP\":\"0.255961\"},{\"datum\":\"2026-06-01T18:00:00+02:00\",\"prijsZP\":\"0.292654\"},{\"datum\":\"2026-06-01T19:00:00+02:00\",\"prijsZP\":\"0.337984\"},{\"datum\":\"2026-06-01T20:00:00+02:00\",\"prijsZP\":\"0.403750\"},{\"datum\":\"2026-06-01T21:00:00+02:00\",\"prijsZP\":\"0.369126\"},{\"datum\":\"2026-06-01T22:00:00+02:00\",\"prijsZP\":\"0.325300\"},{\"datum\":\"2026-06-01T23:00:00+02:00\",\"prijsZP\":\"0.306058\"}],\"code\":\"5\"}";

    // private String testDataE2 =
    // "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-05-26T00:00:00+01:00\",\"prijsZP\":\"0.292442\"},{\"datum\":\"2026-05-26T01:00:00+01:00\",\"prijsZP\":\"0.286526\"},{\"datum\":\"2026-05-26T02:00:00+01:00\",\"prijsZP\":\"0.252466\"},{\"datum\":\"2026-05-26T03:00:00+01:00\",\"prijsZP\":\"0.285621\"},{\"datum\":\"2026-05-26T04:00:00+01:00\",\"prijsZP\":\"0.283216\"},{\"datum\":\"2026-05-26T05:00:00+01:00\",\"prijsZP\":\"0.302259\"},{\"datum\":\"2026-05-26T06:00:00+01:00\",\"prijsZP\":\"0.343756\"},{\"datum\":\"2026-05-26T07:00:00+01:00\",\"prijsZP\":\"0.358935\"},{\"datum\":\"2026-05-26T08:00:00+01:00\",\"prijsZP\":\"0.319084\"},{\"datum\":\"2026-05-26T09:00:00+01:00\",\"prijsZP\":\"0.282330\"},{\"datum\":\"2026-05-26T10:00:00+01:00\",\"prijsZP\":\"0.241498\"},{\"datum\":\"2026-05-26T11:00:00+01:00\",\"prijsZP\":\"0.214437\"},{\"datum\":\"2026-05-26T12:00:00+01:00\",\"prijsZP\":\"0.201045\"},{\"datum\":\"2026-05-26T13:00:00+01:00\",\"prijsZP\":\"0.199832\"},{\"datum\":\"2026-05-26T14:00:00+01:00\",\"prijsZP\":\"0.222970\"},{\"datum\":\"2026-05-26T15:00:00+01:00\",\"prijsZP\":\"0.254990\"},{\"datum\":\"2026-05-26T16:00:00+01:00\",\"prijsZP\":\"0.284901\"},{\"datum\":\"2026-05-26T17:00:00+01:00\",\"prijsZP\":\"0.317662\"},{\"datum\":\"2026-05-26T18:00:00+01:00\",\"prijsZP\":\"0.367130\"},{\"datum\":\"2026-05-26T19:00:00+01:00\",\"prijsZP\":\"0.372278\"},{\"datum\":\"2026-05-26T20:00:00+01:00\",\"prijsZP\":\"0.331825\"},{\"datum\":\"2026-05-26T21:00:00+01:00\",\"prijsZP\":\"0.312181\"},{\"datum\":\"2026-05-26T22:00:00+01:00\",\"prijsZP\":\"0.301378\"},{\"datum\":\"2026-05-26T23:00:00+01:00\",\"prijsZP\":\"0.296575\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(0.40, 0.15, 4);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // prices.addPrices(prices1);

        prices.addPrices(prices2);
        // prices.processPrices();
        var now = LocalDateTime.now().withMonth(5).withDayOfMonth(31);

        var p = prices.getPriceFor(now);

        // logger.info("Strategy: " + prices.getControlStrategy());
        // logger.info("Peak hour: " + prices.getMaxPriceFor(now.toLocalDate()));
        logger.info("Laden start -12: " + prices.getPlan().getChargeStart(now.minusHours(12)));
        logger.info("Laden stopt -12: " + prices.getPlan().getChargeEnd(now.minusHours(12)));
        logger.info("Ontladen start -12: " + prices.getPlan().getDischargeStart(now.minusHours(12)));
        logger.info("Ontladen stopt -12: " + prices.getPlan().getDischargeEnd(now.minusHours(12)));

        logger.info("Laden start -5: " + prices.getPlan().getChargeStart(now.minusHours(5)));
        logger.info("Laden stopt -5: " + prices.getPlan().getChargeEnd(now.minusHours(5)));
        logger.info("Ontladen start -5: " + prices.getPlan().getDischargeStart(now.minusHours(5)));
        logger.info("Ontladen stopt -5: " + prices.getPlan().getDischargeEnd(now.minusHours(5)));
        if (p != null) {
            logger.info("NU: " + p.toString());
        }
    }

    // @Test
    public void testGetPrice() {
        var prices = new EPrices(0.40, 0.15, 4);
        var pr1 = gson.fromJson(testDataE2, EneVerPayload.class);
        // var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // var prices2 = pr2.getElectricityPrices().stream()
        // .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        // prices.addPrices(prices2);
        // prices.processPrices();
        // logger.info("plus 2: " + prices.getPriceFor(LocalDateTime.now().plusHours(2)).toString());
        // logger.info("now: " + prices.getPriceFor(LocalDateTime.now()).toString());

        logger.info("Strategy: " + prices.getControlStrategy());
        var p = prices.getPriceFor(LocalDateTime.now());
        logger.info("NU: " + p.toString());
        var p2 = prices.getPriceFor(LocalDateTime.now().plusHours(2));
        logger.info("plus 2: " + p2.toString());
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }

    // @Test
    public void testProcessPricesSolar() {
        var prices = new EPrices(0.40, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        // var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        // var prices2 = pr2.getElectricityPrices().stream()
        // .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        // prices.addPrices(prices2);

        // logger.info("mode: " + prices.controlStrategy);
        // logger.info("Prices: " + prices.getAllPrices().toString());
    }
}
