package org.openhab.binding.enever.internal;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openhab.binding.enever.internal.payloads.EneVerPayload;
import org.openhab.binding.enever.internal.payloads.PayloadPriceItem;
import org.openhab.binding.enever.internal.payloads.ZonneplanPayloadPriceItem;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class EPricesTests {

    private Logger logger = Logger.getLogger(EPricesTests.class.getName());
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-19T00:00:00+01:00\",\"prijsZP\":\"0.251052\"},{\"datum\":\"2025-12-19T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-19T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-19T03:00:00+01:00\",\"prijsZP\":\"0.145955\"},{\"datum\":\"2025-12-19T04:00:00+01:00\",\"prijsZP\":\"0.137875\"},{\"datum\":\"2025-12-19T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-19T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-19T07:00:00+01:00\",\"prijsZP\":\"0.175960\"},{\"datum\":\"2025-12-19T08:00:00+01:00\",\"prijsZP\":\"0.296155\"},{\"datum\":\"2025-12-19T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-19T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-19T11:00:00+01:00\",\"prijsZP\":\"0.254183\"},{\"datum\":\"2025-12-19T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-19T13:00:00+01:00\",\"prijsZP\":\"0.252643\"},{\"datum\":\"2025-12-19T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-19T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-19T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-19T17:00:00+01:00\",\"prijsZP\":\"0.598418\"},{\"datum\":\"2025-12-19T18:00:00+01:00\",\"prijsZP\":\"0.270161\"},{\"datum\":\"2025-12-19T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-19T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-19T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-19T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-19T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-19T00:00:00+01:00\",\"prijsZP\":\"0.151052\"},{\"datum\":\"2025-12-19T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-19T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-19T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-19T04:00:00+01:00\",\"prijsZP\":\"0.237875\"},{\"datum\":\"2025-12-19T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-19T06:00:00+01:00\",\"prijsZP\":\"0.274820\"},{\"datum\":\"2025-12-19T07:00:00+01:00\",\"prijsZP\":\"0.275960\"},{\"datum\":\"2025-12-19T08:00:00+01:00\",\"prijsZP\":\"0.196155\"},{\"datum\":\"2025-12-19T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-19T10:00:00+01:00\",\"prijsZP\":\"0.154749\"},{\"datum\":\"2025-12-19T11:00:00+01:00\",\"prijsZP\":\"0.294183\"},{\"datum\":\"2025-12-19T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-19T13:00:00+01:00\",\"prijsZP\":\"0.452643\"},{\"datum\":\"2025-12-19T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-19T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-19T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-19T17:00:00+01:00\",\"prijsZP\":\"0.198418\"},{\"datum\":\"2025-12-19T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-19T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-19T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-19T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-19T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-19T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(EPrices.PRICES_CONTROL, 0.50, 0.15, 3);
        var pr1 = gson.fromJson(testDataE1, EneVerPayload.class);
        var pr2 = gson.fromJson(testDataE2, EneVerPayload.class);

        var prices1 = pr1.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        var prices2 = pr2.getElectricityPrices().stream()
                .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
        prices.addPrices(prices1);
        prices.addPrices(prices2);
        prices.processPrices();

        logger.info("Prices: " + prices.getAllPrices().toString());
    }

    @Test
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

    @Test
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

    @Test
    public void testGson() {
        var js = "{\r\n" + //
                "  \"pageProps\": {\r\n" + //
                "    \"data\": {\r\n" + //
                "      \"templateName\": \"block-builder\",\r\n" + //
                "      \"templateProps\": {\r\n" + //
                "        \"pageLayout\": \"default\",\r\n" + //
                "        \"title\": \"Dynamisch energiecontract\",\r\n" + //
                "        \"featuredImage\": null,\r\n" + //
                "        \"modified\": \"2025-11-24T13:48:59\",\r\n" + //
                "        \"noIndex\": null,\r\n" + //
                "        \"energyData\": {\r\n" + //
                "          \"__typename\": \"EnergyData\",\r\n" + //
                "          \"electricity\": {\r\n" + //
                "            \"__typename\": \"Electricity\",\r\n" + //
                "            \"hours\": [\r\n" + //
                "              {\r\n" + //
                "                \"__typename\": \"ElectricityHour\",\r\n" + //
                "                \"dateTime\": \"2025-12-19T22:00:00.000000Z\",\r\n" + //
                "                \"priceTotalTaxIncluded\": 2355524,\r\n" + //
                "                \"marketPrice\": 766025,\r\n" + //
                "                \"priceInclHandlingVat\": 1126890,\r\n" + //
                "                \"priceEnergyTaxes\": 1228634,\r\n" + //
                "                \"priceCbsAverage\": 0.4,\r\n" + //
                "                \"pricingProfile\": \"low\"\r\n" + //
                "              },\r\n" + //
                "              {\r\n" + //
                "                \"__typename\": \"ElectricityHour\",\r\n" + //
                "                \"dateTime\": \"2025-12-19T21:00:00.000000Z\",\r\n" + //
                "                \"priceTotalTaxIncluded\": 2405618,\r\n" + //
                "                \"marketPrice\": 807425,\r\n" + //
                "                \"priceInclHandlingVat\": 1176984,\r\n" + //
                "                \"priceEnergyTaxes\": 1228634,\r\n" + //
                "                \"priceCbsAverage\": 0.4,\r\n" + //
                "                \"pricingProfile\": \"low\"\r\n" + //
                "              }\r\n" + //
                "            ]\r\n" + //
                "          }\r\n" + //
                "        }\r\n" + //
                "      }\r\n" + //
                "    }\r\n" + //
                "  }\r\n" + //
                "}";
        var ob = JsonParser.parseString(js).getAsJsonObject();

        var hours = JsonParser.parseString(js).getAsJsonObject().get("pageProps").getAsJsonObject().get("data")
                .getAsJsonObject().get("templateProps").getAsJsonObject().get("energyData").getAsJsonObject()
                .get("electricity").getAsJsonObject().get("hours");

        // for (var hour : hours) {
        var price = gson.fromJson(hours.toString(), ZonneplanPayloadPriceItem[].class);
        for (var p : price) {
            logger.info("Price: " + p.toString());
        }

        // var datum = gson.fromJson(hour, PayloadPriceItem.class).getDatum();
        // logger.info("Datum: " + datum);
        // }
    }
}
