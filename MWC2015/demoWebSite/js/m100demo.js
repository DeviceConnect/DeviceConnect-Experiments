/**
 m100demo.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

// M100 IP Address.
var devIpAddr = "192.168.1.155";
// M100 Port.
var devPort = "4035";
// Update Counter.
var counter = 40;
// Update Interval (mSec).
var updateInterval = 500
// timer variable.
var timer = null;
// event control.
var isEvent = 0;
// Processing control.
var isProcess = 0;


// Setting parameter.
// Host IP Address.
var HostIpAddr = "localhost";
// Host Port.
var HostPort = "4035";
/** Local Client ID. */
var HostCurrentClientId = null;
/* var HostCurrentClientId = makeHostSessionKey(); */
/** HostAccessTonen. */
var HostAccessToken = null;
var M100IpAddr = null;
var M100Port = null;
var M100DemoCurrentClientId = null;
var M100DemoAccessToken = null;
var M100ServiceID = null;
var HealthServiceID = null;
var M100EiNormalMin = null;
var M100EiNormalMax = null;
var M100EiNormalColor = null;
var M100EiWarnupMin = null;
var M100EiWarnupMax = null;
var M100EiWarnupColor = null;
var M100EiFitnessMin = null;
var M100EiFitnessMax = null;
var M100EiFitnessColor = null;
var M100EiCardioMin = null;
var M100EiCardioMax = null;
var M100EiCardioColor = null;
var M100EiTrainingMin = null;
var M100EiTrainingMax = null;
var M100EiTrainingColor = null;

var isCookie = 0;
var isDrawable = 0;

var width = 258;
var height = 135;
var PosX = 27;
var PosY = 90;

/**
 * Demo page initialize.
 */
function demoinit() {
    readParam();

    // Show Host IP address for page.
    $('#host').html("Host connecting:" + HostIpAddr);

    // Show Host accessToken for page.
    $('#hosttoken').html("Host accessToken:" + HostAccessToken);

    // Show M100 IP address for page.
    $('#m100').html("M100 connecting:" + M100IpAddr);

    // Show M100 accessToken for page.
    $('#m100token').html("M100 accessToken:" + M100DemoAccessToken);

    showM100Demo("");
}

/** 
 * M100 Demonstration
 */
function showM100Demo(serviceId) {
    initAll();
    setTitle("Demo Top");

    var btnStr = "";
        btnStr += '<center><input data-icon="grid" data-inline="true" data-mini="true" onclick="javascript:DemoAuthorization();" type="button" value="accessToken" /></center>';
    reloadHeader(btnStr);

    var str = "";
        str += '<li><a href="javascript:showHRDeviceSetup(\'' + serviceId + '\');" >Device Setup</a></li>';
        str += '<li><a href="javascript:showHRParamSetup(\'' + serviceId + '\');" >Parameter Setup</a></li>';
        str += '<li><a href="javascript:showHRValue(\'' + serviceId + '\');" >Show Heart Rate</a></li>';
    reloadList(str);
}

/** 
 * Read Parameter
 */
