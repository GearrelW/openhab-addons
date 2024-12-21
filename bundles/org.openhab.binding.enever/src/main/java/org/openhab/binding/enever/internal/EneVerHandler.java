/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.enever.internal;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EneVerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class EneVerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable EneVerConfiguration config;

    private @Nullable ScheduledFuture<?> dailyJob;
    private @Nullable ScheduledFuture<?> hourlyJob;

    private String testDataE = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-10-21 00:00:00\",\"prijs\":\"-0.000100\",\"prijsAA\":\"0.152729\",\"prijsAIP\":\"0.161779\",\"prijsANWB\":\"0.179929\",\"prijsBE\":\"0.152519\",\"prijsEE\":\"0.158379\",\"prijsEN\":\"0.154919\",\"prijsEVO\":\"0.179929\",\"prijsEZ\":\"0.181529\",\"prijsFR\":\"0.149729\",\"prijsGSL\":\"0.179929\",\"prijsMDE\":\"0.179929\",\"prijsNE\":\"0.151429\",\"prijsTI\":\"0.153309\",\"prijsVDB\":\"0.153409\",\"prijsVON\":\"0.151529\",\"prijsWE\":\"0.156929\",\"prijsZG\":\"0.179929\",\"prijsZP\":\"0.151529\"},{\"datum\":\"2024-10-21 01:00:00\",\"prijs\":\"-0.000140\",\"prijsAA\":\"0.152681\",\"prijsAIP\":\"0.161731\",\"prijsANWB\":\"0.179881\",\"prijsBE\":\"0.152471\",\"prijsEE\":\"0.158331\",\"prijsEN\":\"0.154871\",\"prijsEVO\":\"0.179881\",\"prijsEZ\":\"0.181481\",\"prijsFR\":\"0.149681\",\"prijsGSL\":\"0.179881\",\"prijsMDE\":\"0.179881\",\"prijsNE\":\"0.151381\",\"prijsTI\":\"0.153261\",\"prijsVDB\":\"0.153361\",\"prijsVON\":\"0.151481\",\"prijsWE\":\"0.156881\",\"prijsZG\":\"0.179881\",\"prijsZP\":\"0.151481\"},{\"datum\":\"2024-10-21 02:00:00\",\"prijs\":\"-0.000080\",\"prijsAA\":\"0.152753\",\"prijsAIP\":\"0.161803\",\"prijsANWB\":\"0.179953\",\"prijsBE\":\"0.152543\",\"prijsEE\":\"0.158403\",\"prijsEN\":\"0.154943\",\"prijsEVO\":\"0.179953\",\"prijsEZ\":\"0.181553\",\"prijsFR\":\"0.149753\",\"prijsGSL\":\"0.179953\",\"prijsMDE\":\"0.179953\",\"prijsNE\":\"0.151453\",\"prijsTI\":\"0.153333\",\"prijsVDB\":\"0.153433\",\"prijsVON\":\"0.151553\",\"prijsWE\":\"0.156953\",\"prijsZG\":\"0.179953\",\"prijsZP\":\"0.151553\"},{\"datum\":\"2024-10-21 03:00:00\",\"prijs\":\"0.000000\",\"prijsAA\":\"0.152850\",\"prijsAIP\":\"0.161900\",\"prijsANWB\":\"0.180050\",\"prijsBE\":\"0.152640\",\"prijsEE\":\"0.158500\",\"prijsEN\":\"0.155040\",\"prijsEVO\":\"0.180050\",\"prijsEZ\":\"0.181650\",\"prijsFR\":\"0.149850\",\"prijsGSL\":\"0.180050\",\"prijsMDE\":\"0.180050\",\"prijsNE\":\"0.151550\",\"prijsTI\":\"0.153430\",\"prijsVDB\":\"0.153530\",\"prijsVON\":\"0.151650\",\"prijsWE\":\"0.157050\",\"prijsZG\":\"0.180050\",\"prijsZP\":\"0.151650\"},{\"datum\":\"2024-10-21 04:00:00\",\"prijs\":\"0.003450\",\"prijsAA\":\"0.157025\",\"prijsAIP\":\"0.166075\",\"prijsANWB\":\"0.184225\",\"prijsBE\":\"0.156815\",\"prijsEE\":\"0.162674\",\"prijsEN\":\"0.159215\",\"prijsEVO\":\"0.184225\",\"prijsEZ\":\"0.185824\",\"prijsFR\":\"0.154024\",\"prijsGSL\":\"0.184225\",\"prijsMDE\":\"0.184225\",\"prijsNE\":\"0.155725\",\"prijsTI\":\"0.157605\",\"prijsVDB\":\"0.157704\",\"prijsVON\":\"0.155825\",\"prijsWE\":\"0.161225\",\"prijsZG\":\"0.184225\",\"prijsZP\":\"0.155825\"},{\"datum\":\"2024-10-21 05:00:00\",\"prijs\":\"0.061960\",\"prijsAA\":\"0.227822\",\"prijsAIP\":\"0.236872\",\"prijsANWB\":\"0.255022\",\"prijsBE\":\"0.227612\",\"prijsEE\":\"0.233472\",\"prijsEN\":\"0.230012\",\"prijsEVO\":\"0.255022\",\"prijsEZ\":\"0.256622\",\"prijsFR\":\"0.224822\",\"prijsGSL\":\"0.255022\",\"prijsMDE\":\"0.255022\",\"prijsNE\":\"0.226522\",\"prijsTI\":\"0.228402\",\"prijsVDB\":\"0.228502\",\"prijsVON\":\"0.226622\",\"prijsWE\":\"0.232022\",\"prijsZG\":\"0.255022\",\"prijsZP\":\"0.226622\"},{\"datum\":\"2024-10-21 06:00:00\",\"prijs\":\"0.104130\",\"prijsAA\":\"0.278847\",\"prijsAIP\":\"0.287897\",\"prijsANWB\":\"0.306047\",\"prijsBE\":\"0.278637\",\"prijsEE\":\"0.284497\",\"prijsEN\":\"0.281037\",\"prijsEVO\":\"0.306047\",\"prijsEZ\":\"0.307647\",\"prijsFR\":\"0.275847\",\"prijsGSL\":\"0.306047\",\"prijsMDE\":\"0.306047\",\"prijsNE\":\"0.277547\",\"prijsTI\":\"0.279427\",\"prijsVDB\":\"0.279527\",\"prijsVON\":\"0.277647\",\"prijsWE\":\"0.283047\",\"prijsZG\":\"0.306047\",\"prijsZP\":\"0.277647\"},{\"datum\":\"2024-10-21 07:00:00\",\"prijs\":\"0.120350\",\"prijsAA\":\"0.298474\",\"prijsAIP\":\"0.307524\",\"prijsANWB\":\"0.325674\",\"prijsBE\":\"0.298264\",\"prijsEE\":\"0.304124\",\"prijsEN\":\"0.300664\",\"prijsEVO\":\"0.325674\",\"prijsEZ\":\"0.327274\",\"prijsFR\":\"0.295474\",\"prijsGSL\":\"0.325674\",\"prijsMDE\":\"0.325674\",\"prijsNE\":\"0.297174\",\"prijsTI\":\"0.299054\",\"prijsVDB\":\"0.299154\",\"prijsVON\":\"0.297274\",\"prijsWE\":\"0.302674\",\"prijsZG\":\"0.325674\",\"prijsZP\":\"0.297274\"},{\"datum\":\"2024-10-21 08:00:00\",\"prijs\":\"0.103900\",\"prijsAA\":\"0.278569\",\"prijsAIP\":\"0.287619\",\"prijsANWB\":\"0.305769\",\"prijsBE\":\"0.278359\",\"prijsEE\":\"0.284219\",\"prijsEN\":\"0.280759\",\"prijsEVO\":\"0.305769\",\"prijsEZ\":\"0.307369\",\"prijsFR\":\"0.275569\",\"prijsGSL\":\"0.305769\",\"prijsMDE\":\"0.305769\",\"prijsNE\":\"0.277269\",\"prijsTI\":\"0.279149\",\"prijsVDB\":\"0.279249\",\"prijsVON\":\"0.277369\",\"prijsWE\":\"0.282769\",\"prijsZG\":\"0.305769\",\"prijsZP\":\"0.277369\"},{\"datum\":\"2024-10-21 09:00:00\",\"prijs\":\"0.091000\",\"prijsAA\":\"0.262960\",\"prijsAIP\":\"0.272010\",\"prijsANWB\":\"0.290160\",\"prijsBE\":\"0.262750\",\"prijsEE\":\"0.268610\",\"prijsEN\":\"0.265150\",\"prijsEVO\":\"0.290160\",\"prijsEZ\":\"0.291760\",\"prijsFR\":\"0.259960\",\"prijsGSL\":\"0.290160\",\"prijsMDE\":\"0.290160\",\"prijsNE\":\"0.261660\",\"prijsTI\":\"0.263540\",\"prijsVDB\":\"0.263640\",\"prijsVON\":\"0.261760\",\"prijsWE\":\"0.267160\",\"prijsZG\":\"0.290160\",\"prijsZP\":\"0.261760\"},{\"datum\":\"2024-10-21 10:00:00\",\"prijs\":\"0.084910\",\"prijsAA\":\"0.255591\",\"prijsAIP\":\"0.264641\",\"prijsANWB\":\"0.282791\",\"prijsBE\":\"0.255381\",\"prijsEE\":\"0.261241\",\"prijsEN\":\"0.257781\",\"prijsEVO\":\"0.282791\",\"prijsEZ\":\"0.284391\",\"prijsFR\":\"0.252591\",\"prijsGSL\":\"0.282791\",\"prijsMDE\":\"0.282791\",\"prijsNE\":\"0.254291\",\"prijsTI\":\"0.256171\",\"prijsVDB\":\"0.256271\",\"prijsVON\":\"0.254391\",\"prijsWE\":\"0.259791\",\"prijsZG\":\"0.282791\",\"prijsZP\":\"0.254391\"},{\"datum\":\"2024-10-21 11:00:00\",\"prijs\":\"0.080000\",\"prijsAA\":\"0.249650\",\"prijsAIP\":\"0.258700\",\"prijsANWB\":\"0.276850\",\"prijsBE\":\"0.249440\",\"prijsEE\":\"0.255300\",\"prijsEN\":\"0.251840\",\"prijsEVO\":\"0.276850\",\"prijsEZ\":\"0.278450\",\"prijsFR\":\"0.246650\",\"prijsGSL\":\"0.276850\",\"prijsMDE\":\"0.276850\",\"prijsNE\":\"0.248350\",\"prijsTI\":\"0.250230\",\"prijsVDB\":\"0.250330\",\"prijsVON\":\"0.248450\",\"prijsWE\":\"0.253850\",\"prijsZG\":\"0.276850\",\"prijsZP\":\"0.248450\"},{\"datum\":\"2024-10-21 12:00:00\",\"prijs\":\"0.083150\",\"prijsAA\":\"0.253462\",\"prijsAIP\":\"0.262512\",\"prijsANWB\":\"0.280662\",\"prijsBE\":\"0.253252\",\"prijsEE\":\"0.259112\",\"prijsEN\":\"0.255652\",\"prijsEVO\":\"0.280662\",\"prijsEZ\":\"0.282262\",\"prijsFR\":\"0.250462\",\"prijsGSL\":\"0.280662\",\"prijsMDE\":\"0.280662\",\"prijsNE\":\"0.252162\",\"prijsTI\":\"0.254042\",\"prijsVDB\":\"0.254141\",\"prijsVON\":\"0.252262\",\"prijsWE\":\"0.257662\",\"prijsZG\":\"0.280662\",\"prijsZP\":\"0.252262\"},{\"datum\":\"2024-10-21 13:00:00\",\"prijs\":\"0.083190\",\"prijsAA\":\"0.253510\",\"prijsAIP\":\"0.262560\",\"prijsANWB\":\"0.280710\",\"prijsBE\":\"0.253300\",\"prijsEE\":\"0.259160\",\"prijsEN\":\"0.255700\",\"prijsEVO\":\"0.280710\",\"prijsEZ\":\"0.282310\",\"prijsFR\":\"0.250510\",\"prijsGSL\":\"0.280710\",\"prijsMDE\":\"0.280710\",\"prijsNE\":\"0.252210\",\"prijsTI\":\"0.254090\",\"prijsVDB\":\"0.254190\",\"prijsVON\":\"0.252310\",\"prijsWE\":\"0.257710\",\"prijsZG\":\"0.280710\",\"prijsZP\":\"0.252310\"},{\"datum\":\"2024-10-21 14:00:00\",\"prijs\":\"0.095820\",\"prijsAA\":\"0.268792\",\"prijsAIP\":\"0.277842\",\"prijsANWB\":\"0.295992\",\"prijsBE\":\"0.268582\",\"prijsEE\":\"0.274442\",\"prijsEN\":\"0.270982\",\"prijsEVO\":\"0.295992\",\"prijsEZ\":\"0.297592\",\"prijsFR\":\"0.265792\",\"prijsGSL\":\"0.295992\",\"prijsMDE\":\"0.295992\",\"prijsNE\":\"0.267492\",\"prijsTI\":\"0.269372\",\"prijsVDB\":\"0.269472\",\"prijsVON\":\"0.267592\",\"prijsWE\":\"0.272992\",\"prijsZG\":\"0.295992\",\"prijsZP\":\"0.267592\"},{\"datum\":\"2024-10-21 15:00:00\",\"prijs\":\"0.116570\",\"prijsAA\":\"0.293900\",\"prijsAIP\":\"0.302950\",\"prijsANWB\":\"0.321100\",\"prijsBE\":\"0.293690\",\"prijsEE\":\"0.299550\",\"prijsEN\":\"0.296090\",\"prijsEVO\":\"0.321100\",\"prijsEZ\":\"0.322700\",\"prijsFR\":\"0.290900\",\"prijsGSL\":\"0.321100\",\"prijsMDE\":\"0.321100\",\"prijsNE\":\"0.292600\",\"prijsTI\":\"0.294480\",\"prijsVDB\":\"0.294580\",\"prijsVON\":\"0.292700\",\"prijsWE\":\"0.298100\",\"prijsZG\":\"0.321100\",\"prijsZP\":\"0.292700\"},{\"datum\":\"2024-10-21 16:00:00\",\"prijs\":\"0.130040\",\"prijsAA\":\"0.310198\",\"prijsAIP\":\"0.319248\",\"prijsANWB\":\"0.337398\",\"prijsBE\":\"0.309988\",\"prijsEE\":\"0.315848\",\"prijsEN\":\"0.312388\",\"prijsEVO\":\"0.337398\",\"prijsEZ\":\"0.338998\",\"prijsFR\":\"0.307198\",\"prijsGSL\":\"0.337398\",\"prijsMDE\":\"0.337398\",\"prijsNE\":\"0.308898\",\"prijsTI\":\"0.310778\",\"prijsVDB\":\"0.310878\",\"prijsVON\":\"0.308998\",\"prijsWE\":\"0.314398\",\"prijsZG\":\"0.337398\",\"prijsZP\":\"0.308998\"},{\"datum\":\"2024-10-21 17:00:00\",\"prijs\":\"0.212150\",\"prijsAA\":\"0.409552\",\"prijsAIP\":\"0.418602\",\"prijsANWB\":\"0.436752\",\"prijsBE\":\"0.409342\",\"prijsEE\":\"0.415202\",\"prijsEN\":\"0.411742\",\"prijsEVO\":\"0.436752\",\"prijsEZ\":\"0.438352\",\"prijsFR\":\"0.406552\",\"prijsGSL\":\"0.436752\",\"prijsMDE\":\"0.436752\",\"prijsNE\":\"0.408252\",\"prijsTI\":\"0.410132\",\"prijsVDB\":\"0.410232\",\"prijsVON\":\"0.408352\",\"prijsWE\":\"0.413752\",\"prijsZG\":\"0.436752\",\"prijsZP\":\"0.408352\"},{\"datum\":\"2024-10-21 18:00:00\",\"prijs\":\"0.188350\",\"prijsAA\":\"0.380754\",\"prijsAIP\":\"0.389804\",\"prijsANWB\":\"0.407954\",\"prijsBE\":\"0.380544\",\"prijsEE\":\"0.386404\",\"prijsEN\":\"0.382944\",\"prijsEVO\":\"0.407954\",\"prijsEZ\":\"0.409554\",\"prijsFR\":\"0.377754\",\"prijsGSL\":\"0.407954\",\"prijsMDE\":\"0.407954\",\"prijsNE\":\"0.379454\",\"prijsTI\":\"0.381334\",\"prijsVDB\":\"0.381434\",\"prijsVON\":\"0.379554\",\"prijsWE\":\"0.384954\",\"prijsZG\":\"0.407954\",\"prijsZP\":\"0.379554\"},{\"datum\":\"2024-10-21 19:00:00\",\"prijs\":\"0.128180\",\"prijsAA\":\"0.307948\",\"prijsAIP\":\"0.316998\",\"prijsANWB\":\"0.335148\",\"prijsBE\":\"0.307738\",\"prijsEE\":\"0.313598\",\"prijsEN\":\"0.310138\",\"prijsEVO\":\"0.335148\",\"prijsEZ\":\"0.336748\",\"prijsFR\":\"0.304948\",\"prijsGSL\":\"0.335148\",\"prijsMDE\":\"0.335148\",\"prijsNE\":\"0.306648\",\"prijsTI\":\"0.308528\",\"prijsVDB\":\"0.308628\",\"prijsVON\":\"0.306748\",\"prijsWE\":\"0.312148\",\"prijsZG\":\"0.335148\",\"prijsZP\":\"0.306748\"},{\"datum\":\"2024-10-21 20:00:00\",\"prijs\":\"0.100560\",\"prijsAA\":\"0.274528\",\"prijsAIP\":\"0.283578\",\"prijsANWB\":\"0.301728\",\"prijsBE\":\"0.274318\",\"prijsEE\":\"0.280178\",\"prijsEN\":\"0.276718\",\"prijsEVO\":\"0.301728\",\"prijsEZ\":\"0.303328\",\"prijsFR\":\"0.271528\",\"prijsGSL\":\"0.301728\",\"prijsMDE\":\"0.301728\",\"prijsNE\":\"0.273228\",\"prijsTI\":\"0.275108\",\"prijsVDB\":\"0.275208\",\"prijsVON\":\"0.273328\",\"prijsWE\":\"0.278728\",\"prijsZG\":\"0.301728\",\"prijsZP\":\"0.273328\"},{\"datum\":\"2024-10-21 21:00:00\",\"prijs\":\"0.097790\",\"prijsAA\":\"0.271176\",\"prijsAIP\":\"0.280226\",\"prijsANWB\":\"0.298376\",\"prijsBE\":\"0.270966\",\"prijsEE\":\"0.276826\",\"prijsEN\":\"0.273366\",\"prijsEVO\":\"0.298376\",\"prijsEZ\":\"0.299976\",\"prijsFR\":\"0.268176\",\"prijsGSL\":\"0.298376\",\"prijsMDE\":\"0.298376\",\"prijsNE\":\"0.269876\",\"prijsTI\":\"0.271756\",\"prijsVDB\":\"0.271856\",\"prijsVON\":\"0.269976\",\"prijsWE\":\"0.275376\",\"prijsZG\":\"0.298376\",\"prijsZP\":\"0.269976\"},{\"datum\":\"2024-10-21 22:00:00\",\"prijs\":\"0.092970\",\"prijsAA\":\"0.265344\",\"prijsAIP\":\"0.274394\",\"prijsANWB\":\"0.292544\",\"prijsBE\":\"0.265134\",\"prijsEE\":\"0.270994\",\"prijsEN\":\"0.267534\",\"prijsEVO\":\"0.292544\",\"prijsEZ\":\"0.294144\",\"prijsFR\":\"0.262344\",\"prijsGSL\":\"0.292544\",\"prijsMDE\":\"0.292544\",\"prijsNE\":\"0.264044\",\"prijsTI\":\"0.265924\",\"prijsVDB\":\"0.266024\",\"prijsVON\":\"0.264144\",\"prijsWE\":\"0.269544\",\"prijsZG\":\"0.292544\",\"prijsZP\":\"0.264144\"}],\"code\":\"5\"}";
    // private String testDataE = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-09-23
    // 00:00:00\",\"prijs\":\"0.087300\",\"prijsAA\":\"0.258483\",\"prijsAIP\":\"0.267533\",\"prijsANWB\":\"0.285683\",\"prijsBE\":\"0.258273\",\"prijsEE\":\"0.264133\",\"prijsEN\":\"0.257873\",\"prijsEVO\":\"0.285683\",\"prijsEZ\":\"0.287283\",\"prijsFR\":\"0.262683\",\"prijsGSL\":\"0.285683\",\"prijsMDE\":\"0.285683\",\"prijsNE\":\"0.257183\",\"prijsTI\":\"0.259063\",\"prijsVDB\":\"0.259163\",\"prijsVON\":\"0.257283\",\"prijsWE\":\"0.262683\",\"prijsZG\":\"0.285683\",\"prijsZP\":\"0.257283\"},{\"datum\":\"2024-09-23
    // 01:00:00\",\"prijs\":\"0.080150\",\"prijsAA\":\"0.249832\",\"prijsAIP\":\"0.258882\",\"prijsANWB\":\"0.277032\",\"prijsBE\":\"0.249622\",\"prijsEE\":\"0.255482\",\"prijsEN\":\"0.249222\",\"prijsEVO\":\"0.277032\",\"prijsEZ\":\"0.278632\",\"prijsFR\":\"0.254032\",\"prijsGSL\":\"0.277032\",\"prijsMDE\":\"0.277032\",\"prijsNE\":\"0.248532\",\"prijsTI\":\"0.250412\",\"prijsVDB\":\"0.250511\",\"prijsVON\":\"0.248632\",\"prijsWE\":\"0.254032\",\"prijsZG\":\"0.277032\",\"prijsZP\":\"0.148632\"},{\"datum\":\"2024-09-23
    // 02:00:00\",\"prijs\":\"0.081600\",\"prijsAA\":\"0.251586\",\"prijsAIP\":\"0.260636\",\"prijsANWB\":\"0.278786\",\"prijsBE\":\"0.251376\",\"prijsEE\":\"0.257236\",\"prijsEN\":\"0.250976\",\"prijsEVO\":\"0.278786\",\"prijsEZ\":\"0.280386\",\"prijsFR\":\"0.255786\",\"prijsGSL\":\"0.278786\",\"prijsMDE\":\"0.278786\",\"prijsNE\":\"0.250286\",\"prijsTI\":\"0.252166\",\"prijsVDB\":\"0.252266\",\"prijsVON\":\"0.250386\",\"prijsWE\":\"0.255786\",\"prijsZG\":\"0.278786\",\"prijsZP\":\"0.250386\"},{\"datum\":\"2024-09-23
    // 03:00:00\",\"prijs\":\"0.082770\",\"prijsAA\":\"0.253002\",\"prijsAIP\":\"0.262052\",\"prijsANWB\":\"0.280202\",\"prijsBE\":\"0.252792\",\"prijsEE\":\"0.258652\",\"prijsEN\":\"0.252392\",\"prijsEVO\":\"0.280202\",\"prijsEZ\":\"0.281802\",\"prijsFR\":\"0.257202\",\"prijsGSL\":\"0.280202\",\"prijsMDE\":\"0.280202\",\"prijsNE\":\"0.251702\",\"prijsTI\":\"0.253582\",\"prijsVDB\":\"0.253682\",\"prijsVON\":\"0.251802\",\"prijsWE\":\"0.257202\",\"prijsZG\":\"0.280202\",\"prijsZP\":\"0.251802\"},{\"datum\":\"2024-09-23
    // 04:00:00\",\"prijs\":\"0.084030\",\"prijsAA\":\"0.254526\",\"prijsAIP\":\"0.263576\",\"prijsANWB\":\"0.281726\",\"prijsBE\":\"0.254316\",\"prijsEE\":\"0.260176\",\"prijsEN\":\"0.253916\",\"prijsEVO\":\"0.281726\",\"prijsEZ\":\"0.283326\",\"prijsFR\":\"0.258726\",\"prijsGSL\":\"0.281726\",\"prijsMDE\":\"0.281726\",\"prijsNE\":\"0.253226\",\"prijsTI\":\"0.255106\",\"prijsVDB\":\"0.255206\",\"prijsVON\":\"0.253326\",\"prijsWE\":\"0.258726\",\"prijsZG\":\"0.281726\",\"prijsZP\":\"0.253326\"},{\"datum\":\"2024-09-23
    // 05:00:00\",\"prijs\":\"0.090900\",\"prijsAA\":\"0.262839\",\"prijsAIP\":\"0.271889\",\"prijsANWB\":\"0.290039\",\"prijsBE\":\"0.262629\",\"prijsEE\":\"0.268489\",\"prijsEN\":\"0.262229\",\"prijsEVO\":\"0.290039\",\"prijsEZ\":\"0.291639\",\"prijsFR\":\"0.267039\",\"prijsGSL\":\"0.290039\",\"prijsMDE\":\"0.290039\",\"prijsNE\":\"0.261539\",\"prijsTI\":\"0.263419\",\"prijsVDB\":\"0.263519\",\"prijsVON\":\"0.261639\",\"prijsWE\":\"0.267039\",\"prijsZG\":\"0.290039\",\"prijsZP\":\"0.261639\"},{\"datum\":\"2024-09-23
    // 06:00:00\",\"prijs\":\"0.130690\",\"prijsAA\":\"0.310985\",\"prijsAIP\":\"0.320035\",\"prijsANWB\":\"0.338185\",\"prijsBE\":\"0.310775\",\"prijsEE\":\"0.316635\",\"prijsEN\":\"0.310375\",\"prijsEVO\":\"0.338185\",\"prijsEZ\":\"0.339785\",\"prijsFR\":\"0.315185\",\"prijsGSL\":\"0.338185\",\"prijsMDE\":\"0.338185\",\"prijsNE\":\"0.309685\",\"prijsTI\":\"0.311565\",\"prijsVDB\":\"0.311665\",\"prijsVON\":\"0.309785\",\"prijsWE\":\"0.315185\",\"prijsZG\":\"0.338185\",\"prijsZP\":\"0.309785\"},{\"datum\":\"2024-09-23
    // 07:00:00\",\"prijs\":\"0.203730\",\"prijsAA\":\"0.399363\",\"prijsAIP\":\"0.408413\",\"prijsANWB\":\"0.426563\",\"prijsBE\":\"0.399153\",\"prijsEE\":\"0.405013\",\"prijsEN\":\"0.398753\",\"prijsEVO\":\"0.426563\",\"prijsEZ\":\"0.428163\",\"prijsFR\":\"0.403563\",\"prijsGSL\":\"0.426563\",\"prijsMDE\":\"0.426563\",\"prijsNE\":\"0.398063\",\"prijsTI\":\"0.399943\",\"prijsVDB\":\"0.400043\",\"prijsVON\":\"0.398163\",\"prijsWE\":\"0.403563\",\"prijsZG\":\"0.426563\",\"prijsZP\":\"0.398163\"},{\"datum\":\"2024-09-23
    // 08:00:00\",\"prijs\":\"0.151330\",\"prijsAA\":\"0.335959\",\"prijsAIP\":\"0.345009\",\"prijsANWB\":\"0.363159\",\"prijsBE\":\"0.335749\",\"prijsEE\":\"0.341609\",\"prijsEN\":\"0.335349\",\"prijsEVO\":\"0.363159\",\"prijsEZ\":\"0.364759\",\"prijsFR\":\"0.340159\",\"prijsGSL\":\"0.363159\",\"prijsMDE\":\"0.363159\",\"prijsNE\":\"0.334659\",\"prijsTI\":\"0.336539\",\"prijsVDB\":\"0.336639\",\"prijsVON\":\"0.334759\",\"prijsWE\":\"0.340159\",\"prijsZG\":\"0.363159\",\"prijsZP\":\"0.934759\"},{\"datum\":\"2024-09-23
    // 09:00:00\",\"prijs\":\"0.109990\",\"prijsAA\":\"0.285938\",\"prijsAIP\":\"0.294988\",\"prijsANWB\":\"0.313138\",\"prijsBE\":\"0.285728\",\"prijsEE\":\"0.291588\",\"prijsEN\":\"0.285328\",\"prijsEVO\":\"0.313138\",\"prijsEZ\":\"0.314738\",\"prijsFR\":\"0.290138\",\"prijsGSL\":\"0.313138\",\"prijsMDE\":\"0.313138\",\"prijsNE\":\"0.284638\",\"prijsTI\":\"0.286518\",\"prijsVDB\":\"0.286618\",\"prijsVON\":\"0.284738\",\"prijsWE\":\"0.290138\",\"prijsZG\":\"0.313138\",\"prijsZP\":\"0.284738\"},{\"datum\":\"2024-09-23
    // 10:00:00\",\"prijs\":\"0.096900\",\"prijsAA\":\"0.270099\",\"prijsAIP\":\"0.279149\",\"prijsANWB\":\"0.297299\",\"prijsBE\":\"0.269889\",\"prijsEE\":\"0.275749\",\"prijsEN\":\"0.269489\",\"prijsEVO\":\"0.297299\",\"prijsEZ\":\"0.298899\",\"prijsFR\":\"0.274299\",\"prijsGSL\":\"0.297299\",\"prijsMDE\":\"0.297299\",\"prijsNE\":\"0.268799\",\"prijsTI\":\"0.270679\",\"prijsVDB\":\"0.270779\",\"prijsVON\":\"0.268899\",\"prijsWE\":\"0.274299\",\"prijsZG\":\"0.297299\",\"prijsZP\":\"0.268899\"},{\"datum\":\"2024-09-23
    // 11:00:00\",\"prijs\":\"0.080160\",\"prijsAA\":\"0.249844\",\"prijsAIP\":\"0.258894\",\"prijsANWB\":\"0.277044\",\"prijsBE\":\"0.249634\",\"prijsEE\":\"0.255494\",\"prijsEN\":\"0.249234\",\"prijsEVO\":\"0.277044\",\"prijsEZ\":\"0.278644\",\"prijsFR\":\"0.254044\",\"prijsGSL\":\"0.277044\",\"prijsMDE\":\"0.277044\",\"prijsNE\":\"0.248544\",\"prijsTI\":\"0.250424\",\"prijsVDB\":\"0.250524\",\"prijsVON\":\"0.248644\",\"prijsWE\":\"0.254044\",\"prijsZG\":\"0.277044\",\"prijsZP\":\"0.248644\"},{\"datum\":\"2024-09-23
    // 12:00:00\",\"prijs\":\"0.078940\",\"prijsAA\":\"0.248367\",\"prijsAIP\":\"0.257417\",\"prijsANWB\":\"0.275567\",\"prijsBE\":\"0.248157\",\"prijsEE\":\"0.254017\",\"prijsEN\":\"0.247757\",\"prijsEVO\":\"0.275567\",\"prijsEZ\":\"0.277167\",\"prijsFR\":\"0.252567\",\"prijsGSL\":\"0.275567\",\"prijsMDE\":\"0.275567\",\"prijsNE\":\"0.247067\",\"prijsTI\":\"0.248947\",\"prijsVDB\":\"0.249047\",\"prijsVON\":\"0.247167\",\"prijsWE\":\"0.252567\",\"prijsZG\":\"0.275567\",\"prijsZP\":\"0.247167\"},{\"datum\":\"2024-09-23
    // 13:00:00\",\"prijs\":\"0.073000\",\"prijsAA\":\"0.241180\",\"prijsAIP\":\"0.250230\",\"prijsANWB\":\"0.268380\",\"prijsBE\":\"0.240970\",\"prijsEE\":\"0.246830\",\"prijsEN\":\"0.240570\",\"prijsEVO\":\"0.268380\",\"prijsEZ\":\"0.269980\",\"prijsFR\":\"0.245380\",\"prijsGSL\":\"0.268380\",\"prijsMDE\":\"0.268380\",\"prijsNE\":\"0.239880\",\"prijsTI\":\"0.241760\",\"prijsVDB\":\"0.241860\",\"prijsVON\":\"0.239980\",\"prijsWE\":\"0.245380\",\"prijsZG\":\"0.268380\",\"prijsZP\":\"0.239980\"},{\"datum\":\"2024-09-23
    // 14:00:00\",\"prijs\":\"0.070420\",\"prijsAA\":\"0.238058\",\"prijsAIP\":\"0.247108\",\"prijsANWB\":\"0.265258\",\"prijsBE\":\"0.237848\",\"prijsEE\":\"0.243708\",\"prijsEN\":\"0.237448\",\"prijsEVO\":\"0.265258\",\"prijsEZ\":\"0.266858\",\"prijsFR\":\"0.242258\",\"prijsGSL\":\"0.265258\",\"prijsMDE\":\"0.265258\",\"prijsNE\":\"0.236758\",\"prijsTI\":\"0.238638\",\"prijsVDB\":\"0.238738\",\"prijsVON\":\"0.236858\",\"prijsWE\":\"0.242258\",\"prijsZG\":\"0.265258\",\"prijsZP\":\"0.236858\"},{\"datum\":\"2024-09-23
    // 15:00:00\",\"prijs\":\"0.069530\",\"prijsAA\":\"0.236981\",\"prijsAIP\":\"0.246031\",\"prijsANWB\":\"0.264181\",\"prijsBE\":\"0.236771\",\"prijsEE\":\"0.242631\",\"prijsEN\":\"0.236371\",\"prijsEVO\":\"0.264181\",\"prijsEZ\":\"0.265781\",\"prijsFR\":\"0.241181\",\"prijsGSL\":\"0.264181\",\"prijsMDE\":\"0.264181\",\"prijsNE\":\"0.235681\",\"prijsTI\":\"0.237561\",\"prijsVDB\":\"0.237661\",\"prijsVON\":\"0.235781\",\"prijsWE\":\"0.241181\",\"prijsZG\":\"0.264181\",\"prijsZP\":\"0.235781\"},{\"datum\":\"2024-09-23
    // 16:00:00\",\"prijs\":\"0.090540\",\"prijsAA\":\"0.262403\",\"prijsAIP\":\"0.271453\",\"prijsANWB\":\"0.289603\",\"prijsBE\":\"0.262193\",\"prijsEE\":\"0.268053\",\"prijsEN\":\"0.261793\",\"prijsEVO\":\"0.289603\",\"prijsEZ\":\"0.291203\",\"prijsFR\":\"0.266603\",\"prijsGSL\":\"0.289603\",\"prijsMDE\":\"0.289603\",\"prijsNE\":\"0.261103\",\"prijsTI\":\"0.262983\",\"prijsVDB\":\"0.263083\",\"prijsVON\":\"0.261203\",\"prijsWE\":\"0.266603\",\"prijsZG\":\"0.289603\",\"prijsZP\":\"0.261203\"},{\"datum\":\"2024-09-23
    // 17:00:00\",\"prijs\":\"0.118240\",\"prijsAA\":\"0.295920\",\"prijsAIP\":\"0.304970\",\"prijsANWB\":\"0.323120\",\"prijsBE\":\"0.295710\",\"prijsEE\":\"0.301570\",\"prijsEN\":\"0.295310\",\"prijsEVO\":\"0.323120\",\"prijsEZ\":\"0.324720\",\"prijsFR\":\"0.300120\",\"prijsGSL\":\"0.323120\",\"prijsMDE\":\"0.323120\",\"prijsNE\":\"0.294620\",\"prijsTI\":\"0.296500\",\"prijsVDB\":\"0.296600\",\"prijsVON\":\"0.294720\",\"prijsWE\":\"0.300120\",\"prijsZG\":\"0.323120\",\"prijsZP\":\"0.294720\"},{\"datum\":\"2024-09-23
    // 18:00:00\",\"prijs\":\"0.205510\",\"prijsAA\":\"0.401517\",\"prijsAIP\":\"0.410567\",\"prijsANWB\":\"0.428717\",\"prijsBE\":\"0.401307\",\"prijsEE\":\"0.407167\",\"prijsEN\":\"0.400907\",\"prijsEVO\":\"0.428717\",\"prijsEZ\":\"0.430317\",\"prijsFR\":\"0.405717\",\"prijsGSL\":\"0.428717\",\"prijsMDE\":\"0.428717\",\"prijsNE\":\"0.400217\",\"prijsTI\":\"0.402097\",\"prijsVDB\":\"0.402197\",\"prijsVON\":\"0.400317\",\"prijsWE\":\"0.405717\",\"prijsZG\":\"0.428717\",\"prijsZP\":\"0.400317\"},{\"datum\":\"2024-09-23
    // 19:00:00\",\"prijs\":\"0.283290\",\"prijsAA\":\"0.495631\",\"prijsAIP\":\"0.504681\",\"prijsANWB\":\"0.522831\",\"prijsBE\":\"0.495421\",\"prijsEE\":\"0.501281\",\"prijsEN\":\"0.495021\",\"prijsEVO\":\"0.522831\",\"prijsEZ\":\"0.524431\",\"prijsFR\":\"0.499831\",\"prijsGSL\":\"0.522831\",\"prijsMDE\":\"0.522831\",\"prijsNE\":\"0.494331\",\"prijsTI\":\"0.496211\",\"prijsVDB\":\"0.496311\",\"prijsVON\":\"0.494431\",\"prijsWE\":\"0.499831\",\"prijsZG\":\"0.522831\",\"prijsZP\":\"0.494431\"},{\"datum\":\"2024-09-23
    // 20:00:00\",\"prijs\":\"0.167990\",\"prijsAA\":\"0.356118\",\"prijsAIP\":\"0.365168\",\"prijsANWB\":\"0.383318\",\"prijsBE\":\"0.355908\",\"prijsEE\":\"0.361768\",\"prijsEN\":\"0.355508\",\"prijsEVO\":\"0.383318\",\"prijsEZ\":\"0.384918\",\"prijsFR\":\"0.360318\",\"prijsGSL\":\"0.383318\",\"prijsMDE\":\"0.383318\",\"prijsNE\":\"0.354818\",\"prijsTI\":\"0.356698\",\"prijsVDB\":\"0.356798\",\"prijsVON\":\"0.354918\",\"prijsWE\":\"0.360318\",\"prijsZG\":\"0.383318\",\"prijsZP\":\"0.354918\"},{\"datum\":\"2024-09-23
    // 21:00:00\",\"prijs\":\"0.108870\",\"prijsAA\":\"0.284583\",\"prijsAIP\":\"0.293633\",\"prijsANWB\":\"0.311783\",\"prijsBE\":\"0.284373\",\"prijsEE\":\"0.290233\",\"prijsEN\":\"0.283973\",\"prijsEVO\":\"0.311783\",\"prijsEZ\":\"0.313383\",\"prijsFR\":\"0.288783\",\"prijsGSL\":\"0.311783\",\"prijsMDE\":\"0.311783\",\"prijsNE\":\"0.283283\",\"prijsTI\":\"0.285163\",\"prijsVDB\":\"0.285263\",\"prijsVON\":\"0.283383\",\"prijsWE\":\"0.288783\",\"prijsZG\":\"0.311783\",\"prijsZP\":\"0.283383\"},{\"datum\":\"2024-09-23
    // 22:00:00\",\"prijs\":\"0.090840\",\"prijsAA\":\"0.262766\",\"prijsAIP\":\"0.271816\",\"prijsANWB\":\"0.289966\",\"prijsBE\":\"0.262556\",\"prijsEE\":\"0.268416\",\"prijsEN\":\"0.262156\",\"prijsEVO\":\"0.289966\",\"prijsEZ\":\"0.291566\",\"prijsFR\":\"0.266966\",\"prijsGSL\":\"0.289966\",\"prijsMDE\":\"0.289966\",\"prijsNE\":\"0.261466\",\"prijsTI\":\"0.263346\",\"prijsVDB\":\"0.263446\",\"prijsVON\":\"0.261566\",\"prijsWE\":\"0.266966\",\"prijsZG\":\"0.289966\",\"prijsZP\":\"0.261566\"},{\"datum\":\"2024-09-23
    // 23:00:00\",\"prijs\":\"0.084490\",\"prijsAA\":\"0.255083\",\"prijsAIP\":\"0.264133\",\"prijsANWB\":\"0.282283\",\"prijsBE\":\"0.254873\",\"prijsEE\":\"0.260733\",\"prijsEN\":\"0.254473\",\"prijsEVO\":\"0.282283\",\"prijsEZ\":\"0.283883\",\"prijsFR\":\"0.259283\",\"prijsGSL\":\"0.282283\",\"prijsMDE\":\"0.282283\",\"prijsNE\":\"0.253783\",\"prijsTI\":\"0.255663\",\"prijsVDB\":\"0.255763\",\"prijsVON\":\"0.253883\",\"prijsWE\":\"0.259283\",\"prijsZG\":\"0.282283\",\"prijsZP\":\"0.253883\"}],\"code\":\"5\"}";
    private String testDataG = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-09-24 06:00:00\",\"prijsEGSI\":\"0.350059\",\"prijsEOD\":\"0.354690\",\"prijsAA\":\"1.201611\",\"prijsAIP\":\"1.236701\",\"prijsANWB\":\"1.188121\",\"prijsBE\":\"1.204021\",\"prijsEE\":\"1.254199\",\"prijsEN\":\"1.208001\",\"prijsEVO\":\"1.188121\",\"prijsEZ\":\"1.189011\",\"prijsFR\":\"1.214475\",\"prijsGSL\":\"1.188121\",\"prijsMDE\":\"1.188121\",\"prijsNE\":\"1.188011\",\"prijsVDB\":\"1.235631\",\"prijsVON\":\"1.208911\",\"prijsWE\":\"1.213711\",\"prijsZG\":\"1.188121\",\"prijsZP\":\"1.209011\"}],\"code\":\"5\"}";

    private String token = "";

    private double treshold = 0;

    private boolean excludeNightlyHours = false;

    private Hashtable<Integer, Double> ePrices = new Hashtable<>();
    private double averagePrice = 0;

    private double gasPrice = 0;

    private int numberOfHours = 0;

    private int numberOfHoursBeforeWarning = 0;
    private double warningTreshold = 0;

    private List<Integer> cheapHours = new ArrayList<Integer>();
    private List<Integer> expensiveHours = new ArrayList<Integer>();

    private boolean debug = false;

    public EneVerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // None
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(EneVerConfiguration.class);
        if (configure()) {

            // get prices for today
            if (retrieveElectricityPrices(true) && retrieveGasPrice()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            var now = LocalDateTime.now();

            // update channels
            determineCheapAndExpensiveHours();
            updateDailyChannels();
            updateHourlyChannels(now.getHour());

            // schedule get prices next day
            long nextDailyScheduleInNanos = Duration
                    .between(now, now.withHour(23).withMinute(55).withSecond(0).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::scheduleDailyPrices, nextDailyScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update channels hourly
            long nextHourlyScheduleInNanos = Duration
                    .between(now, now.plusHours(1).withMinute(0).withSecond(0).withNano(0)).toNanos();
            hourlyJob = scheduler.scheduleWithFixedDelay(this::scheduleHourlyPrices, nextHourlyScheduleInNanos,
                    TimeUnit.HOURS.toNanos(1), TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    @SuppressWarnings("null")
    private boolean configure() {
        if (config == null || config.token.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing token configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            token = config.token;
            numberOfHours = config.numberOfHours;
            numberOfHoursBeforeWarning = config.numberOfHoursBeforeWarning;
            warningTreshold = (double) config.warningTreshold / 100;
            debug = config.debug;
            excludeNightlyHours = config.excludeNightlyHours;
            treshold = (double) config.priceTreshold / 100;
            return true;
        }
    }

    private boolean retrieveElectricityPrices(boolean today) {
        String url;
        if (today) {
            url = "https://enever.nl/api/stroomprijs_vandaag.php?token=" + token;
        } else {
            url = "https://enever.nl/api/stroomprijs_morgen.php?token=" + token;
        }

        Payload p = null;
        if (!debug) {
            p = retrievePayload(url);
        } else {
            logger.debug("Using test electricity data");
            p = gson.fromJson(testDataE, Payload.class);
        }

        if (p == null) {
            return false;
        }
        if (p.getStatus()) {
            ePrices = new Hashtable<>();
            averagePrice = 0;

            p.getPrices().forEach((price) -> {
                ePrices.put(price.getTime().getHour(), price.getPrijs());
                averagePrice += price.getPrijs();
            });
            averagePrice = averagePrice / ePrices.size();
        }

        return p.getStatus();
    }

    private boolean retrieveGasPrice() {
        String url = "https://enever.nl/api/gasprijs_vandaag.php?token=" + token;

        Payload p = null;
        if (!debug) {
            p = retrievePayload(url);
        } else {
            logger.debug("Using test gas data");
            p = gson.fromJson(testDataG, Payload.class);
        }

        if (p == null) {
            return false;
        }
        if (p.getStatus()) {
            gasPrice = 0;
        }

        p.getPrices().forEach((price) -> {
            gasPrice = price.getPrijs();
        });

        return p.getStatus();
    }

    private @Nullable Payload retrievePayload(String url) {
        @Nullable
        String dataResult = null;
        try {
            dataResult = HttpUtil.executeUrl("GET", url, 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device data: %s", e.getMessage()));
        }

        if (dataResult == null || dataResult.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return null;
        }

        Payload payload = null;
        try {
            payload = gson.fromJson(dataResult, Payload.class);
        } catch (JsonSyntaxException ex) {
            logger.debug(dataResult);
        }

        if (payload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse data response from device");
            return null;
        }

        if (!payload.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return null;
        }
        return payload;
    }

    private void updateDailyChannels() {
        updateState(EneVerBindingConstants.CHANNEL_AVG_ELECTRICITY_PRICE, new DecimalType(averagePrice));
        updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(gasPrice));
    }

    private void updateHourlyChannels(int hour) {
        logger.debug("updating channels for " + hour);
        if (ePrices.containsKey(hour)) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE, new DecimalType(ePrices.get(hour)));
        }

        if (cheapHours.contains(hour)) {
            updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(1));
        } else if (expensiveHours.contains(hour)) {
            updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(-1));
        } else {
            updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(0));
        }

        if (ePrices.containsKey(hour) && ePrices.containsKey(hour + numberOfHoursBeforeWarning)) {
            var warn = ePrices.get(hour) * (1 + warningTreshold) < ePrices.get(hour + numberOfHoursBeforeWarning);
            updateState(EneVerBindingConstants.CHANNEL_PRICE_WARNING, OnOffType.from(warn));
        } else {
            updateState(EneVerBindingConstants.CHANNEL_PRICE_WARNING, OnOffType.from(false));
        }
    }

    protected void scheduleDailyPrices() {
        retrieveElectricityPrices(false);
        retrieveGasPrice();
        determineCheapAndExpensiveHours();
        updateDailyChannels();
    }

    protected void scheduleHourlyPrices() {
        updateHourlyChannels(LocalDateTime.now().getHour());
    }

    private void determineCheapAndExpensiveHours() {
        cheapHours.clear();
        expensiveHours.clear();

        logger.debug("Using treshold " + treshold * 100 + "%");
        if (excludeNightlyHours) {
            logger.debug("Excluding nightly hours ");
        }

        for (Entry<Integer, Double> entry : ePrices.entrySet()) {
            logger.debug("key = " + entry.getKey() + ", value = " + entry.getValue());
        }

        Map<Integer, Double> ePricesCheap = ePrices.entrySet().stream()
                .filter(e -> !excludeNightlyHours || e.getKey() > 5)
                .filter(e -> e.getValue() <= averagePrice * (1 - treshold))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        Map<Integer, Double> ePricesExpensive = ePrices.entrySet().stream()
                .filter(e -> !excludeNightlyHours || e.getKey() > 5)
                .filter(e -> e.getValue() >= averagePrice * (1 + treshold))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        // for (Entry<Integer, Double> entry : ePricesCheap.entrySet()) {
        // logger.debug("CHEAP key = " + entry.getKey() + ", value = " + entry.getValue());
        // }
        //
        // for (Entry<Integer, Double> entry : ePricesExpensive.entrySet()) {
        // logger.debug("EXPENSIVE key = " + entry.getKey() + ", value = " + entry.getValue());
        // }

        var prices = new ArrayList<Double>();

        for (Entry<Integer, Double> entry : ePricesCheap.entrySet()) {
            prices.add(entry.getValue());
        }
        Collections.sort(prices);

        for (int i = 0; i < Math.min(numberOfHours, prices.size()); i++) {
            for (Entry<Integer, Double> entry : ePricesCheap.entrySet()) {
                if (entry.getValue().equals(prices.get(i)) && !cheapHours.contains(entry.getKey())) {
                    cheapHours.add(entry.getKey());
                    break;
                }
            }
        }

        prices.clear();
        for (Entry<Integer, Double> entry : ePricesExpensive.entrySet()) {
            prices.add(entry.getValue());
        }
        Collections.sort(prices, Collections.reverseOrder());

        for (int i = 0; i < Math.min(numberOfHours, prices.size()); i++) {
            for (Entry<Integer, Double> entry : ePricesExpensive.entrySet()) {
                if (entry.getValue().equals(prices.get(i)) && !expensiveHours.contains(entry.getKey())) {
                    expensiveHours.add(entry.getKey());
                    break;
                }
            }
        }
        logger.debug("cheap " + cheapHours);
        logger.debug("expensive " + expensiveHours);
    }

    @Override
    public void dispose() {
        var job = dailyJob;
        if (job != null) {
            job.cancel(true);
        }
        dailyJob = null;

        job = hourlyJob;
        if (job != null) {
            job.cancel(true);
        }
        hourlyJob = null;
    }

}
