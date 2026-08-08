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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2036-08-10T00:00:00+02:00\",\"prijsZP\":\"0.320034\"},{\"datum\":\"2036-08-10T00:15:00+02:00\",\"prijsZP\":\"0.313354\"},{\"datum\":\"2036-08-10T00:30:00+02:00\",\"prijsZP\":\"0.315157\"},{\"datum\":\"2036-08-10T00:45:00+02:00\",\"prijsZP\":\"0.303723\"},{\"datum\":\"2036-08-10T01:00:00+02:00\",\"prijsZP\":\"0.304969\"},{\"datum\":\"2036-08-10T01:15:00+02:00\",\"prijsZP\":\"0.299536\"},{\"datum\":\"2036-08-10T01:30:00+02:00\",\"prijsZP\":\"0.300347\"},{\"datum\":\"2036-08-10T01:45:00+02:00\",\"prijsZP\":\"0.296729\"},{\"datum\":\"2036-08-10T02:00:00+02:00\",\"prijsZP\":\"0.299282\"},{\"datum\":\"2036-08-10T02:15:00+02:00\",\"prijsZP\":\"0.289614\"},{\"datum\":\"2036-08-10T02:30:00+02:00\",\"prijsZP\":\"0.285391\"},{\"datum\":\"2036-08-10T02:45:00+02:00\",\"prijsZP\":\"0.284447\"},{\"datum\":\"2036-08-10T03:00:00+02:00\",\"prijsZP\":\"0.286323\"},{\"datum\":\"2036-08-10T03:15:00+02:00\",\"prijsZP\":\"0.285415\"},{\"datum\":\"2036-08-10T03:30:00+02:00\",\"prijsZP\":\"0.286940\"},{\"datum\":\"2036-08-10T03:45:00+02:00\",\"prijsZP\":\"0.288102\"},{\"datum\":\"2036-08-10T04:00:00+02:00\",\"prijsZP\":\"0.285065\"},{\"datum\":\"2036-08-10T04:15:00+02:00\",\"prijsZP\":\"0.286831\"},{\"datum\":\"2036-08-10T04:30:00+02:00\",\"prijsZP\":\"0.291877\"},{\"datum\":\"2036-08-10T04:45:00+02:00\",\"prijsZP\":\"0.295906\"},{\"datum\":\"2036-08-10T05:00:00+02:00\",\"prijsZP\":\"0.285863\"},{\"datum\":\"2036-08-10T05:15:00+02:00\",\"prijsZP\":\"0.296197\"},{\"datum\":\"2036-08-10T05:30:00+02:00\",\"prijsZP\":\"0.311406\"},{\"datum\":\"2036-08-10T05:45:00+02:00\",\"prijsZP\":\"0.313729\"},{\"datum\":\"2036-08-10T06:00:00+02:00\",\"prijsZP\":\"0.314528\"},{\"datum\":\"2036-08-10T06:15:00+02:00\",\"prijsZP\":\"0.320215\"},{\"datum\":\"2036-08-10T06:30:00+02:00\",\"prijsZP\":\"0.324377\"},{\"datum\":\"2036-08-10T06:45:00+02:00\",\"prijsZP\":\"0.329084\"},{\"datum\":\"2036-08-10T07:00:00+02:00\",\"prijsZP\":\"0.335425\"},{\"datum\":\"2036-08-10T07:15:00+02:00\",\"prijsZP\":\"0.329314\"},{\"datum\":\"2036-08-10T07:30:00+02:00\",\"prijsZP\":\"0.316851\"},{\"datum\":\"2036-08-10T07:45:00+02:00\",\"prijsZP\":\"0.294563\"},{\"datum\":\"2036-08-10T08:00:00+02:00\",\"prijsZP\":\"0.333283\"},{\"datum\":\"2036-08-10T08:15:00+02:00\",\"prijsZP\":\"0.313984\"},{\"datum\":\"2036-08-10T08:30:00+02:00\",\"prijsZP\":\"0.305126\"},{\"datum\":\"2036-08-10T08:45:00+02:00\",\"prijsZP\":\"0.277611\"},{\"datum\":\"2036-08-10T09:00:00+02:00\",\"prijsZP\":\"0.312858\"},{\"datum\":\"2036-08-10T09:15:00+02:00\",\"prijsZP\":\"0.288295\"},{\"datum\":\"2036-08-10T09:30:00+02:00\",\"prijsZP\":\"0.268403\"},{\"datum\":\"2036-08-10T09:45:00+02:00\",\"prijsZP\":\"0.261796\"},{\"datum\":\"2036-08-10T10:00:00+02:00\",\"prijsZP\":\"0.261203\"},{\"datum\":\"2036-08-10T10:15:00+02:00\",\"prijsZP\":\"0.251911\"},{\"datum\":\"2036-08-10T10:30:00+02:00\",\"prijsZP\":\"0.245086\"},{\"datum\":\"2036-08-10T10:45:00+02:00\",\"prijsZP\":\"0.239750\"},{\"datum\":\"2036-08-10T11:00:00+02:00\",\"prijsZP\":\"0.228388\"},{\"datum\":\"2036-08-10T11:15:00+02:00\",\"prijsZP\":\"0.216796\"},{\"datum\":\"2036-08-10T11:30:00+02:00\",\"prijsZP\":\"0.200703\"},{\"datum\":\"2036-08-10T11:45:00+02:00\",\"prijsZP\":\"0.180884\"},{\"datum\":\"2036-08-10T12:00:00+02:00\",\"prijsZP\":\"0.184380\"},{\"datum\":\"2036-08-10T12:15:00+02:00\",\"prijsZP\":\"0.170974\"},{\"datum\":\"2036-08-10T12:30:00+02:00\",\"prijsZP\":\"0.167041\"},{\"datum\":\"2036-08-10T12:45:00+02:00\",\"prijsZP\":\"0.160495\"},{\"datum\":\"2036-08-10T13:00:00+02:00\",\"prijsZP\":\"0.168070\"},{\"datum\":\"2036-08-10T13:15:00+02:00\",\"prijsZP\":\"0.155776\"},{\"datum\":\"2036-08-10T13:30:00+02:00\",\"prijsZP\":\"0.152860\"},{\"datum\":\"2036-08-10T13:45:00+02:00\",\"prijsZP\":\"0.148347\"},{\"datum\":\"2036-08-10T14:00:00+02:00\",\"prijsZP\":\"0.149726\"},{\"datum\":\"2036-08-10T14:15:00+02:00\",\"prijsZP\":\"0.157180\"},{\"datum\":\"2036-08-10T14:30:00+02:00\",\"prijsZP\":\"0.162867\"},{\"datum\":\"2036-08-10T14:45:00+02:00\",\"prijsZP\":\"0.177169\"},{\"datum\":\"2036-08-10T15:00:00+02:00\",\"prijsZP\":\"0.165988\"},{\"datum\":\"2036-08-10T15:15:00+02:00\",\"prijsZP\":\"0.180266\"},{\"datum\":\"2036-08-10T15:30:00+02:00\",\"prijsZP\":\"0.195512\"},{\"datum\":\"2036-08-10T15:45:00+02:00\",\"prijsZP\":\"0.214050\"},{\"datum\":\"2036-08-10T16:00:00+02:00\",\"prijsZP\":\"0.204781\"},{\"datum\":\"2036-08-10T16:15:00+02:00\",\"prijsZP\":\"0.224371\"},{\"datum\":\"2036-08-10T16:30:00+02:00\",\"prijsZP\":\"0.235321\"},{\"datum\":\"2036-08-10T16:45:00+02:00\",\"prijsZP\":\"0.251620\"},{\"datum\":\"2036-08-10T17:00:00+02:00\",\"prijsZP\":\"0.252673\"},{\"datum\":\"2036-08-10T17:15:00+02:00\",\"prijsZP\":\"0.261397\"},{\"datum\":\"2036-08-10T17:30:00+02:00\",\"prijsZP\":\"0.276631\"},{\"datum\":\"2036-08-10T17:45:00+02:00\",\"prijsZP\":\"0.288719\"},{\"datum\":\"2036-08-10T18:00:00+02:00\",\"prijsZP\":\"0.277635\"},{\"datum\":\"2036-08-10T18:15:00+02:00\",\"prijsZP\":\"0.297878\"},{\"datum\":\"2036-08-10T18:30:00+02:00\",\"prijsZP\":\"0.307087\"},{\"datum\":\"2036-08-10T18:45:00+02:00\",\"prijsZP\":\"0.326979\"},{\"datum\":\"2036-08-10T19:00:00+02:00\",\"prijsZP\":\"0.307292\"},{\"datum\":\"2036-08-10T19:15:00+02:00\",\"prijsZP\":\"0.332968\"},{\"datum\":\"2036-08-10T19:30:00+02:00\",\"prijsZP\":\"0.339974\"},{\"datum\":\"2036-08-10T19:45:00+02:00\",\"prijsZP\":\"0.360048\"},{\"datum\":\"2036-08-10T20:00:00+02:00\",\"prijsZP\":\"0.347537\"},{\"datum\":\"2036-08-10T20:15:00+02:00\",\"prijsZP\":\"0.362807\"},{\"datum\":\"2036-08-10T20:30:00+02:00\",\"prijsZP\":\"0.372487\"},{\"datum\":\"2036-08-10T20:45:00+02:00\",\"prijsZP\":\"0.369026\"},{\"datum\":\"2036-08-10T21:00:00+02:00\",\"prijsZP\":\"0.380909\"},{\"datum\":\"2036-08-10T21:15:00+02:00\",\"prijsZP\":\"0.365433\"},{\"datum\":\"2036-08-10T21:30:00+02:00\",\"prijsZP\":\"0.356745\"},{\"datum\":\"2036-08-10T21:45:00+02:00\",\"prijsZP\":\"0.343229\"},{\"datum\":\"2036-08-10T22:00:00+02:00\",\"prijsZP\":\"0.356503\"},{\"datum\":\"2036-08-10T22:15:00+02:00\",\"prijsZP\":\"0.342697\"},{\"datum\":\"2036-08-10T22:30:00+02:00\",\"prijsZP\":\"0.342661\"},{\"datum\":\"2036-08-10T22:45:00+02:00\",\"prijsZP\":\"0.337264\"},{\"datum\":\"2036-08-10T23:00:00+02:00\",\"prijsZP\":\"0.338994\"},{\"datum\":\"2036-08-10T23:15:00+02:00\",\"prijsZP\":\"0.330875\"},{\"datum\":\"2036-08-10T23:30:00+02:00\",\"prijsZP\":\"0.329024\"},{\"datum\":\"2036-08-10T23:45:00+02:00\",\"prijsZP\":\"0.322514\"}],\"code\":\"5\"}";
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

    @Test
    public void testGetPrice() {
        var prices = new EPrices(0.40, 0.15, 16);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
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

        logger.info("prijzen: " + prices.getAllPrices().toString());
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