function readParam() {

    // Check cookie enable.
    document.cookie = "UseCookie=1";
    if (document.cookie.length == 0) {
        // Cookie unavailable.
        // Setting is not saved.
        // At the time of the first call, set the default value.
        isCookie = 0;

        if (M100IpAddr          == null) M100IpAddr          = devIpAddr;
        if (M100Port            == null) M100Port            = devPort;
        if (M100EiNormalMin     == null) M100EiNormalMin     = "40";
        if (M100EiNormalMax     == null) M100EiNormalMax     = "90";
        if (M100EiNormalColor   == null) M100EiNormalColor   = "#00fdff";
        if (M100EiWarnupMin     == null) M100EiWarnupMin     = "91";
        if (M100EiWarnupMax     == null) M100EiWarnupMax     = "110";
        if (M100EiWarnupColor   == null) M100EiWarnupColor   = "#00f900";
        if (M100EiFitnessMin    == null) M100EiFitnessMin    = "111";
        if (M100EiFitnessMax    == null) M100EiFitnessMax    = "130";
        if (M100EiFitnessColor  == null) M100EiFitnessColor  = "#ff9300";
        if (M100EiCardioMin     == null) M100EiCardioMin     = "131";
        if (M100EiCardioMax     == null) M100EiCardioMax     = "150";
        if (M100EiCardioColor   == null) M100EiCardioColor   = "#ff40ff";
        if (M100EiTrainingMin   == null) M100EiTrainingMin   = "151";
        if (M100EiTrainingMax   == null) M100EiTrainingMax   = "165";
        if (M100EiTrainingColor == null) M100EiTrainingColor = "#ff2600";
    } else {
        // Cookie available.
        isCookie = 1;

　　　   M100IpAddr = getCookie('M100IpAddr');
        if (M100IpAddr == null) M100IpAddr = devIpAddr;

        var token = 'M100DemoAccessToken' + M100IpAddr;
        M100DemoAccessToken = getCookie(token);

　　　   M100Port = getCookie('M100Port');
        if (M100Port == null) M100Port = devPort;

        M100EiNormalMin = getCookie("M100EiNormalMin");
        if (M100EiNormalMin == null) M100EiNormalMin = "40";

        M100EiNormalMax = getCookie("M100EiNormalMax");
        if (M100EiNormalMax == null) M100EiNormalMax = "90";

        M100EiNormalColor = getCookie("M100EiNormalColor");
        if (M100EiNormalColor == null) M100EiNormalColor = "#00fdff";

        M100EiWarnupMin = getCookie("M100EiWarnupMin");
        if (M100EiWarnupMin == null) M100EiWarnupMin = "91";

        M100EiWarnupMax = getCookie("M100EiWarnupMax");
        if (M100EiWarnupMax == null) M100EiWarnupMax = "110";

        M100EiWarnupColor = getCookie("M100EiWarnupColor");
        if (M100EiWarnupColor == null) M100EiWarnupColor = "#00f900";

        M100EiFitnessMin = getCookie("M100EiFitnessMin");
        if (M100EiFitnessMin == null) M100EiFitnessMin = "111";

        M100EiFitnessMax = getCookie("M100EiFitnessMax");
        if (M100EiFitnessMax == null) M100EiFitnessMax = "130";

        M100EiFitnessColor = getCookie("M100EiFitnessColor");
        if (M100EiFitnessColor == null) M100EiFitnessColor = "#ff9300";

        M100EiCardioMin = getCookie("M100EiCardioMin");
        if (M100EiCardioMin == null) M100EiCardioMin = "131";

        M100EiCardioMax = getCookie("M100EiCardioMax");
        if (M100EiCardioMax == null) M100EiCardioMax = "150";

        M100EiCardioColor = getCookie("M100EiCardioColor");
        if (M100EiCardioColor == null) M100EiCardioColor = "#ff40ff";

        M100EiTrainingMin = getCookie("M100EiTrainingMin");
        if (M100EiTrainingMin == null) M100EiTrainingMin = "151";

        M100EiTrainingMax = getCookie("M100EiTrainingMax");
        if (M100EiTrainingMax == null) M100EiTrainingMax = "165";

        M100EiTrainingColor = getCookie("M100EiTrainingColor");
        if (M100EiTrainingColor == null) M100EiTrainingColor = "#ff2600";
    }
}

/**
 * Show HR Device Setup
 *
 * @param {String}serviceId Service ID
 */
function showHRDeviceSetup(serviceId){
    initAll();
    setTitle("Device Setup");

    var sessionKey = currentClientId;

    var btnStr = getBackButton('Demo Top', 'doHRDeviceBack', serviceId, sessionKey);
    reloadHeader(btnStr);
    reloadFooter(btnStr);
    
    var str = "";
    str += '<li><a href="javascript:showDeviceHostSetup(\'' + serviceId + '\');" >Host Setup</a></li>';
    str += '<li><a href="javascript:showDeviceM100Setup(\'' + serviceId + '\');" >M100 Setup</a></li>';
    str += '<li><a href="javascript:doDeviceHRSetup(\'' + serviceId + '\');" >Heart Rate Device Setup</a></li>';
    reloadList(str);
}

/**
 * Show Device Host Setup
 *
 * @param {String}serviceId Service ID
 */
function showDeviceHostSetup(serviceId){
    initAll();
    setTitle("Host Setup");

    var sessionKey = HostCurrentClientId;

    var btnStr = getBackButton('Device Setup Top', 'doHostSetupBack', serviceId, sessionKey);
    reloadHeader(btnStr);
    reloadFooter(btnStr);
    
    var str = "";
    str += '<form name="HostParamForm">';
    str += 'Host IP Address<br>';
    str += '<input type="text" id="HostIpAddr" width="100%" value="' + HostIpAddr + '"/>';
    str += 'Host Port<br>';
    str += '<input type="text" id="HostPort" width="100%" value="' + HostPort + '"/>';
    str += '<input type="button" name="setButton" id="setButton" value="Set" onclick="doSetHostParameter();"/>';
    str += '</form>';
    reloadContent(str);
}

