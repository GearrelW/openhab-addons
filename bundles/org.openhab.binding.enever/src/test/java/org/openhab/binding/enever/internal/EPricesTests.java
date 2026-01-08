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

    private String testDataE1 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-01-10T00:00:00+01:00\",\"prijsZP\":\"0.231891\"},{\"datum\":\"2026-01-10T01:00:00+01:00\",\"prijsZP\":\"0.230530\"},{\"datum\":\"2026-01-10T02:00:00+01:00\",\"prijsZP\":\"0.233122\"},{\"datum\":\"2026-01-10T03:00:00+01:00\",\"prijsZP\":\"0.230642\"},{\"datum\":\"2026-01-10T04:00:00+01:00\",\"prijsZP\":\"0.233770\"},{\"datum\":\"2026-01-10T05:00:00+01:00\",\"prijsZP\":\"0.236311\"},{\"datum\":\"2026-01-10T06:00:00+01:00\",\"prijsZP\":\"0.247845\"},{\"datum\":\"2026-01-10T07:00:00+01:00\",\"prijsZP\":\"0.281725\"},{\"datum\":\"2026-01-10T08:00:00+01:00\",\"prijsZP\":\"0.333906\"},{\"datum\":\"2026-01-10T09:00:00+01:00\",\"prijsZP\":\"0.363052\"},{\"datum\":\"2026-01-10T10:00:00+01:00\",\"prijsZP\":\"0.379687\"},{\"datum\":\"2026-01-10T11:00:00+01:00\",\"prijsZP\":\"0.382288\"},{\"datum\":\"2026-01-10T12:00:00+01:00\",\"prijsZP\":\"0.390316\"},{\"datum\":\"2026-01-10T13:00:00+01:00\",\"prijsZP\":\"0.403100\"},{\"datum\":\"2026-01-10T14:00:00+01:00\",\"prijsZP\":\"0.432149\"},{\"datum\":\"2026-01-10T15:00:00+01:00\",\"prijsZP\":\"0.430150\"},{\"datum\":\"2026-01-10T16:00:00+01:00\",\"prijsZP\":\"0.431245\"},{\"datum\":\"2026-01-10T17:00:00+01:00\",\"prijsZP\":\"0.403100\"},{\"datum\":\"2026-01-10T18:00:00+01:00\",\"prijsZP\":\"0.365729\"},{\"datum\":\"2026-01-10T19:00:00+01:00\",\"prijsZP\":\"0.295065\"},{\"datum\":\"2026-01-10T20:00:00+01:00\",\"prijsZP\":\"0.250815\"},{\"datum\":\"2026-01-10T21:00:00+01:00\",\"prijsZP\":\"0.241320\"},{\"datum\":\"2026-01-10T22:00:00+01:00\",\"prijsZP\":\"0.237775\"},{\"datum\":\"2026-01-10T23:00:00+01:00\",\"prijsZP\":\"0.225551\"}],\"code\":\"5\"}";
    private String testDataE2 = "{\"status\":\"true\",\"data\":[{\"datum\":\"2026-01-09T00:00:00+01:00\",\"prijsZP\":\"0.242718\"},{\"datum\":\"2026-01-09T01:00:00+01:00\",\"prijsZP\":\"0.238355\"},{\"datum\":\"2026-01-09T02:00:00+01:00\",\"prijsZP\":\"0.235524\"},{\"datum\":\"2026-01-09T03:00:00+01:00\",\"prijsZP\":\"0.235288\"},{\"datum\":\"2026-01-09T04:00:00+01:00\",\"prijsZP\":\"0.236719\"},{\"datum\":\"2026-01-09T05:00:00+01:00\",\"prijsZP\":\"0.240788\"},{\"datum\":\"2026-01-09T06:00:00+01:00\",\"prijsZP\":\"0.262767\"},{\"datum\":\"2026-01-09T07:00:00+01:00\",\"prijsZP\":\"0.287318\"},{\"datum\":\"2026-01-09T08:00:00+01:00\",\"prijsZP\":\"0.313772\"},{\"datum\":\"2026-01-09T09:00:00+01:00\",\"prijsZP\":\"0.314952\"},{\"datum\":\"2026-01-09T10:00:00+01:00\",\"prijsZP\":\"0.305453\"},{\"datum\":\"2026-01-09T11:00:00+01:00\",\"prijsZP\":\"0.294436\"},{\"datum\":\"2026-01-09T12:00:00+01:00\",\"prijsZP\":\"0.291229\"},{\"datum\":\"2026-01-09T13:00:00+01:00\",\"prijsZP\":\"0.290159\"},{\"datum\":\"2026-01-09T14:00:00+01:00\",\"prijsZP\":\"0.296142\"},{\"datum\":\"2026-01-09T15:00:00+01:00\",\"prijsZP\":\"0.308741\"},{\"datum\":\"2026-01-09T16:00:00+01:00\",\"prijsZP\":\"0.343810\"},{\"datum\":\"2026-01-09T17:00:00+01:00\",\"prijsZP\":\"0.398378\"},{\"datum\":\"2026-01-09T18:00:00+01:00\",\"prijsZP\":\"0.389288\"},{\"datum\":\"2026-01-09T19:00:00+01:00\",\"prijsZP\":\"0.352038\"},{\"datum\":\"2026-01-09T20:00:00+01:00\",\"prijsZP\":\"0.308759\"},{\"datum\":\"2026-01-09T21:00:00+01:00\",\"prijsZP\":\"0.287064\"},{\"datum\":\"2026-01-09T22:00:00+01:00\",\"prijsZP\":\"0.280709\"},{\"datum\":\"2026-01-09T23:00:00+01:00\",\"prijsZP\":\"0.265523\"}],\"code\":\"5\"}";

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
