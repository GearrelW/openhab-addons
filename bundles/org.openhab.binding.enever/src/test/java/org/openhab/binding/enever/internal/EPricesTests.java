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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-01-15T00:00:00+01:00\",\"prijsZP\":\"0.234381\"},{\"datum\":\"2026-01-15T01:00:00+01:00\",\"prijsZP\":\"0.232847\"},{\"datum\":\"2026-01-15T02:00:00+01:00\",\"prijsZP\":\"0.234768\"},{\"datum\":\"2026-01-15T03:00:00+01:00\",\"prijsZP\":\"0.235116\"},{\"datum\":\"2026-01-15T04:00:00+01:00\",\"prijsZP\":\"0.236900\"},{\"datum\":\"2026-01-15T05:00:00+01:00\",\"prijsZP\":\"0.239333\"},{\"datum\":\"2026-01-15T06:00:00+01:00\",\"prijsZP\":\"0.243640\"},{\"datum\":\"2026-01-15T07:00:00+01:00\",\"prijsZP\":\"0.257477\"},{\"datum\":\"2026-01-15T08:00:00+01:00\",\"prijsZP\":\"0.285573\"},{\"datum\":\"2026-01-15T09:00:00+01:00\",\"prijsZP\":\"0.288504\"},{\"datum\":\"2026-01-15T10:00:00+01:00\",\"prijsZP\":\"0.269855\"},{\"datum\":\"2026-01-15T11:00:00+01:00\",\"prijsZP\":\"0.255879\"},{\"datum\":\"2026-01-15T12:00:00+01:00\",\"prijsZP\":\"0.249427\"},{\"datum\":\"2026-01-15T13:00:00+01:00\",\"prijsZP\":\"0.252192\"},{\"datum\":\"2026-01-15T14:00:00+01:00\",\"prijsZP\":\"0.269050\"},{\"datum\":\"2026-01-15T15:00:00+01:00\",\"prijsZP\":\"0.298229\"},{\"datum\":\"2026-01-15T16:00:00+01:00\",\"prijsZP\":\"0.309752\"},{\"datum\":\"2026-01-15T17:00:00+01:00\",\"prijsZP\":\"0.364628\"},{\"datum\":\"2026-01-15T18:00:00+01:00\",\"prijsZP\":\"0.349809\"},{\"datum\":\"2026-01-15T19:00:00+01:00\",\"prijsZP\":\"0.322115\"},{\"datum\":\"2026-01-15T20:00:00+01:00\",\"prijsZP\":\"0.287654\"},{\"datum\":\"2026-01-15T21:00:00+01:00\",\"prijsZP\":\"0.251608\"},{\"datum\":\"2026-01-15T22:00:00+01:00\",\"prijsZP\":\"0.242663\"},{\"datum\":\"2026-01-15T23:00:00+01:00\",\"prijsZP\":\"0.242170\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-01-16T00:00:00+01:00\",\"prijsZP\":\"0.234381\"},{\"datum\":\"2026-01-16T01:00:00+01:00\",\"prijsZP\":\"0.232847\"},{\"datum\":\"2026-01-16T02:00:00+01:00\",\"prijsZP\":\"0.234768\"},{\"datum\":\"2026-01-16T03:00:00+01:00\",\"prijsZP\":\"0.235116\"},{\"datum\":\"2026-01-16T04:00:00+01:00\",\"prijsZP\":\"0.236900\"},{\"datum\":\"2026-01-16T05:00:00+01:00\",\"prijsZP\":\"0.239333\"},{\"datum\":\"2026-01-16T06:00:00+01:00\",\"prijsZP\":\"0.243640\"},{\"datum\":\"2026-01-16T07:00:00+01:00\",\"prijsZP\":\"0.257477\"},{\"datum\":\"2026-01-16T08:00:00+01:00\",\"prijsZP\":\"0.285573\"},{\"datum\":\"2026-01-16T09:00:00+01:00\",\"prijsZP\":\"0.288504\"},{\"datum\":\"2026-01-16T10:00:00+01:00\",\"prijsZP\":\"0.269855\"},{\"datum\":\"2026-01-16T11:00:00+01:00\",\"prijsZP\":\"0.255879\"},{\"datum\":\"2026-01-16T12:00:00+01:00\",\"prijsZP\":\"0.249427\"},{\"datum\":\"2026-01-16T13:00:00+01:00\",\"prijsZP\":\"0.252192\"},{\"datum\":\"2026-01-16T14:00:00+01:00\",\"prijsZP\":\"0.269050\"},{\"datum\":\"2026-01-16T15:00:00+01:00\",\"prijsZP\":\"0.298229\"},{\"datum\":\"2026-01-16T16:00:00+01:00\",\"prijsZP\":\"0.309752\"},{\"datum\":\"2026-01-16T17:00:00+01:00\",\"prijsZP\":\"0.364628\"},{\"datum\":\"2026-01-16T18:00:00+01:00\",\"prijsZP\":\"0.349809\"},{\"datum\":\"2026-01-16T19:00:00+01:00\",\"prijsZP\":\"0.322115\"},{\"datum\":\"2026-01-16T20:00:00+01:00\",\"prijsZP\":\"0.287654\"},{\"datum\":\"2026-01-16T21:00:00+01:00\",\"prijsZP\":\"0.251608\"},{\"datum\":\"2026-01-16T22:00:00+01:00\",\"prijsZP\":\"0.242663\"},{\"datum\":\"2026-01-16T23:00:00+01:00\",\"prijsZP\":\"0.242170\"}],\"code\":\"5\"}";

    @Test
    public void testProcessPricesPrices() {
        var prices = new EPrices(EPrices.SOLAR_CONTROL, 0.40, 0.15, 3);
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