/**
 * Show Device M100 Setup
 *
 * @param {String}serviceId Service ID
 */
function showDeviceM100Setup(serviceId){
    initAll();
    setTitle("M100 Setup");

    var sessionKey = M100DemoCurrentClientId;

    var btnStr = getBackButton('Device Setup Top', 'doM100SetupBack', serviceId, sessionKey);
    reloadHeader(btnStr);
    reloadFooter(btnStr);
    
    var str = "";
    str += '<form name="M100ParamForm">';
    str += 'M100 IP Address<br>';
    str += '<input type="text" id="M100IpAddr" width="100%" value="' + M100IpAddr + '"/>';
    str += 'M100 Port<br>';
    str += '<input type="text" id="M100Port" width="100%" value="' + M100Port + '"/>';
    str += '<input type="button" name="setButton" id="setButton" value="Set" onclick="doSetM100Parameter();"/>';
    str += '</form>';
    reloadContent(str);
}

/**
 * Device HR Setup
 *
 * @param {String}serviceId Service ID
 */
function doDeviceHRSetup(serviceId){
    if (DEBUG) console.log("ip : " + HostIpAddr);
    dConnect.setHost(HostIpAddr);
    var builder = new dConnect.URIBuilder();
    builder.setProfile("system");
    var uri = builder.build();

    if(DEBUG) console.log("Uri:"+uri)

    dConnect.get(uri, null, null, function(json) {
        if (DEBUG) console.log("Response: ", json);
        for (var i = 0; i < json.plugins.length; i++) {
            if (DEBUG) console.log("id : " + json.plugins[i].id + " name : " + json.plugins[i].name);
            if (json.plugins[i].name == "HeartRate(BLE)デバイスプラグイン") {
                launchDevicePlugin(json.plugins[i].id);
                break;
            }
            if (i == json.plugins.length) {
                alert("Health Profile not found.");
                return;
            }
        }
    });
}

/**
 * Show HR Parameter Setup
 *
 * @param {String}serviceId Service ID
 */
function showHRParamSetup(serviceId){
    initAll();
    setTitle("Parameter Setup");

    var sessionKey = currentClientId;

    var btnStr = getBackButton('Demo Top', 'doHRParamBack', serviceId, sessionKey);
    reloadHeader(btnStr);
    reloadFooter(btnStr);

    var str = "";
    str += '<form name="ParamForm">';

    str += '<div data-role="rangeslider" id="EiNormal">';
    str += '  <label for="EiNormalMin">Exercise intensity (Normal)：</label>';
    str += '  <input id="EiNormalMin" name="EiNormalMin" type="range" min="30" max="130" step="1" value="' + M100EiNormalMin + '"/>';
    str += '  <label for="EiNormalMax">Exercise intensity (Normal) MAX：</label>';
    str += '  <input id="EiNormalMax" name="EiNormalMax" type="range" min="30" max="130" step="1" value="' + M100EiNormalMax + '"/>';
    str += '</div>';
    str += '<input id="EiNormalColor" name="EiNormalColor" type="color" value="' + M100EiNormalColor + '"/>';

    str += '<div data-role="rangeslider" id="EiWarnup">';
    str += '  <label for="EiWarnupMin">Exercise intensity (Warm up)：</label>';
    str += '  <input id="EiWarnupMin" name="EiWarnupMin" type="range" min="40" max="140" step="1" value="' + M100EiWarnupMin + '"/>';
    str += '  <label for="EiWarnupMax">Exercise intensity (Warm up) MAX：</label>';
    str += '  <input id="EiWarnupMax" name="EiWarnupMax" type="range" min="40" max="140" step="1" value="' + M100EiWarnupMax + '"/>';
    str += '</div>';
    str += '<input id="EiWarnupColor" name="EiWarnupColor" type="color" value="' + M100EiWarnupColor + '"/>';

    str += '<div data-role="rangeslider" id="EiFitness">';
    str += '  <label for="EiFitnessMin">Exercise intensity (Fitness)：</label>';
    str += '  <input id="EiFitnessMin" name="EiFitnessMin" type="range" min="60" max="160" step="1" value="' + M100EiFitnessMin + '"/>';
    str += '  <label for="EiFitnessMax">Exercise intensity (Fitness) MAX：</label>';
    str += '  <input id="EiFitnessMax" name="EiFitnessMax" type="range" min="60" max="160" step="1" value="' + M100EiFitnessMax + '"/>';
    str += '</div>';
    str += '<input id="EiFitnessColor" name="EiFitnessColor" type="color" value="' + M100EiFitnessColor + '"/>';

    str += '<div data-role="rangeslider" id="EiCardio">';
    str += '  <label for="EiCardioMin">Exercise intensity (Cardio)：</label>';
    str += '  <input id="EiCardioMin" name="EiCardioMin" type="range" min="80" max="180" step="1" value="' + M100EiCardioMin + '"/>';
    str += '  <label for="EiCardioMax">Exercise intensity (Cardio) MAX：</label>';
    str += '  <input id="EiCardioMax" name="EiCardioMax" type="range" min="80" max="180" step="1" value="' + M100EiCardioMax + '"/>';
    str += '</div>';
    str += '<input id="EiCardioColor" name="EiCardioColor" type="color" value="' + M100EiCardioColor + '"/>';

    str += '<div data-role="rangeslider" id="EiTraining">';
    str += '  <label for="EiTrainingMin">Exercise intensity (Training)：</label>';
    str += '  <input id="EiTrainingMin" name="EiTrainingMin" type="range" min="95" max="195" step="1" value="' + M100EiTrainingMin + '"/>';
    str += '  <label for="EiTrainingMax">Exercise intensity (Training) MAX：</label>';
    str += '  <input id="EiTrainingMax" name="EiTrainingMax" type="range" min="95" max="195" step="1" value="' + M100EiTrainingMax + '"/>';
    str += '</div>';
    str += '<input id="EiTrainingColor" name="EiTrainingColor" type="color" value="' + M100EiTrainingColor + '"/>';

    str += '<input type="button" name="defaultButton" id="defaultButton" value="Default" onclick="doSetDefaultParameter();"/>';
    str += '<input type="button" name="setButton" id="setButton" value="Set" onclick="doSetParameter();"/>';
    str += '</form>';
    reloadContent(str);
}

