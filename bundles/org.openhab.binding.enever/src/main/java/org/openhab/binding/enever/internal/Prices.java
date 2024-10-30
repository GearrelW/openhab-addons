package org.openhab.binding.enever.internal;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class Prices {

    private final Logger logger = LoggerFactory.getLogger(Prices.class);

    // private String testDataToday = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-10-21
    // 00:00:00\",\"prijs\":\"-0.000100\",\"prijsAA\":\"0.152729\",\"prijsAIP\":\"0.161779\",\"prijsANWB\":\"0.179929\",\"prijsBE\":\"0.152519\",\"prijsEE\":\"0.158379\",\"prijsEN\":\"0.154919\",\"prijsEVO\":\"0.179929\",\"prijsEZ\":\"0.181529\",\"prijsFR\":\"0.149729\",\"prijsGSL\":\"0.179929\",\"prijsMDE\":\"0.179929\",\"prijsNE\":\"0.151429\",\"prijsTI\":\"0.153309\",\"prijsVDB\":\"0.153409\",\"prijsVON\":\"0.151529\",\"prijsWE\":\"0.156929\",\"prijsZG\":\"0.179929\",\"prijsZP\":\"0.151529\"},{\"datum\":\"2024-10-21
    // 01:00:00\",\"prijs\":\"-0.000140\",\"prijsAA\":\"0.152681\",\"prijsAIP\":\"0.161731\",\"prijsANWB\":\"0.179881\",\"prijsBE\":\"0.152471\",\"prijsEE\":\"0.158331\",\"prijsEN\":\"0.154871\",\"prijsEVO\":\"0.179881\",\"prijsEZ\":\"0.181481\",\"prijsFR\":\"0.149681\",\"prijsGSL\":\"0.179881\",\"prijsMDE\":\"0.179881\",\"prijsNE\":\"0.151381\",\"prijsTI\":\"0.153261\",\"prijsVDB\":\"0.153361\",\"prijsVON\":\"0.151481\",\"prijsWE\":\"0.156881\",\"prijsZG\":\"0.179881\",\"prijsZP\":\"0.151481\"},{\"datum\":\"2024-10-21
    // 02:00:00\",\"prijs\":\"-0.000080\",\"prijsAA\":\"0.152753\",\"prijsAIP\":\"0.161803\",\"prijsANWB\":\"0.179953\",\"prijsBE\":\"0.152543\",\"prijsEE\":\"0.158403\",\"prijsEN\":\"0.154943\",\"prijsEVO\":\"0.179953\",\"prijsEZ\":\"0.181553\",\"prijsFR\":\"0.149753\",\"prijsGSL\":\"0.179953\",\"prijsMDE\":\"0.179953\",\"prijsNE\":\"0.151453\",\"prijsTI\":\"0.153333\",\"prijsVDB\":\"0.153433\",\"prijsVON\":\"0.151553\",\"prijsWE\":\"0.156953\",\"prijsZG\":\"0.179953\",\"prijsZP\":\"0.151553\"},{\"datum\":\"2024-10-21
    // 03:00:00\",\"prijs\":\"0.000000\",\"prijsAA\":\"0.152850\",\"prijsAIP\":\"0.161900\",\"prijsANWB\":\"0.180050\",\"prijsBE\":\"0.152640\",\"prijsEE\":\"0.158500\",\"prijsEN\":\"0.155040\",\"prijsEVO\":\"0.180050\",\"prijsEZ\":\"0.181650\",\"prijsFR\":\"0.149850\",\"prijsGSL\":\"0.180050\",\"prijsMDE\":\"0.180050\",\"prijsNE\":\"0.151550\",\"prijsTI\":\"0.153430\",\"prijsVDB\":\"0.153530\",\"prijsVON\":\"0.151650\",\"prijsWE\":\"0.157050\",\"prijsZG\":\"0.180050\",\"prijsZP\":\"0.151650\"},{\"datum\":\"2024-10-21
    // 04:00:00\",\"prijs\":\"0.003450\",\"prijsAA\":\"0.157025\",\"prijsAIP\":\"0.166075\",\"prijsANWB\":\"0.184225\",\"prijsBE\":\"0.156815\",\"prijsEE\":\"0.162674\",\"prijsEN\":\"0.159215\",\"prijsEVO\":\"0.184225\",\"prijsEZ\":\"0.185824\",\"prijsFR\":\"0.154024\",\"prijsGSL\":\"0.184225\",\"prijsMDE\":\"0.184225\",\"prijsNE\":\"0.155725\",\"prijsTI\":\"0.157605\",\"prijsVDB\":\"0.157704\",\"prijsVON\":\"0.155825\",\"prijsWE\":\"0.161225\",\"prijsZG\":\"0.184225\",\"prijsZP\":\"0.155825\"},{\"datum\":\"2024-10-21
    // 05:00:00\",\"prijs\":\"0.061960\",\"prijsAA\":\"0.227822\",\"prijsAIP\":\"0.236872\",\"prijsANWB\":\"0.255022\",\"prijsBE\":\"0.227612\",\"prijsEE\":\"0.233472\",\"prijsEN\":\"0.230012\",\"prijsEVO\":\"0.255022\",\"prijsEZ\":\"0.256622\",\"prijsFR\":\"0.224822\",\"prijsGSL\":\"0.255022\",\"prijsMDE\":\"0.255022\",\"prijsNE\":\"0.226522\",\"prijsTI\":\"0.228402\",\"prijsVDB\":\"0.228502\",\"prijsVON\":\"0.226622\",\"prijsWE\":\"0.232022\",\"prijsZG\":\"0.255022\",\"prijsZP\":\"0.226622\"},{\"datum\":\"2024-10-21
    // 06:00:00\",\"prijs\":\"0.104130\",\"prijsAA\":\"0.278847\",\"prijsAIP\":\"0.287897\",\"prijsANWB\":\"0.306047\",\"prijsBE\":\"0.278637\",\"prijsEE\":\"0.284497\",\"prijsEN\":\"0.281037\",\"prijsEVO\":\"0.306047\",\"prijsEZ\":\"0.307647\",\"prijsFR\":\"0.275847\",\"prijsGSL\":\"0.306047\",\"prijsMDE\":\"0.306047\",\"prijsNE\":\"0.277547\",\"prijsTI\":\"0.279427\",\"prijsVDB\":\"0.279527\",\"prijsVON\":\"0.277647\",\"prijsWE\":\"0.283047\",\"prijsZG\":\"0.306047\",\"prijsZP\":\"0.277647\"},{\"datum\":\"2024-10-21
    // 07:00:00\",\"prijs\":\"0.120350\",\"prijsAA\":\"0.298474\",\"prijsAIP\":\"0.307524\",\"prijsANWB\":\"0.325674\",\"prijsBE\":\"0.298264\",\"prijsEE\":\"0.304124\",\"prijsEN\":\"0.300664\",\"prijsEVO\":\"0.325674\",\"prijsEZ\":\"0.327274\",\"prijsFR\":\"0.295474\",\"prijsGSL\":\"0.325674\",\"prijsMDE\":\"0.325674\",\"prijsNE\":\"0.297174\",\"prijsTI\":\"0.299054\",\"prijsVDB\":\"0.299154\",\"prijsVON\":\"0.297274\",\"prijsWE\":\"0.302674\",\"prijsZG\":\"0.325674\",\"prijsZP\":\"0.297274\"},{\"datum\":\"2024-10-21
    // 08:00:00\",\"prijs\":\"0.103900\",\"prijsAA\":\"0.278569\",\"prijsAIP\":\"0.287619\",\"prijsANWB\":\"0.305769\",\"prijsBE\":\"0.278359\",\"prijsEE\":\"0.284219\",\"prijsEN\":\"0.280759\",\"prijsEVO\":\"0.305769\",\"prijsEZ\":\"0.307369\",\"prijsFR\":\"0.275569\",\"prijsGSL\":\"0.305769\",\"prijsMDE\":\"0.305769\",\"prijsNE\":\"0.277269\",\"prijsTI\":\"0.279149\",\"prijsVDB\":\"0.279249\",\"prijsVON\":\"0.277369\",\"prijsWE\":\"0.282769\",\"prijsZG\":\"0.305769\",\"prijsZP\":\"0.277369\"},{\"datum\":\"2024-10-21
    // 09:00:00\",\"prijs\":\"0.091000\",\"prijsAA\":\"0.262960\",\"prijsAIP\":\"0.272010\",\"prijsANWB\":\"0.290160\",\"prijsBE\":\"0.262750\",\"prijsEE\":\"0.268610\",\"prijsEN\":\"0.265150\",\"prijsEVO\":\"0.290160\",\"prijsEZ\":\"0.291760\",\"prijsFR\":\"0.259960\",\"prijsGSL\":\"0.290160\",\"prijsMDE\":\"0.290160\",\"prijsNE\":\"0.261660\",\"prijsTI\":\"0.263540\",\"prijsVDB\":\"0.263640\",\"prijsVON\":\"0.261760\",\"prijsWE\":\"0.267160\",\"prijsZG\":\"0.290160\",\"prijsZP\":\"0.261760\"},{\"datum\":\"2024-10-21
    // 10:00:00\",\"prijs\":\"0.084910\",\"prijsAA\":\"0.255591\",\"prijsAIP\":\"0.264641\",\"prijsANWB\":\"0.282791\",\"prijsBE\":\"0.255381\",\"prijsEE\":\"0.261241\",\"prijsEN\":\"0.257781\",\"prijsEVO\":\"0.282791\",\"prijsEZ\":\"0.284391\",\"prijsFR\":\"0.252591\",\"prijsGSL\":\"0.282791\",\"prijsMDE\":\"0.282791\",\"prijsNE\":\"0.254291\",\"prijsTI\":\"0.256171\",\"prijsVDB\":\"0.256271\",\"prijsVON\":\"0.254391\",\"prijsWE\":\"0.259791\",\"prijsZG\":\"0.282791\",\"prijsZP\":\"0.254391\"},{\"datum\":\"2024-10-21
    // 11:00:00\",\"prijs\":\"0.080000\",\"prijsAA\":\"0.249650\",\"prijsAIP\":\"0.258700\",\"prijsANWB\":\"0.276850\",\"prijsBE\":\"0.249440\",\"prijsEE\":\"0.255300\",\"prijsEN\":\"0.251840\",\"prijsEVO\":\"0.276850\",\"prijsEZ\":\"0.278450\",\"prijsFR\":\"0.246650\",\"prijsGSL\":\"0.276850\",\"prijsMDE\":\"0.276850\",\"prijsNE\":\"0.248350\",\"prijsTI\":\"0.250230\",\"prijsVDB\":\"0.250330\",\"prijsVON\":\"0.248450\",\"prijsWE\":\"0.253850\",\"prijsZG\":\"0.276850\",\"prijsZP\":\"0.248450\"},{\"datum\":\"2024-10-21
    // 12:00:00\",\"prijs\":\"0.083150\",\"prijsAA\":\"0.253462\",\"prijsAIP\":\"0.262512\",\"prijsANWB\":\"0.280662\",\"prijsBE\":\"0.253252\",\"prijsEE\":\"0.259112\",\"prijsEN\":\"0.255652\",\"prijsEVO\":\"0.280662\",\"prijsEZ\":\"0.282262\",\"prijsFR\":\"0.250462\",\"prijsGSL\":\"0.280662\",\"prijsMDE\":\"0.280662\",\"prijsNE\":\"0.252162\",\"prijsTI\":\"0.254042\",\"prijsVDB\":\"0.254141\",\"prijsVON\":\"0.252262\",\"prijsWE\":\"0.257662\",\"prijsZG\":\"0.280662\",\"prijsZP\":\"0.952262\"},{\"datum\":\"2024-10-21
    // 13:00:00\",\"prijs\":\"0.083190\",\"prijsAA\":\"0.253510\",\"prijsAIP\":\"0.262560\",\"prijsANWB\":\"0.280710\",\"prijsBE\":\"0.253300\",\"prijsEE\":\"0.259160\",\"prijsEN\":\"0.255700\",\"prijsEVO\":\"0.280710\",\"prijsEZ\":\"0.282310\",\"prijsFR\":\"0.250510\",\"prijsGSL\":\"0.280710\",\"prijsMDE\":\"0.280710\",\"prijsNE\":\"0.252210\",\"prijsTI\":\"0.254090\",\"prijsVDB\":\"0.254190\",\"prijsVON\":\"0.252310\",\"prijsWE\":\"0.257710\",\"prijsZG\":\"0.280710\",\"prijsZP\":\"0.152310\"},{\"datum\":\"2024-10-21
    // 14:00:00\",\"prijs\":\"0.095820\",\"prijsAA\":\"0.268792\",\"prijsAIP\":\"0.277842\",\"prijsANWB\":\"0.295992\",\"prijsBE\":\"0.268582\",\"prijsEE\":\"0.274442\",\"prijsEN\":\"0.270982\",\"prijsEVO\":\"0.295992\",\"prijsEZ\":\"0.297592\",\"prijsFR\":\"0.265792\",\"prijsGSL\":\"0.295992\",\"prijsMDE\":\"0.295992\",\"prijsNE\":\"0.267492\",\"prijsTI\":\"0.269372\",\"prijsVDB\":\"0.269472\",\"prijsVON\":\"0.267592\",\"prijsWE\":\"0.272992\",\"prijsZG\":\"0.295992\",\"prijsZP\":\"0.267592\"},{\"datum\":\"2024-10-21
    // 15:00:00\",\"prijs\":\"0.116570\",\"prijsAA\":\"0.293900\",\"prijsAIP\":\"0.302950\",\"prijsANWB\":\"0.321100\",\"prijsBE\":\"0.293690\",\"prijsEE\":\"0.299550\",\"prijsEN\":\"0.296090\",\"prijsEVO\":\"0.321100\",\"prijsEZ\":\"0.322700\",\"prijsFR\":\"0.290900\",\"prijsGSL\":\"0.321100\",\"prijsMDE\":\"0.321100\",\"prijsNE\":\"0.292600\",\"prijsTI\":\"0.294480\",\"prijsVDB\":\"0.294580\",\"prijsVON\":\"0.292700\",\"prijsWE\":\"0.298100\",\"prijsZG\":\"0.321100\",\"prijsZP\":\"0.292700\"},{\"datum\":\"2024-10-21
    // 16:00:00\",\"prijs\":\"0.130040\",\"prijsAA\":\"0.310198\",\"prijsAIP\":\"0.319248\",\"prijsANWB\":\"0.337398\",\"prijsBE\":\"0.309988\",\"prijsEE\":\"0.315848\",\"prijsEN\":\"0.312388\",\"prijsEVO\":\"0.337398\",\"prijsEZ\":\"0.338998\",\"prijsFR\":\"0.307198\",\"prijsGSL\":\"0.337398\",\"prijsMDE\":\"0.337398\",\"prijsNE\":\"0.308898\",\"prijsTI\":\"0.310778\",\"prijsVDB\":\"0.310878\",\"prijsVON\":\"0.308998\",\"prijsWE\":\"0.314398\",\"prijsZG\":\"0.337398\",\"prijsZP\":\"0.308998\"},{\"datum\":\"2024-10-21
    // 17:00:00\",\"prijs\":\"0.212150\",\"prijsAA\":\"0.409552\",\"prijsAIP\":\"0.418602\",\"prijsANWB\":\"0.436752\",\"prijsBE\":\"0.409342\",\"prijsEE\":\"0.415202\",\"prijsEN\":\"0.411742\",\"prijsEVO\":\"0.436752\",\"prijsEZ\":\"0.438352\",\"prijsFR\":\"0.406552\",\"prijsGSL\":\"0.436752\",\"prijsMDE\":\"0.436752\",\"prijsNE\":\"0.408252\",\"prijsTI\":\"0.410132\",\"prijsVDB\":\"0.410232\",\"prijsVON\":\"0.408352\",\"prijsWE\":\"0.413752\",\"prijsZG\":\"0.436752\",\"prijsZP\":\"0.408352\"},{\"datum\":\"2024-10-21
    // 18:00:00\",\"prijs\":\"0.188350\",\"prijsAA\":\"0.380754\",\"prijsAIP\":\"0.389804\",\"prijsANWB\":\"0.407954\",\"prijsBE\":\"0.380544\",\"prijsEE\":\"0.386404\",\"prijsEN\":\"0.382944\",\"prijsEVO\":\"0.407954\",\"prijsEZ\":\"0.409554\",\"prijsFR\":\"0.377754\",\"prijsGSL\":\"0.407954\",\"prijsMDE\":\"0.407954\",\"prijsNE\":\"0.379454\",\"prijsTI\":\"0.381334\",\"prijsVDB\":\"0.381434\",\"prijsVON\":\"0.379554\",\"prijsWE\":\"0.384954\",\"prijsZG\":\"0.407954\",\"prijsZP\":\"0.379554\"},{\"datum\":\"2024-10-21
    // 19:00:00\",\"prijs\":\"0.128180\",\"prijsAA\":\"0.307948\",\"prijsAIP\":\"0.316998\",\"prijsANWB\":\"0.335148\",\"prijsBE\":\"0.307738\",\"prijsEE\":\"0.313598\",\"prijsEN\":\"0.310138\",\"prijsEVO\":\"0.335148\",\"prijsEZ\":\"0.336748\",\"prijsFR\":\"0.304948\",\"prijsGSL\":\"0.335148\",\"prijsMDE\":\"0.335148\",\"prijsNE\":\"0.306648\",\"prijsTI\":\"0.308528\",\"prijsVDB\":\"0.308628\",\"prijsVON\":\"0.306748\",\"prijsWE\":\"0.312148\",\"prijsZG\":\"0.335148\",\"prijsZP\":\"0.306748\"},{\"datum\":\"2024-10-21
    // 20:00:00\",\"prijs\":\"0.100560\",\"prijsAA\":\"0.274528\",\"prijsAIP\":\"0.283578\",\"prijsANWB\":\"0.301728\",\"prijsBE\":\"0.274318\",\"prijsEE\":\"0.280178\",\"prijsEN\":\"0.276718\",\"prijsEVO\":\"0.301728\",\"prijsEZ\":\"0.303328\",\"prijsFR\":\"0.271528\",\"prijsGSL\":\"0.301728\",\"prijsMDE\":\"0.301728\",\"prijsNE\":\"0.273228\",\"prijsTI\":\"0.275108\",\"prijsVDB\":\"0.275208\",\"prijsVON\":\"0.273328\",\"prijsWE\":\"0.278728\",\"prijsZG\":\"0.301728\",\"prijsZP\":\"0.273328\"},{\"datum\":\"2024-10-21
    // 21:00:00\",\"prijs\":\"0.097790\",\"prijsAA\":\"0.271176\",\"prijsAIP\":\"0.280226\",\"prijsANWB\":\"0.298376\",\"prijsBE\":\"0.270966\",\"prijsEE\":\"0.276826\",\"prijsEN\":\"0.273366\",\"prijsEVO\":\"0.298376\",\"prijsEZ\":\"0.299976\",\"prijsFR\":\"0.268176\",\"prijsGSL\":\"0.298376\",\"prijsMDE\":\"0.298376\",\"prijsNE\":\"0.269876\",\"prijsTI\":\"0.271756\",\"prijsVDB\":\"0.271856\",\"prijsVON\":\"0.269976\",\"prijsWE\":\"0.275376\",\"prijsZG\":\"0.298376\",\"prijsZP\":\"0.269976\"},{\"datum\":\"2024-10-21
    // 23:00:00\",\"prijs\":\"0.092970\",\"prijsAA\":\"0.265344\",\"prijsAIP\":\"0.274394\",\"prijsANWB\":\"0.292544\",\"prijsBE\":\"0.265134\",\"prijsEE\":\"0.270994\",\"prijsEN\":\"0.267534\",\"prijsEVO\":\"0.292544\",\"prijsEZ\":\"0.294144\",\"prijsFR\":\"0.262344\",\"prijsGSL\":\"0.292544\",\"prijsMDE\":\"0.292544\",\"prijsNE\":\"0.264044\",\"prijsTI\":\"0.265924\",\"prijsVDB\":\"0.266024\",\"prijsVON\":\"0.264144\",\"prijsWE\":\"0.269544\",\"prijsZG\":\"0.292544\",\"prijsZP\":\"0.264144\"}],\"code\":\"5\"}";
    // private String testDataTomorrow = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-10-21
    // 00:00:00\",\"prijs\":\"-0.000100\",\"prijsAA\":\"0.152729\",\"prijsAIP\":\"0.161779\",\"prijsANWB\":\"0.179929\",\"prijsBE\":\"0.152519\",\"prijsEE\":\"0.158379\",\"prijsEN\":\"0.154919\",\"prijsEVO\":\"0.179929\",\"prijsEZ\":\"0.181529\",\"prijsFR\":\"0.149729\",\"prijsGSL\":\"0.179929\",\"prijsMDE\":\"0.179929\",\"prijsNE\":\"0.151429\",\"prijsTI\":\"0.153309\",\"prijsVDB\":\"0.153409\",\"prijsVON\":\"0.151529\",\"prijsWE\":\"0.156929\",\"prijsZG\":\"0.179929\",\"prijsZP\":\"0.151529\"},{\"datum\":\"2024-10-21
    // 01:00:00\",\"prijs\":\"-0.000140\",\"prijsAA\":\"0.152681\",\"prijsAIP\":\"0.161731\",\"prijsANWB\":\"0.179881\",\"prijsBE\":\"0.152471\",\"prijsEE\":\"0.158331\",\"prijsEN\":\"0.154871\",\"prijsEVO\":\"0.179881\",\"prijsEZ\":\"0.181481\",\"prijsFR\":\"0.149681\",\"prijsGSL\":\"0.179881\",\"prijsMDE\":\"0.179881\",\"prijsNE\":\"0.151381\",\"prijsTI\":\"0.153261\",\"prijsVDB\":\"0.153361\",\"prijsVON\":\"0.151481\",\"prijsWE\":\"0.156881\",\"prijsZG\":\"0.179881\",\"prijsZP\":\"0.151481\"},{\"datum\":\"2024-10-21
    // 02:00:00\",\"prijs\":\"-0.000080\",\"prijsAA\":\"0.152753\",\"prijsAIP\":\"0.161803\",\"prijsANWB\":\"0.179953\",\"prijsBE\":\"0.152543\",\"prijsEE\":\"0.158403\",\"prijsEN\":\"0.154943\",\"prijsEVO\":\"0.179953\",\"prijsEZ\":\"0.181553\",\"prijsFR\":\"0.149753\",\"prijsGSL\":\"0.179953\",\"prijsMDE\":\"0.179953\",\"prijsNE\":\"0.151453\",\"prijsTI\":\"0.153333\",\"prijsVDB\":\"0.153433\",\"prijsVON\":\"0.151553\",\"prijsWE\":\"0.156953\",\"prijsZG\":\"0.179953\",\"prijsZP\":\"0.151553\"},{\"datum\":\"2024-10-21
    // 03:00:00\",\"prijs\":\"0.000000\",\"prijsAA\":\"0.152850\",\"prijsAIP\":\"0.161900\",\"prijsANWB\":\"0.180050\",\"prijsBE\":\"0.152640\",\"prijsEE\":\"0.158500\",\"prijsEN\":\"0.155040\",\"prijsEVO\":\"0.180050\",\"prijsEZ\":\"0.181650\",\"prijsFR\":\"0.149850\",\"prijsGSL\":\"0.180050\",\"prijsMDE\":\"0.180050\",\"prijsNE\":\"0.151550\",\"prijsTI\":\"0.153430\",\"prijsVDB\":\"0.153530\",\"prijsVON\":\"0.151650\",\"prijsWE\":\"0.157050\",\"prijsZG\":\"0.180050\",\"prijsZP\":\"0.151650\"},{\"datum\":\"2024-10-21
    // 04:00:00\",\"prijs\":\"0.003450\",\"prijsAA\":\"0.157025\",\"prijsAIP\":\"0.166075\",\"prijsANWB\":\"0.184225\",\"prijsBE\":\"0.156815\",\"prijsEE\":\"0.162674\",\"prijsEN\":\"0.159215\",\"prijsEVO\":\"0.184225\",\"prijsEZ\":\"0.185824\",\"prijsFR\":\"0.154024\",\"prijsGSL\":\"0.184225\",\"prijsMDE\":\"0.184225\",\"prijsNE\":\"0.155725\",\"prijsTI\":\"0.157605\",\"prijsVDB\":\"0.157704\",\"prijsVON\":\"0.155825\",\"prijsWE\":\"0.161225\",\"prijsZG\":\"0.184225\",\"prijsZP\":\"0.155825\"},{\"datum\":\"2024-10-21
    // 05:00:00\",\"prijs\":\"0.061960\",\"prijsAA\":\"0.227822\",\"prijsAIP\":\"0.236872\",\"prijsANWB\":\"0.255022\",\"prijsBE\":\"0.227612\",\"prijsEE\":\"0.233472\",\"prijsEN\":\"0.230012\",\"prijsEVO\":\"0.255022\",\"prijsEZ\":\"0.256622\",\"prijsFR\":\"0.224822\",\"prijsGSL\":\"0.255022\",\"prijsMDE\":\"0.255022\",\"prijsNE\":\"0.226522\",\"prijsTI\":\"0.228402\",\"prijsVDB\":\"0.228502\",\"prijsVON\":\"0.226622\",\"prijsWE\":\"0.232022\",\"prijsZG\":\"0.255022\",\"prijsZP\":\"0.226622\"},{\"datum\":\"2024-10-21
    // 06:00:00\",\"prijs\":\"0.104130\",\"prijsAA\":\"0.278847\",\"prijsAIP\":\"0.287897\",\"prijsANWB\":\"0.306047\",\"prijsBE\":\"0.278637\",\"prijsEE\":\"0.284497\",\"prijsEN\":\"0.281037\",\"prijsEVO\":\"0.306047\",\"prijsEZ\":\"0.307647\",\"prijsFR\":\"0.275847\",\"prijsGSL\":\"0.306047\",\"prijsMDE\":\"0.306047\",\"prijsNE\":\"0.277547\",\"prijsTI\":\"0.279427\",\"prijsVDB\":\"0.279527\",\"prijsVON\":\"0.277647\",\"prijsWE\":\"0.283047\",\"prijsZG\":\"0.306047\",\"prijsZP\":\"0.277647\"},{\"datum\":\"2024-10-21
    // 07:00:00\",\"prijs\":\"0.120350\",\"prijsAA\":\"0.298474\",\"prijsAIP\":\"0.307524\",\"prijsANWB\":\"0.325674\",\"prijsBE\":\"0.298264\",\"prijsEE\":\"0.304124\",\"prijsEN\":\"0.300664\",\"prijsEVO\":\"0.325674\",\"prijsEZ\":\"0.327274\",\"prijsFR\":\"0.295474\",\"prijsGSL\":\"0.325674\",\"prijsMDE\":\"0.325674\",\"prijsNE\":\"0.297174\",\"prijsTI\":\"0.299054\",\"prijsVDB\":\"0.299154\",\"prijsVON\":\"0.297274\",\"prijsWE\":\"0.302674\",\"prijsZG\":\"0.325674\",\"prijsZP\":\"0.297274\"},{\"datum\":\"2024-10-21
    // 08:00:00\",\"prijs\":\"0.103900\",\"prijsAA\":\"0.278569\",\"prijsAIP\":\"0.287619\",\"prijsANWB\":\"0.305769\",\"prijsBE\":\"0.278359\",\"prijsEE\":\"0.284219\",\"prijsEN\":\"0.280759\",\"prijsEVO\":\"0.305769\",\"prijsEZ\":\"0.307369\",\"prijsFR\":\"0.275569\",\"prijsGSL\":\"0.305769\",\"prijsMDE\":\"0.305769\",\"prijsNE\":\"0.277269\",\"prijsTI\":\"0.279149\",\"prijsVDB\":\"0.279249\",\"prijsVON\":\"0.277369\",\"prijsWE\":\"0.282769\",\"prijsZG\":\"0.305769\",\"prijsZP\":\"0.277369\"},{\"datum\":\"2024-10-21
    // 09:00:00\",\"prijs\":\"0.091000\",\"prijsAA\":\"0.262960\",\"prijsAIP\":\"0.272010\",\"prijsANWB\":\"0.290160\",\"prijsBE\":\"0.262750\",\"prijsEE\":\"0.268610\",\"prijsEN\":\"0.265150\",\"prijsEVO\":\"0.290160\",\"prijsEZ\":\"0.291760\",\"prijsFR\":\"0.259960\",\"prijsGSL\":\"0.290160\",\"prijsMDE\":\"0.290160\",\"prijsNE\":\"0.261660\",\"prijsTI\":\"0.263540\",\"prijsVDB\":\"0.263640\",\"prijsVON\":\"0.261760\",\"prijsWE\":\"0.267160\",\"prijsZG\":\"0.290160\",\"prijsZP\":\"0.261760\"},{\"datum\":\"2024-10-21
    // 10:00:00\",\"prijs\":\"0.084910\",\"prijsAA\":\"0.255591\",\"prijsAIP\":\"0.264641\",\"prijsANWB\":\"0.282791\",\"prijsBE\":\"0.255381\",\"prijsEE\":\"0.261241\",\"prijsEN\":\"0.257781\",\"prijsEVO\":\"0.282791\",\"prijsEZ\":\"0.284391\",\"prijsFR\":\"0.252591\",\"prijsGSL\":\"0.282791\",\"prijsMDE\":\"0.282791\",\"prijsNE\":\"0.254291\",\"prijsTI\":\"0.256171\",\"prijsVDB\":\"0.256271\",\"prijsVON\":\"0.254391\",\"prijsWE\":\"0.259791\",\"prijsZG\":\"0.282791\",\"prijsZP\":\"0.254391\"},{\"datum\":\"2024-10-21
    // 11:00:00\",\"prijs\":\"0.080000\",\"prijsAA\":\"0.249650\",\"prijsAIP\":\"0.258700\",\"prijsANWB\":\"0.276850\",\"prijsBE\":\"0.249440\",\"prijsEE\":\"0.255300\",\"prijsEN\":\"0.251840\",\"prijsEVO\":\"0.276850\",\"prijsEZ\":\"0.278450\",\"prijsFR\":\"0.246650\",\"prijsGSL\":\"0.276850\",\"prijsMDE\":\"0.276850\",\"prijsNE\":\"0.248350\",\"prijsTI\":\"0.250230\",\"prijsVDB\":\"0.250330\",\"prijsVON\":\"0.248450\",\"prijsWE\":\"0.253850\",\"prijsZG\":\"0.276850\",\"prijsZP\":\"0.248450\"},{\"datum\":\"2024-10-21
    // 12:00:00\",\"prijs\":\"0.083150\",\"prijsAA\":\"0.253462\",\"prijsAIP\":\"0.262512\",\"prijsANWB\":\"0.280662\",\"prijsBE\":\"0.253252\",\"prijsEE\":\"0.259112\",\"prijsEN\":\"0.255652\",\"prijsEVO\":\"0.280662\",\"prijsEZ\":\"0.282262\",\"prijsFR\":\"0.250462\",\"prijsGSL\":\"0.280662\",\"prijsMDE\":\"0.280662\",\"prijsNE\":\"0.252162\",\"prijsTI\":\"0.254042\",\"prijsVDB\":\"0.254141\",\"prijsVON\":\"0.252262\",\"prijsWE\":\"0.257662\",\"prijsZG\":\"0.280662\",\"prijsZP\":\"0.252262\"},{\"datum\":\"2024-10-21
    // 13:00:00\",\"prijs\":\"0.083190\",\"prijsAA\":\"0.253510\",\"prijsAIP\":\"0.262560\",\"prijsANWB\":\"0.280710\",\"prijsBE\":\"0.253300\",\"prijsEE\":\"0.259160\",\"prijsEN\":\"0.255700\",\"prijsEVO\":\"0.280710\",\"prijsEZ\":\"0.282310\",\"prijsFR\":\"0.250510\",\"prijsGSL\":\"0.280710\",\"prijsMDE\":\"0.280710\",\"prijsNE\":\"0.252210\",\"prijsTI\":\"0.254090\",\"prijsVDB\":\"0.254190\",\"prijsVON\":\"0.252310\",\"prijsWE\":\"0.257710\",\"prijsZG\":\"0.280710\",\"prijsZP\":\"0.252310\"},{\"datum\":\"2024-10-21
    // 14:00:00\",\"prijs\":\"0.095820\",\"prijsAA\":\"0.268792\",\"prijsAIP\":\"0.277842\",\"prijsANWB\":\"0.295992\",\"prijsBE\":\"0.268582\",\"prijsEE\":\"0.274442\",\"prijsEN\":\"0.270982\",\"prijsEVO\":\"0.295992\",\"prijsEZ\":\"0.297592\",\"prijsFR\":\"0.265792\",\"prijsGSL\":\"0.295992\",\"prijsMDE\":\"0.295992\",\"prijsNE\":\"0.267492\",\"prijsTI\":\"0.269372\",\"prijsVDB\":\"0.269472\",\"prijsVON\":\"0.267592\",\"prijsWE\":\"0.272992\",\"prijsZG\":\"0.295992\",\"prijsZP\":\"0.267592\"},{\"datum\":\"2024-10-21
    // 15:00:00\",\"prijs\":\"0.116570\",\"prijsAA\":\"0.293900\",\"prijsAIP\":\"0.302950\",\"prijsANWB\":\"0.321100\",\"prijsBE\":\"0.293690\",\"prijsEE\":\"0.299550\",\"prijsEN\":\"0.296090\",\"prijsEVO\":\"0.321100\",\"prijsEZ\":\"0.322700\",\"prijsFR\":\"0.290900\",\"prijsGSL\":\"0.321100\",\"prijsMDE\":\"0.321100\",\"prijsNE\":\"0.292600\",\"prijsTI\":\"0.294480\",\"prijsVDB\":\"0.294580\",\"prijsVON\":\"0.292700\",\"prijsWE\":\"0.298100\",\"prijsZG\":\"0.321100\",\"prijsZP\":\"0.292700\"},{\"datum\":\"2024-10-21
    // 16:00:00\",\"prijs\":\"0.130040\",\"prijsAA\":\"0.310198\",\"prijsAIP\":\"0.319248\",\"prijsANWB\":\"0.337398\",\"prijsBE\":\"0.309988\",\"prijsEE\":\"0.315848\",\"prijsEN\":\"0.312388\",\"prijsEVO\":\"0.337398\",\"prijsEZ\":\"0.338998\",\"prijsFR\":\"0.307198\",\"prijsGSL\":\"0.337398\",\"prijsMDE\":\"0.337398\",\"prijsNE\":\"0.308898\",\"prijsTI\":\"0.310778\",\"prijsVDB\":\"0.310878\",\"prijsVON\":\"0.308998\",\"prijsWE\":\"0.314398\",\"prijsZG\":\"0.337398\",\"prijsZP\":\"0.308998\"},{\"datum\":\"2024-10-21
    // 17:00:00\",\"prijs\":\"0.212150\",\"prijsAA\":\"0.409552\",\"prijsAIP\":\"0.418602\",\"prijsANWB\":\"0.436752\",\"prijsBE\":\"0.409342\",\"prijsEE\":\"0.415202\",\"prijsEN\":\"0.411742\",\"prijsEVO\":\"0.436752\",\"prijsEZ\":\"0.438352\",\"prijsFR\":\"0.406552\",\"prijsGSL\":\"0.436752\",\"prijsMDE\":\"0.436752\",\"prijsNE\":\"0.408252\",\"prijsTI\":\"0.410132\",\"prijsVDB\":\"0.410232\",\"prijsVON\":\"0.408352\",\"prijsWE\":\"0.413752\",\"prijsZG\":\"0.436752\",\"prijsZP\":\"0.508352\"},{\"datum\":\"2024-10-21
    // 18:00:00\",\"prijs\":\"0.188350\",\"prijsAA\":\"0.380754\",\"prijsAIP\":\"0.389804\",\"prijsANWB\":\"0.407954\",\"prijsBE\":\"0.380544\",\"prijsEE\":\"0.386404\",\"prijsEN\":\"0.382944\",\"prijsEVO\":\"0.407954\",\"prijsEZ\":\"0.409554\",\"prijsFR\":\"0.377754\",\"prijsGSL\":\"0.407954\",\"prijsMDE\":\"0.407954\",\"prijsNE\":\"0.379454\",\"prijsTI\":\"0.381334\",\"prijsVDB\":\"0.381434\",\"prijsVON\":\"0.379554\",\"prijsWE\":\"0.384954\",\"prijsZG\":\"0.407954\",\"prijsZP\":\"0.979554\"},{\"datum\":\"2024-10-21
    // 19:00:00\",\"prijs\":\"0.128180\",\"prijsAA\":\"0.307948\",\"prijsAIP\":\"0.316998\",\"prijsANWB\":\"0.335148\",\"prijsBE\":\"0.307738\",\"prijsEE\":\"0.313598\",\"prijsEN\":\"0.310138\",\"prijsEVO\":\"0.335148\",\"prijsEZ\":\"0.336748\",\"prijsFR\":\"0.304948\",\"prijsGSL\":\"0.335148\",\"prijsMDE\":\"0.335148\",\"prijsNE\":\"0.306648\",\"prijsTI\":\"0.308528\",\"prijsVDB\":\"0.308628\",\"prijsVON\":\"0.306748\",\"prijsWE\":\"0.312148\",\"prijsZG\":\"0.335148\",\"prijsZP\":\"0.306748\"},{\"datum\":\"2024-10-21
    // 20:00:00\",\"prijs\":\"0.100560\",\"prijsAA\":\"0.274528\",\"prijsAIP\":\"0.283578\",\"prijsANWB\":\"0.301728\",\"prijsBE\":\"0.274318\",\"prijsEE\":\"0.280178\",\"prijsEN\":\"0.276718\",\"prijsEVO\":\"0.301728\",\"prijsEZ\":\"0.303328\",\"prijsFR\":\"0.271528\",\"prijsGSL\":\"0.301728\",\"prijsMDE\":\"0.301728\",\"prijsNE\":\"0.273228\",\"prijsTI\":\"0.275108\",\"prijsVDB\":\"0.275208\",\"prijsVON\":\"0.273328\",\"prijsWE\":\"0.278728\",\"prijsZG\":\"0.301728\",\"prijsZP\":\"0.273328\"},{\"datum\":\"2024-10-21
    // 21:00:00\",\"prijs\":\"0.097790\",\"prijsAA\":\"0.271176\",\"prijsAIP\":\"0.280226\",\"prijsANWB\":\"0.298376\",\"prijsBE\":\"0.270966\",\"prijsEE\":\"0.276826\",\"prijsEN\":\"0.273366\",\"prijsEVO\":\"0.298376\",\"prijsEZ\":\"0.299976\",\"prijsFR\":\"0.268176\",\"prijsGSL\":\"0.298376\",\"prijsMDE\":\"0.298376\",\"prijsNE\":\"0.269876\",\"prijsTI\":\"0.271756\",\"prijsVDB\":\"0.271856\",\"prijsVON\":\"0.269976\",\"prijsWE\":\"0.275376\",\"prijsZG\":\"0.298376\",\"prijsZP\":\"0.269976\"},{\"datum\":\"2024-10-21
    // 23:00:00\",\"prijs\":\"0.092970\",\"prijsAA\":\"0.265344\",\"prijsAIP\":\"0.274394\",\"prijsANWB\":\"0.292544\",\"prijsBE\":\"0.265134\",\"prijsEE\":\"0.270994\",\"prijsEN\":\"0.267534\",\"prijsEVO\":\"0.292544\",\"prijsEZ\":\"0.294144\",\"prijsFR\":\"0.262344\",\"prijsGSL\":\"0.292544\",\"prijsMDE\":\"0.292544\",\"prijsNE\":\"0.264044\",\"prijsTI\":\"0.265924\",\"prijsVDB\":\"0.266024\",\"prijsVON\":\"0.264144\",\"prijsWE\":\"0.269544\",\"prijsZG\":\"0.292544\",\"prijsZP\":\"0.264144\"}],\"code\":\"5\"}";

    private String todayElectricityUrl;
    private String tomorrowElectricityUrl;
    private String todayGasUrl;

    private Hashtable<LocalDate, Hashtable<Integer, Double>> ePrices = new Hashtable<>();
    private Hashtable<LocalDate, Double> eAveragePrices = new Hashtable<>();
    private Double gPrice = 0.0;

    public Prices(String apiToken) {
        this.todayElectricityUrl = "https://enever.nl/api/stroomprijs_vandaag.php?token=" + apiToken;
        this.tomorrowElectricityUrl = "https://enever.nl/api/stroomprijs_morgen.php?token=" + apiToken;
        this.todayGasUrl = "https://enever.nl/api/gasprijs_vandaag.php?token=" + apiToken;
    }

    public boolean refresh() throws EneVerException {
        var now = LocalDateTime.now();
        var hour = now.getHour();
        var date = now.toLocalDate();
        var result = false;
        if (!ePrices.containsKey(date)) {
            result = retrieveElectricityPrices(true);
        }

        if (hour > 18 && !ePrices.containsKey(date.plusDays(1))) {
            result = retrieveElectricityPrices(false);
        }

        if ((hour >= 6 && hour <= 7) || gPrice == 0.0) {
            result = retrieveGasPrice();
        }

        return result;
    }

    public void checkElectricityPrices() throws EneVerException {
        checkElectricityPrices(0);
    }

    public void checkElectricityPrices(int offsetHours) throws EneVerException {
        var dt = LocalDateTime.now().plusHours(offsetHours);
        var date = dt.toLocalDate();

        if (!ePrices.containsKey(date) || !ePrices.get(date).containsKey(dt.getHour())) {
            if (!refresh()) {
                throw new EneVerException();
            }
        }

        // Clean up
        ePrices.remove(date.minusDays(2));
    }

    public Double getElectricityPrice() {
        return getElectricityPrice(0);
    }

    public Double getElectricityPrice(int offsetHours) {
        var dt = LocalDateTime.now().plusHours(offsetHours);
        var date = dt.toLocalDate();

        try {
            checkElectricityPrices(offsetHours);
        } catch (EneVerException ex) {
            return Double.MIN_VALUE;
        }

        return ePrices.get(date).get(dt.getHour());
    }

    public Double getAverageElectricityPrice() {
        var date = LocalDate.now();

        if (!eAveragePrices.containsKey(date)) {
            return Double.MIN_VALUE;
        }

        // Clean up
        eAveragePrices.remove(date.minusDays(2));

        return eAveragePrices.get(date);
    }

    public boolean isElectricityCheap(double treshold, int numberOfHours, boolean excludeNightlyHours) {
        var dt = LocalDateTime.now();
        var date = dt.toLocalDate();

        try {
            checkElectricityPrices();
        } catch (EneVerException ex) {
            return false;
        }

        var prices = ePrices.get(date).entrySet().stream().filter(e -> !excludeNightlyHours || e.getKey() > 5)
                .filter(e -> e.getValue() < eAveragePrices.get(date) * (1 - treshold))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())).values().stream()
                .sorted((e1, e2) -> Double.compare(e2, e1)).limit(numberOfHours).collect(Collectors.toList());

        return prices.contains(ePrices.get(date).get(dt.getHour()));
    }

    public boolean isElectricityExpensive(double treshold, int numberOfHours, boolean excludeNightlyHours) {
        var dt = LocalDateTime.now();
        var date = dt.toLocalDate();

        try {
            checkElectricityPrices();
        } catch (EneVerException ex) {
            return false;
        }

        var prices = ePrices.get(date).entrySet().stream().filter(e -> !excludeNightlyHours || e.getKey() > 5)
                .filter(e -> e.getValue() > eAveragePrices.get(date) * (1 + treshold))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())).values().stream()
                .sorted((e1, e2) -> Double.compare(e2, e1)).limit(numberOfHours).collect(Collectors.toList());

        return prices.contains(ePrices.get(date).get(dt.getHour()));
    }

    public Double getGasPrice() {
        try {
            refresh();
        } catch (EneVerException ex) {
            return 0.0;
        }
        return gPrice;
    }

    private boolean retrieveElectricityPrices(boolean today) throws EneVerException {
        logger.info("Retrieving electricity prices for today? " + today);
        Payload p = null;

        if (today) {
            p = retrievePayload(todayElectricityUrl);
        } else {
            p = retrievePayload(tomorrowElectricityUrl);
        }

        if (p == null) {
            throw new EneVerException();
        }

        if (p.getStatus()) {
            var prices = new Hashtable<Integer, Double>();

            p.getPrices().forEach((price) -> {
                prices.put(price.getTime().getHour(), price.getPrijs());
            });

            var date = LocalDate.now();
            if (!today) {
                date = date.plusDays(1);
            }

            ePrices.put(date, prices);
            eAveragePrices.put(date, prices.values().stream().mapToDouble(a -> a).average().getAsDouble());
        }

        return p.getStatus();
    }

    private boolean retrieveGasPrice() throws EneVerException {
        logger.info("Retrieving gas price");

        Payload p = retrievePayload(todayGasUrl);

        if (p == null) {
            throw new EneVerException();
        }

        if (p.getStatus()) {
            gPrice = p.getPrices().get(0).getPrijs();
        }

        return p.getStatus();
    }

    private @Nullable Payload retrievePayload(String url) throws EneVerException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        @Nullable
        String dataResult = null;
        // if (url.contains("vandaag")) {
        // dataResult = testDataToday;
        // } else {
        // dataResult = testDataTomorrow;
        // }
        try {
            dataResult = HttpUtil.executeUrl("GET", url, 30000);
        } catch (IOException e) {
            throw new EneVerException();
        }

        if (dataResult == null || dataResult.trim().isEmpty()) {
            return null;
        }

        Payload payload = null;
        try {
            payload = gson.fromJson(dataResult, Payload.class);
        } catch (JsonSyntaxException ex) {
            return null;
        }

        if (payload == null) {
            return null;
        }

        if (!payload.getStatus()) {
            return null;
        }

        return payload;
    }

}