/**
 * Show HR Value
 *
 * @param {String}serviceId Service ID
 */
function showHRValue(serviceId){
    initAll();
    setTitle("Show Heart Rate");

    var sessionKey = currentClientId;

    var btnStr = getBackButton('Demo Top', 'doHRValueBack', serviceId, sessionKey);
    reloadHeader(btnStr);
    reloadFooter(btnStr);
    
    var str = "";
    str += '<form name="upForm">';
    str += '<input type="button" name="clearButton" id="clearButton" value="Clear Screen" onclick="doClearWindow();"/>';
    str += '<input type="button" name="eventStartButton" id="eventStartButton" value="Start (Event)" onclick="doStartHeartRate(0);"/>';
    str += '<input type="button" name="eventStopButton" id="eventStopButton" value="Stop (Event)" onclick="stopInterval(0);"/>';
    str += '<input type="button" name="incStartButton" id="incStartButton" value="Start (Incremental)" onclick="doStartHeartRate(1);"/>';
    str += '<input type="button" name="incStopButton" id="incStopButton" value="Stop (Incremental)" onclick="stopInterval(1);"/>';
    str += 'Heart Rate<br>';
    str += '<center><canvas id="canvas" width="' + width + '" height="' + height + '" ></canvas></center>';
    str += '</form>';
    reloadContent(str);
}

/**
 * Back button
 *
 * serviceId Service ID
 * sessionKey Session Key
 */
function doHostSetupBack(serviceId, sessionKey){
    showHRDeviceSetup(serviceId);
}

/**
 * Back button
 *
 * serviceId Service ID
 * sessionKey Session Key
 */
function doM100SetupBack(serviceId, sessionKey){
    showHRDeviceSetup(serviceId);
}

/**
 * Back button
 *
 * serviceId Service ID
 * sessionKey Session Key
 */
function doHRDeviceBack(serviceId, sessionKey){
    showM100Demo(serviceId);
}

/**
 * Back button
 *
 * serviceId Service ID
 * sessionKey Session Key
 */
function doHRParamBack(serviceId, sessionKey){
    showM100Demo(serviceId);
}

/**
 * Back button
 *
 * serviceId Service ID
 * sessionKey Session Key
 */
function doHRValueBack(serviceId, sessionKey){
    stopInterval();
    showM100Demo(serviceId);
}

/**
 * Heart Rate Event Regist
 */
function doHeartRateRegist(serviceId, sessionKey) {
    if (DEBUG) console.log("ip : " + HostIpAddr);
    dConnect.setHost(HostIpAddr);
    var builder = new dConnect.URIBuilder();
    builder.setProfile("health");
    builder.setAttribute("heartrate");
    builder.setServiceId(serviceId);
    builder.setAccessToken(HostAccessToken);
    builder.setSessionKey(sessionKey);
    var uri = builder.build();
    if (DEBUG) console.log("Uri: " + uri);

    if (dConnect.isConnectedWebSocket()) {
        dConnect.disconnectWebSocket();
    }
    dConnect.connectWebSocket(sessionKey, function(errorCode, errorMessage) {});

    dConnect.addEventListener(uri,function(message) {
        // Parse event message.
        if(DEBUG) console.log("Event-Message:"+message)
        var json = JSON.parse(message);
        if (json.heartRate && isDrawable == 1) {
            /* Draw canvas. */
            canvasDraw(json.heartRate);
        }
    }, null, function(errorCode, errorMessage){
        alert(errorMessage);
    });
}

/**
 * Heart Rate Event Unregist
 */
function doHeartRateUnregist(serviceId, sessionKey) {
    if (DEBUG) console.log("ip : " + HostIpAddr);
    dConnect.setHost(HostIpAddr);
    var builder = new dConnect.URIBuilder();
    builder.setProfile("health");
    builder.setAttribute("heartrate");
    builder.setServiceId(serviceId);
    builder.setAccessToken(HostAccessToken);
    builder.setSessionKey(sessionKey);
    var uri = builder.build();
    if (DEBUG) console.log("Uri : "+uri);

    dConnect.removeEventListener(uri, null, function(errorCode, errorMessage){
        alert(errorMessage);
    });
    isDrawable = 0;
}

/**
 * Set Host Parameter
 */
function doSetHostParameter() {
    // add cookie
    if (DEBUG) console.log(document.HostParamForm.elements[0].value);
    HostIpAddr = document.HostParamForm.elements[0].value;
    document.cookie = "HostIpAddr=" + encodeURIComponent(HostIpAddr);
    if (DEBUG) console.log(document.HostParamForm.elements[1].value);
    HostPort = document.HostParamForm.elements[1].value;
    document.cookie = "HostPort=" + encodeURIComponent(HostPort);
    if (DEBUG) console.log("length:"+document.cookie.length);
    if (DEBUG) console.log("cookie:"+document.cookie);
    HostDemoAuthorization(false);
/*    makeHostSessionKey();*/
}

/**
 * Set M100 Parameter
 */
function doSetM100Parameter() {
    // add cookie
    if (DEBUG) console.log(document.M100ParamForm.elements[0].value);
    M100IpAddr = document.M100ParamForm.elements[0].value;
    document.cookie = "M100IpAddr=" + encodeURIComponent(M100IpAddr);
    if (DEBUG) console.log(document.M100ParamForm.elements[1].value);
    M100Port = document.M100ParamForm.elements[1].value;
    document.cookie = "M100Port=" + encodeURIComponent(M100Port);
    if (DEBUG) console.log("length:"+document.cookie.length);
    if (DEBUG) console.log("cookie:"+document.cookie);
    M100DemoAuthorization();
    $('#m100').html("M100 connecting:" + M100IpAddr);
}

/**
 * Set Default Parameter
 */
function doSetDefaultParameter() {
    // Set default value
    document.ParamForm.elements[0].value = "40";
    document.ParamForm.elements[1].value = "90";
    document.ParamForm.elements[2].value = "#00fdff";
    document.ParamForm.elements[3].value = "91";
    document.ParamForm.elements[4].value = "110";
    document.ParamForm.elements[5].value = "#00f900";
    document.ParamForm.elements[6].value = "111";
    document.ParamForm.elements[7].value = "130";
    document.ParamForm.elements[8].value = "#ff9300";
    document.ParamForm.elements[9].value = "131";
    document.ParamForm.elements[10].value = "150";
    document.ParamForm.elements[11].value = "#ff40ff";
    document.ParamForm.elements[12].value = "151";
    document.ParamForm.elements[13].value = "165";
    document.ParamForm.elements[14].value = "#ff2600";
    reloadContent();
    alert("Set Default.");
}

/**
 * Set Parameter
 */
function doSetParameter() {
    // Check range.
    var errMsg = "Please check parameter.\n";
    var err = "";
    if (document.ParamForm.EiWarnupMin.value <= document.ParamForm.EiNormalMax.value) {
        err += "  Normal(Max) - Warm up(Min)\n";
    }
    if (document.ParamForm.EiFitnessMin.value <= document.ParamForm.EiWarnupMax.value) {
        err += "  Warm up(Max) - Fitness(Min)\n";
    }
    if (document.ParamForm.EiCardioMin.value <= document.ParamForm.EiFitnessMax.value) {
        err += "  Fitness(Max) - Cardio(Min)\n";
    }
    if (document.ParamForm.EiTrainingMin.value <= document.ParamForm.EiCardioMax.value) {
        err += "  Cardio(Max) - Training(Min)\n";
    }
    if (err != "") {
        alert(errMsg + err);
        return;
    }

    // add cookie
    M100EiNormalMin = document.ParamForm.elements[0].value;
    document.cookie = "M100EiNormalMin=" + encodeURIComponent(M100EiNormalMin);
    M100EiNormalMax = document.ParamForm.elements[1].value;
    document.cookie = "M100EiNormalMax=" + encodeURIComponent(M100EiNormalMax);
    M100EiNormalColor = document.ParamForm.elements[2].value;
    document.cookie = "M100EiNormalColor=" + encodeURIComponent(M100EiNormalColor);
    M100EiWarnupMin = document.ParamForm.elements[3].value;
    document.cookie = "M100EiWarnupMin=" + encodeURIComponent(M100EiWarnupMin);
    M100EiWarnupMax = document.ParamForm.elements[4].value;
    document.cookie = "M100EiWarnupMax=" + encodeURIComponent(M100EiWarnupMax);
    M100EiWarnupColor = document.ParamForm.elements[5].value;
    document.cookie = "M100EiWarnupColor=" + encodeURIComponent(M100EiWarnupColor);
    M100EiFitnessMin = document.ParamForm.elements[6].value;
    document.cookie = "M100EiFitnessMin=" + encodeURIComponent(M100EiFitnessMin);
    M100EiFitnessMax = document.ParamForm.elements[7].value;
    document.cookie = "M100EiFitnessMax=" + encodeURIComponent(M100EiFitnessMax);
    M100EiFitnessColor = document.ParamForm.elements[8].value;
    document.cookie = "M100EiFitnessColor=" + encodeURIComponent(M100EiFitnessColor);
    M100EiCardioMin = document.ParamForm.elements[9].value;
    document.cookie = "M100EiCardioMin=" + encodeURIComponent(M100EiCardioMin);
    M100EiCardioMax = document.ParamForm.elements[10].value;
    document.cookie = "M100EiCardioMax=" + encodeURIComponent(M100EiCardioMax);
    M100EiCardioColor = document.ParamForm.elements[11].value;
    document.cookie = "M100EiCardioColor=" + encodeURIComponent(M100EiCardioColor);
    M100EiTrainingMin = document.ParamForm.elements[12].value;
    document.cookie = "M100EiTrainingMin=" + encodeURIComponent(M100EiTrainingMin);
    M100EiTrainingMax = document.ParamForm.elements[13].value;
    document.cookie = "M100EiTrainingMax=" + encodeURIComponent(M100EiTrainingMax);
    M100EiTrainingColor = document.ParamForm.elements[14].value;
    document.cookie = "M100EiTrainingColor=" + encodeURIComponent(M100EiTrainingColor);

    alert("Set Success.");
}

/**
 * Clear Window
 */
function doClearWindow() {
    var canvas =  document.getElementById('canvas');
    var context = canvas.getContext('2d');

    context.beginPath();
    context.clearRect(0, 0, width, height);

    dConnect.setHost(M100IpAddr);
    var builder = new dConnect.URIBuilder();
    builder.setProfile("canvas");
    builder.setAttribute("drawimage");
    builder.setServiceId(M100ServiceID);
    builder.setAccessToken(M100DemoAccessToken);
    var uri = builder.build();
    
    if(DEBUG) console.log("Uri:"+uri)
    
    dConnect.delete(uri, null, null, function(json) {
        if (DEBUG) console.log("Response: ", json);
    }, function(errorCode, errorMessage) {
        showError("DELETE canvas/drawimage", errorCode, errorMessage);
    });

}

/**
 * Start Heart Rate
 */
function doStartHeartRate(flag) {
    if (DEBUG) console.log("isProcess:" + isProcess + " flag:" + flag + " timer:" + timer + " isEvent:" + isEvent);
    if ((isProcess == 0 && flag == 1 && timer == null) ||
        (isProcess == 0 && flag == 0 && isEvent == 0)    ) {
        isProcess = 1;
        if (flag == 1) {
            searchM100(flag);
        } else {
            searchHealth(flag);
        }
    } else {
        alert("Already started.");
    }
}

/**
 * Send image data
 */
function sendImageBinary(blob) {
    var formData = new FormData();
    
    formData.append('serviceId', M100ServiceID);
    formData.append('accessToken', M100DemoAccessToken);
    formData.append('mode', 'scales');
    formData.append('filename', 'heartrate.png');
    formData.append('mimeType', 'image/png');
    formData.append('data', blob);

    $.ajax({
        type:'POST',
        url:'http://' + M100IpAddr + ':' + M100Port + '/gotapi/canvas/drawimage',
        data:formData,
        contentType:false,
        processData:false,
        success:function(data, dataType) {
            console.log('success');
        },
        error:function(XMLHttpRequest, textStatus, errorThrown) {
            console.log('error');
        }
    });
}

/**
 * Canvas draw
 */
function canvasDraw(heartRate) {
    var canvas =  document.getElementById('canvas');
    var context = canvas.getContext('2d');
    var orderText = "bpm";
    var heartText = " ♥";

    context.beginPath();
    context.clearRect(0, 0, width, height);

    // Set background color.
    if (heartRate >= M100EiNormalMin && heartRate <= M100EiNormalMax) {
        context.fillStyle = M100EiNormalColor;
    } else if (heartRate >= M100EiWarnupMin && heartRate <= M100EiWarnupMax) {
        context.fillStyle = M100EiWarnupColor;
    } else if (heartRate >= M100EiFitnessMin && heartRate <= M100EiFitnessMax) {
        context.fillStyle = M100EiFitnessColor;
    } else if (heartRate >= M100EiCardioMin && heartRate <= M100EiCardioMax) {
        context.fillStyle = M100EiCardioColor;
    } else if (heartRate >= M100EiTrainingMin && heartRate <= M100EiTrainingMax) {
        context.fillStyle = M100EiTrainingColor;
    } else {
        context.fillStyle = M100EiTrainingColor;
        context.fillStyle = '#0433ff'; 
    }

    // Canvas clear.
    context.fillRect(0, 0, width, height);
    context.stroke();
    context.restore();
    context.save();

    // Draw heart rate.
    context.beginPath();
    context.font = "58pt Arial";
    context.fillStyle = 'rgb(0, 0, 0)'; 
    if (heartRate < 100) {
        context.fillText(" " + heartRate, PosX, PosY);
    } else {
        context.fillText(heartRate, PosX, PosY);
    }
    context.font = "21pt Arial";
    context.fillText(orderText, PosX + 135, PosY);
    context.fillStyle = 'rgb(255, 0, 0)'; 
    context.fillText(heartText, PosX + 186, PosY);
    context.restore();
    context.save();

    // Get image binary data.
    canvas = $('#canvas')[0].toDataURL();
    var base64Data = canvas.split(',')[1],
    data = window.atob(base64Data),
    buff = new ArrayBuffer(data.length),
    arr = new Uint8Array(buff),
    blob, i, dataLen;
    
    // Create blob.
    for (i = 0, dataLen = data.length; i < dataLen; i++) {
        arr[i] = data.charCodeAt(i);
    }
    blob = new Blob([arr], {type: 'image/png'});
    sendImageBinary(blob);
}

/**
 * Get Local OAuth accesstoken.
 */
function DemoAuthorization(){
    HostDemoAuthorization(true);
}
/**
 * Get Local OAuth accesstoken.
 */
function M100DemoAuthorization(){
    if (DEBUG) console.log("ip : " + M100IpAddr);
    $('#m100').html("M100 connecting:" + M100IpAddr);
    dConnect.setHost(M100IpAddr);
    var scopes = Array("servicediscovery", "battery", "connect", "deviceorientation", "file_descriptor", "file", "media_player",
                    "mediastream_recording", "notification", "phone", "proximity", "settings", "vibration", "light",
                    "remote_controller", "drive_controller", "mhealth", "sphero", "dice", "temperature","camera", "canvas", "health");
        dConnect.authorization('http://www.deviceconnect.org/demo/', scopes, 'サンプル',
            function(clientId, clientSecret, newAccessToken) {
                // Client ID
                M100DemoCurrentClientId = clientId;
                
                // accessToken
                M100DemoAccessToken = newAccessToken;
                $('#m100token').html("M100 accessToken:" + M100DemoAccessToken);
                
                // debug log
                console.log("M100DemoCurrentClientId:" + M100DemoCurrentClientId);
                console.log("M100DemoAccessToken:" + M100DemoAccessToken);

                // add cookie
                document.cookie = 'M100DemoAccessToken' + M100IpAddr + '=' + encodeURIComponent(M100DemoAccessToken);
        },
        function(errorCode, errorMessage) {
              alert("Failed to get accessToken.");
        });
}

/**
 * Get Local OAuth accesstoken.
 */
function HostDemoAuthorization(flag){
    if (DEBUG) console.log("ip : " + HostIpAddr);
    $('#host').html("Host connecting:" + HostIpAddr);
    dConnect.setHost(HostIpAddr);
    var scopes = Array("servicediscovery", "battery", "connect", "deviceorientation", "file_descriptor", "file", "media_player",
                    "mediastream_recording", "notification", "phone", "proximity", "settings", "vibration", "light",
                    "remote_controller", "drive_controller", "mhealth", "sphero", "dice", "temperature","camera", "canvas", "health");
        dConnect.authorization('http://www.deviceconnect.org/demo/', scopes, 'サンプル',
            function(clientId, clientSecret, newAccessToken) {
                // Client ID
                HostCurrentClientId = clientId;
                
                // accessToken
                HostAccessToken = newAccessToken;
                $('#hosttoken').html("Host accessToken:" + HostAccessToken);
                
                // debug log
                console.log("HostCurrentClientId:" + HostCurrentClientId);
                console.log("HostAccessToken:" + HostAccessToken);

                // add cookie
                document.cookie = 'HostAccessToken' + HostIpAddr + '=' + encodeURIComponent(HostAccessToken);

                if (flag == true) {
                    M100DemoAuthorization();
                }
        },
        function(errorCode, errorMessage) {
            alert("Failed to get accessToken.");
        });
}

/**
 * Search of M100.
 */
function searchM100(flag) {
    if (DEBUG) console.log("ip : " + M100IpAddr);
    dConnect.setHost(M100IpAddr);
    dConnect.discoverDevices(M100DemoAccessToken, function(obj){
        if(DEBUG) console.log("response: ", obj);

        for (var i = 0; i < obj.services.length; i++) {
            if(obj.services[i].name == "Host") {
                M100ServiceID = obj.services[i].id;
                if(DEBUG) console.log('M100ServiceID:' + M100ServiceID);
                if(DEBUG) console.log('M100DemoCurrentClientId:' + M100DemoCurrentClientId);

                isDrawable = 1;
                if (flag == 0) {
/*
                    if (HostCurrentClientId == null) {
                        makeHostSessionKey();
                    }
*/
                    doHeartRateRegist(HealthServiceID, HostCurrentClientId);
                    isEvent = 1;
                } else {
                    timer = setInterval('debugDrawProcess()', updateInterval);
                }
                break;
            }
            if (i == obj.services.length) {
                alert("Host Profile not found.");
                isProcess = 0;
                return;
            }
        }
    }, function(readyState, status) {
        alert("[SeharchM100] readyState: " + readyState + " status: " + status);
        isProcess = 0;
    });
}

/**
 * Search of Health.
 */
function searchHealth(flag) {
    if (DEBUG) console.log("ip : " + HostIpAddr);
    dConnect.setHost(HostIpAddr);
    dConnect.discoverDevices(HostAccessToken, function(obj){
        if(DEBUG) console.log("response: ", obj);

        for (var i = 0; i < obj.services.length; i++) {
            if (DEBUG) console.log("services: ", obj.services[i].name);
            if ( (obj.services[i].name == "MIO GLOBAL")
              || (obj.services[i].name.indexOf('PS-100') === 0)
              || (obj.services[i].name.indexOf('PS-500') === 0)
              || (obj.services[i].name.indexOf('Polar H7') === 0)
              || (obj.services[i].name.indexOf('Wahoo HRM') === 0) ) {
                HealthServiceID = obj.services[i].id;
                if(DEBUG) console.log('HealthServiceID:' + HealthServiceID);
                break;
            }
        }
        if (i == obj.services.length) {
            alert("Health Profile not found.");
            isProcess = 0;
            return;
        }
        searchM100(flag);
    }, function(readyState, status) {
        alert("[SeharchHealth] readyState: " + readyState + " status: " + status);
        isProcess = 0;
    });
}

/**
 * Debug draw process
 */
function debugDrawProcess() {
    canvasDraw(counter)
    if (++counter > 160) {
        counter = 40;
    }
}

/**
 * Stop interval
 */
function stopInterval(flag) {
    if (flag == 0) {
        doHeartRateUnregist(HealthServiceID, HostCurrentClientId);
        isEvent = 0;
    } else {
        clearInterval(timer);
        timer = null;
    }
    isProcess = 0;
}

/**
 * Make Host sessionKey
 */
function makeHostSessionKey() {
    var datetime = new Date(); 
    var year = datetime.getFullYear();
    var month = datetime.getMonth()+1;
    var day = datetime.getDate();
    var hour = datetime.getHours();
    var minute = datetime.getMinutes();
    var second = datetime.getSeconds();

    HostCurrentClientId = "demo" + year + month + day + hour + minute + second;
    if(DEBUG) console.log("HostCurrentClientId: ", HostCurrentClientId);
}
