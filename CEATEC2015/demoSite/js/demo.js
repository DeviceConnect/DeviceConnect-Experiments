/**
 demo.ja
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function () {
  'use strict';

  var applicationName = 'WalkThroughデモ';

  var scopes = [
    'servicediscovery',
    'serviceinformation',
    'system',
    'file',
    'omnidirectional_image',
    'walkthrough'
  ];

  var roiData = [
    {
      'name' : 'Color Run Savannagh',
      'thumbnail' : 'assets/picture/Color_Run_Savannagh.jpg',
      'source' : 'assets/picture/Color_Run_Savannagh.jpg',
      'fov' : 60,
      'yaw' : 0,
      'roll' : 0,
      'pitch' : 0
    },
    {
      'name' : 'パラグライダー',
      'thumbnail' : 'assets/picture/p_original14.jpg',
      'source' : 'assets/picture/p_original14.jpg',
      'fov' : 65,
      'yaw' : 0,
      'roll' : 0,
      'pitch' : 0
    },
    {
      'name' : 'フライト',
      'thumbnail' : 'assets/picture/flight.JPG',
      'source' : 'assets/picture/flight.JPG',
      'fov' : 65,
      'yaw' : 0,
      'roll' : 0,
      'pitch' : 0
    },
    {
      'name' : '海',
      'thumbnail' : 'assets/picture/sea.JPG',
      'source' : 'assets/picture/sea.JPG',
      'fov' : 65,
      'yaw' : 0,
      'roll' : 0,
      'pitch' : 0
    },
  ];

  var walkthroughData = [
    {
      'name' : '水族館',
      'thumbnail' : 'assets/walk/route4/R0001.jpg',
      'source' : 'org.deviceconnect.android.manager/demoWebSite/assets/walk/route4',
      'yaw': 0,
      'roll': 0,
      'pitch': 0
    },
  ];

  var roiParam = {
    'width': 480,
    'height': 270,
  }

  var walkthroughParam = {
    'width': 480,
    'height': 270,
    'fps': 10,
    'fov': 65
  }

  var KEY_ENABLED_DEBUG_VIEW = 'enabled-debug-view';
  var KEY_ENABLED_DEBUG_AUTOPLAY = 'enabled-debug-autoplay';
  
  var debug = (function() {
    
    var demoSettings;
    
    return {
      
      setSettings: function(settings) {
        demoSettings = settings;
      },
      
      isEnabledDebugView: function() {
        return demoSettings.isEnabledDebugView;
      },

      isEnabledAutoPlay: function() {
        return demoSettings.isEnabledDebugAutoplay;
      },
      
      showDebugView: function(isShown) {
        if (isShown) {
          $('#debug-walk-params').show();
          $('#debug-roi-params').show();
        } else {
          $('#debug-walk-params').hide();
          $('#debug-roi-params').hide();
        }
      },
      
      setWalkThroughParams: function(walk) {
        var str = '';
        str += 'ROI: ' + walkthroughParam.width + ' x ' + walkthroughParam.height + '<br>';
        $('#debug-walk-params').html(str);
      },
      
      setRoiParams: function(roi) {
        var str = '';
        str += 'FOV:' + roi.fov + '<br>';
        str += 'ROI: ' + roiParam.width + ' x ' + roiParam.height + '<br>';
        $('#debug-roi-params').html(str);
      }
    }}());

  var client;
  var roiServiceId;
  var walkthroughServiceId;
  var currentHash;

  var prevTime;

  function getIpString() {
    if (1 < document.location.search.length) {
      var query = document.location.search.substring(1);
      var parameters = query.split('&');
      for (var i = 0; i < parameters.length; i++) {
        var element = parameters[i].split('=');
        var paramName = decodeURIComponent(element[0]);
        var paramValue = decodeURIComponent(element[1]);
        if (paramName == 'ip') {
          return paramValue;
        }
      }
    }
    return 'localhost';
  }

  function init() {
    var ip = getIpString();
    var origin = 'http://localhost';

    client = new demoWeb.Client(ip, origin);
    client.setApplicationName(applicationName);
    client.setScopes(scopes);
    discovery();
  }

  function authorize() {
    client.authorize({
      onsuccess: function() {
        discovery();
      },
      onerror: function(errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'DeviceConnectManagerの認証に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }

  function discovery() {
    client.discoverDevices({
      onsuccess: function(services) {
        for (var i = 0; i < services.length; i++) {
          console.log(services[i].id);
          if (services[i].id.indexOf('roi') >= 0) {
            roiServiceId = services[i].id;
          } else if (services[i].id.indexOf('walker') >= 0) {
            walkthroughServiceId = services[i].id;
          }
        }
        if (!roiServiceId || !walkthroughServiceId) {
          showErrorDialog('エラー', 'デバイスの検索に失敗しました。<br>デバイスプラグインのインストールされていない可能性があります。');
        } else {
          //stopAllWalkThrough();
          changeHash();
        }
      },
      onerror: function(errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'デバイスの検索に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }
  
  function stopAllWalkThrough() {
    client.request({
      'method': 'DELETE',
      'profile': 'walkthrough',
      'devices': [walkthroughServiceId],
      'onsuccess': function(id, json) {
        console.log("Stopped walk through." + json);
      },
      'onerror': function(id, errorCode, errorMessage) {
        console.log("Failed to stop walk through: errorCode=" + errorCode + ", errorMessage=" + errorMessage);
      }
    });
  }

  var retryCount = 0;
  function pollingImage(uri, targetElem, isWalkthrough) {
    var hash = location.hash;
    if (!hash || hash.split("-").length != 2) {
      return;
    }

    var fps = 1000 / walkthroughParam.fps;
    var startTime = Date.now();
    targetElem.unbind('load');
    targetElem.unbind('error');
    targetElem.attr('src', uri + '?snapshot&date=' + startTime);
    targetElem.bind('error', function() {
      console.log("Failed to load a img. count=" + retryCount);
      targetElem.unbind('load');
      targetElem.unbind('error');
      retryCount++;
      if (retryCount > 3) {
        var type = isWalkthrough ? '動画' : '静止画';
        showErrorDialog(
          'エラー', 
          type + 'の読み込みに失敗しました。' + type + '選択画面に戻ります。', 
          function() {
            var hash = location.hash;
            if (hash) {
              var split = hash.split('-');
              if (split.length == 2) {
                var name = split[0];
                var index = split[1];
                stopFuncs[name](index, {
                  onsuccess: function() {
                    startFuncs[name](index);
                  },
                  onerror: function() {}
                });
              }
            }
          });
      } else {
        pollingImage(uri, targetElem, isWalkthrough);
      }
    });
    targetElem.bind('load', function() {
      targetElem.unbind('load');
      targetElem.unbind('error');
      retryCount = 0;

      var currentTime = Date.now();
      var dt = currentTime - startTime;
      if (dt > fps) {
        pollingImage(uri, targetElem, isWalkthrough);
      } else {
        setTimeout(function() {
          pollingImage(uri, targetElem, isWalkthrough);
        }, (fps - dt));
      }

      if (prevTime !== undefined) {
        var delta = (currentTime - prevTime);
      }
      prevTime = currentTime;
    });
  }

  function startWalkThrough(index) {
    if (!checkIndex(index, walkthroughData.length)) {
      showErrorDialog('エラー', '指定されたコンテンツは存在しません。');
      return;
    }

    var source = walkthroughData[index]['source'];
    var yaw = walkthroughData[index]['yaw'];
    var roll = walkthroughData[index]['roll'];
    var pitch = walkthroughData[index]['pitch'];
    var autoPlay = debug.isEnabledAutoPlay();
    console.log('Cookie: AutoPlay: ' + autoPlay);

    client.request({
      'method': 'POST',
      'profile': 'walkthrough',
      'devices': [walkthroughServiceId],
      'params': {
        'source': source,
        'width': walkthroughParam.width,
        'height': walkthroughParam.height,
        'fps': walkthroughParam.fps,
        'fov': walkthroughParam.fov,
        'yaw': yaw,
        'roll': roll,
        'pitch': pitch,
        'autoPlay': autoPlay
      },
      'onsuccess': function(id, json) {
        var targetElem = content['#W'].target;
        walkthroughData[index]['uri'] = json.uri;
        pollingImage(json.uri.replace('localhost', client.getHost()), targetElem, true);

        if (!debug.isEnabledAutoPlay()) {
          content['#W'].overlay.show();
        } else {
          content['#W'].overlay.hide();
        }
      },
      'onerror': function(id, errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'WalkThroughの初期化に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
    
    debug.setWalkThroughParams(walkthroughData[index]);
  }

  function calibrateWalkThrough() {
    var hash = location.hash;
    var split = hash.split('-');
    if (split.length == 2) {
      var name = split[0];
      var index = split[1];
      calibrateWalkThroughInternal(index);
    }
  }

  function calibrateWalkThroughInternal(index) {
    if (!checkIndex(index, walkthroughData.length)) {
      showErrorDialog('エラー', '指定されたコンテンツは存在しません。');
      return;
    }

    var uri = walkthroughData[index]['uri'];
    client.request({
      'method': 'PUT',
      'profile': 'walkthrough',
      'devices': [walkthroughServiceId],
      'params': {
        'uri': uri,
        'calibration': true
      },
      'onsuccess': function(id, json) {
      },
      'onerror': function(id, errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'WalkThroughの設定に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }

  function stepWalkThrough(delta) {
    var hash = location.hash;
    var split = hash.split('-');
    if (split.length == 2) {
      var name = split[0];
      var index = split[1];
      stepWalkThroughInternal(index, delta);
    }
  }

  function stepWalkThroughInternal(index, delta) {
    if (!checkIndex(index, walkthroughData.length)) {
      showErrorDialog('エラー', '指定されたコンテンツは存在しません。');
      return;
    }

    var uri = walkthroughData[index]['uri'];
    client.request({
      'method': 'PUT',
      'profile': 'walkthrough',
      'devices': [walkthroughServiceId],
      'params': {
        'uri': uri,
        'delta': delta,
        'width': walkthroughParam.width,
        'height': walkthroughParam.height,
        'fps': walkthroughParam.fps,
      },
      'onsuccess': function(id, json) {
      },
      'onerror': function(id, errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'WalkThroughの初期化に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }

  function stopWalkThrough(index, callback) {
    if (!checkIndex(index, walkthroughData.length)) {
      return;
    }

    content['#W'].target.unbind('load');
    content['#W'].target.unbind('error');
    content['#W'].target.attr('src', 'assets/img/white.png');

    var uri = walkthroughData[index]['uri'];
    if (uri) {
      client.request({
        'method': 'DELETE',
        'profile': 'walkthrough',
        'devices': [walkthroughServiceId],
        'params': {
          'uri': uri,
        },
        'onsuccess': function(id, json) {
          console.log("Stopped walk through." + json);
          if (callback) {
            callback.onsuccess();
          }
        },
        'onerror': function(id, errorCode, errorMessage) {
          if (errorCode === -1) {
            showSettingsPromptDialog();
          } else {
            showErrorDialog('エラー', 'WalkThroughの終了に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
          }
          if (callback) {
            callback.onerror();
          }
        }
      });
    }
  }

  function startRoi(index) {
    if (!checkIndex(index, roiData.length)) {
      showErrorDialog('エラー', '指定されたコンテンツは存在しません。');
      return;
    }

    var source = getLocationPath() + roiData[index]['source'];
    client.request({
      'method': 'PUT',
      'profile': 'omnidirectional_image',
      'attribute': 'roi',
      'devices': [roiServiceId],
      'params': {
        'source': source,
        'sourceWidth': 2048 / 2,
        'sourceHeight': 1024 / 2
      },
      'onsuccess': function(id, json) {
        var targetElem = content['#R'].target;
        roiData[index]['uri'] = json.uri;
        setVRMode(roiData[index]);
        pollingImage(json.uri.replace('localhost', client.getHost()), targetElem, false);

        if (debug.isEnabledDebugView()) {
          content['#R'].overlay.show();
        } else {
          content['#R'].overlay.hide();
        }
      },
      'onerror': function(id, errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'OmniDirectional Imageの初期化に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }

  function stopRoi(index, callback) {
    if (!checkIndex(index, roiData.length)) {
      return;
    }

    content['#R'].target.unbind('load');
    content['#R'].target.unbind('error');
    content['#R'].target.attr('src', 'assets/img/white.png');

    var uri = roiData[index]['uri'];
    if (uri) {
      client.request({
        'method': 'DELETE',
        'profile': 'omnidirectional_image',
        'attribute': 'roi',
        'devices': [roiServiceId],
        'params': {
          'uri': uri,
        },
        'onsuccess': function(id, json) {
          console.log("Stopped region of image."+ json);
          if (callback) {
            callback.onsuccess();
          }
        },
        'onerror': function(id, errorCode, errorMessage) {
          if (errorCode === -1) {
            showSettingsPromptDialog();
          } else {
            showErrorDialog('エラー', 'OmniDirectional Imageの初期化に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
          }
          if (callback) {
            callback.onerror();
          }
        }
      });
    }
  }
  
  function zoomRoi(fovDelta) {
    var hash = location.hash;
    var split = hash.split('-');
    if (split.length == 2) {
      var name = split[0];
      var index = split[1];
      
      var roi = roiData[index];
      if (roi.fov + fovDelta < 40 || roi.fov + fovDelta > 160) {
        return;
      }
      roi.fov += fovDelta;
      setVRMode(roi);
    }
  }

  function calibrateRoi() {
    var hash = location.hash;
    var split = hash.split('-');
    if (split.length == 2) {
      var name = split[0];
      var index = split[1];
      
      var roi = roiData[index];
      client.request({
        'method': 'PUT',
        'profile': 'omnidirectional_image',
        'interface': 'roi',
        'attribute': 'settings',
        'devices': [roiServiceId],
        'params': {
          'uri': roi.uri,
          'calibration': true,
        },
        'onsuccess': function(id, json) {
          console.log(json);
        },
        'onerror': function(id, errorCode, errorMessage) {
          if (errorCode === -1) {
            showSettingsPromptDialog();
          } else {
            showErrorDialog('エラー', 'OmniDirectional Imageの設定に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
          }
        }
      });
    }
  }

  function setVRMode(roi) {
    debug.setRoiParams(roi);
    client.request({
      'method': 'PUT',
      'profile': 'omnidirectional_image',
      'interface': 'roi',
      'attribute': 'settings',
      'devices': [roiServiceId],
      'params': {
        'uri': roi.uri,
        'width': roiParam.width,
        'height': roiParam.height,
        'fov' : roi.fov,
        'roll' : roi.roll,
        'yaw' : roi.yaw,
        'pitch' : roi.pitch,
        'vr': true,
      },
      'onsuccess': function(id, json) {
        console.log(json);
      },
      'onerror': function(id, errorCode, errorMessage) {
        if (errorCode === -1) {
          showSettingsPromptDialog();
        } else {
          showErrorDialog('エラー', 'OmniDirectional Imageの設定に失敗しました。<br>errorCode: ' + errorCode + '<br>errorMessage:' + errorMessage);
        }
      }
    });
  }

  function checkIndex(index, max) {
    if (!index || (index.match(/[^0-9]+/)) || index >= max || index < 0) {
      return false;
    }
    return true;
  }

  function showErrorDialog(title, message, onretry) {
    $('#retry').off('click');
    if (onretry === undefined) {
      $('#retry').hide();
    } else {
      $('#retry').on('click', onretry);
      $('#retry').show();
    }

    $('#error-dialog .modal-title').text(title);
    $('#error-dialog .modal-body').html(message);
    $('#error-dialog').modal('show');
  }

  function showSettingsPromptDialog() {
    $('#settings-prompt-dialog .modal-title').text('エラー');
    $('#settings-prompt-dialog .modal-body').html('DeviceConnectとの通信に失敗しました。設定画面での再起動をお試しください。');
    $('#settings-prompt-dialog').modal('show');
  }

  function createContent(data) {
    var str = '';
    str += '  <div class="col-sm-6 col-md-4 content-list-view">';
    str += '    <div class="content">';
    str += '      <img class="content-image" src="' + data['thumbnail'] + '">';
    str += '      <div class="play-image"></div>';
    str += '    </div>';
    str += '    <div class="content-text">' + data['name'] + '</div>';
    str += '    <hr class="content-line">';
    str += '  </div>';
    return str;
  }

  function createRoiListView() {
    var str = "";
    for (var i = 0; i < roiData.length; i+=2) {
      str += '<div class="row">';
      str += '  <div class="col-sm-0 col-md-2 content-list-view"></div>';
      str += '   <a href="#R-' + i + '">' + createContent(roiData[i]) + '</a>';
      if (i + 1 < roiData.length) {
        str += '   <a href="#R-' + (i + 1) + '">' + createContent(roiData[i + 1]) + '</a>';
      }
      str += '  <div class="col-sm-0 col-md-2 content-list-view"></div>';
      str += '</div>';
    }
    $('#list-view').html(str);
    $('#movie').css('color', '#666');
    $('#image').css('color', '#FFF');
    $('.play-image').hide();
  }

  function createWalkThroughListView() {
    var str = "";
    for (var i = 0; i < walkthroughData.length; i+=2) {
      str += '<div class="row">';
      str += '  <div class="col-sm-0 col-md-2 content-list-view"></div>';
      str += '   <a href="#W-' + i + '">' + createContent(walkthroughData[i]) + '</a>';
      if (i + 1 < walkthroughData.length) {
        str += '   <a href="#W-' + (i + 1) + '">' + createContent(walkthroughData[i + 1]) + '</a>';
      }
      str += '  <div class="col-sm-0 col-md-2 content-list-view"></div>';
      str += '</div>';
    }
    $('#list-view').html(str);
    $('#movie').css('color', '#FFF');
    $('#image').css('color', '#666');
    $('.play-image').show();
  }

  function fadeInList() {
    content['#W'].overlay.hide();
    content['#W'].main.hide();
    content['#R'].overlay.hide();
    content['#R'].main.hide();
    $('#list-view').show();
    $('#list-view').stop(true, true);
    $('#list-view').css({ opacity: "0.0" });
    $('#list-view').fadeTo(1000, 1);
    $('html,body').animate({ scrollTop: 0 }, 'fast');
  }

  function fadeInContent(name) {
    var overlay = content[name].overlay,
        main = content[name].main,
        target = content[name].target;
  
    $('#list-view').fadeTo(1000, 0, function() {
      var w = window.innerWidth;
      var h = window.innerHeight;
      $('#list-view').hide();
      overlay.css({'right':'-100px'});
      main.show();
      main.stop(true, true);
      main.css({ opacity: "0.0" });
      main.fadeTo(1000, 1, function() {
        overlay.animate({'right':'10px'}, 400, "swing");
      });
      if (w > h) {
        target.css({ width: "100%", height: "auto" });
      } else {
        target.css({ width: "auto", height: "100%" });
      }
      var p = main.offset().top;
      $('html,body').animate({ scrollTop: p }, 'fast');
    });
  }

  var content = {};

  var startFuncs = {};
  startFuncs['#R'] = startRoi;
  startFuncs['#W'] = startWalkThrough;

  var stopFuncs = {};
  stopFuncs['#R'] = stopRoi;
  stopFuncs['#W'] = stopWalkThrough;

  function changeHash() {
    if (currentHash) {
      var split = currentHash.split('-');
      if (split.length == 2) {
        var name = split[0];
        var index = split[1];
        stopFuncs[name](index);
      }
    }

    var hash = currentHash = location.hash;
    if (hash) {
      var split = hash.split('-');
      if (split.length == 2) {
        var name = split[0];
        var index = split[1];
        startFuncs[name](index);
        fadeInContent(name);
        return;
      }
    }
    if (hash === '#R') {
      createRoiListView();
    } else {
      createWalkThroughListView();
    }
    fadeInList();
  }

  function getLocationPath() {
    var href = location.href;
    var index = href.lastIndexOf('/');
    if (index > 8) {
      return href.substring(0, index + 1);
    }
    return href + '/';
  }

  function preloadFunc() {
    for (var i = 0; i< arguments.length; i++) {
      $("<img>").attr("src", arguments[i]);
    }
  }

  function setDoubleTapListener(elem, ondbltap) {
    var dataName = 'dblTap';
    elem.data(dataName, false).on('click', function() {
      if ($(this).data(dataName)) {
        ondbltap();
        elem.data(dataName, false);
      } else {
        $(this).data(dataName, true);
      }
      setTimeout(function() {
        elem.data(dataName, false);
      }, 500);
    });
  }

  $(document).ready(function() {
    preloadFunc(
        "assets/img/down_hover.png",
        "assets/img/down_push.png",
        "assets/img/down_normal.png",
        "assets/img/UP_hover.png",
        "assets/img/UP_push.png",
        "assets/img/UP_normal.png",
        "assets/img/play_push.png",
        "assets/img/play_normal.png",
        "assets/img/white.png"
    );

    init();

    window.onhashchange = function() {
      changeHash();
    }

    content['#R'] = {
      main: $('#roi-content-main'),
      target: $('#roi-target'),
      overlay: $('#roi-overlay')
    };

    content['#W'] = {
      main: $('#walk-content-main'),
      target: $('#walk-target'),
      overlay: $('#walk-overlay')
    };

    $('#close').on('click', function() {
      var href = location.href;
      if (href.indexOf("#") > 0) {
        var uri = href.substring(0, href.indexOf("#"));
        var hash = location.hash;
        if (hash.indexOf('R') == 1) {
          uri += '#R';
        } else {
          uri += '#W';
        }
        location.href = uri;
      }
    });

    $('#settings').on('click', function() {
      location.href = 'dconnect://settings/';
    });

    $('#walk-arrow-up').on('click', function() {
      stepWalkThrough(+30);
    });

    $('#walk-arrow-down').on('click', function() {
      stepWalkThrough(-30);
    });
    
    $('#roi-zoom-in').on('click', function() {
      zoomRoi(-5);
    });

    $('#roi-zoom-out').on('click', function() {
      zoomRoi(5);
    });

    setDoubleTapListener($('#walk-target'), function() {
      calibrateWalkThrough();
    });

    setDoubleTapListener($('#roi-target'), function() {
      calibrateRoi();
    });
    
    $.cookie.json = true;
    
    var demoSettings = $.cookie('demoSettings');
    if (demoSettings === undefined) {
      $.cookie('demoSettings', {
        isEnabledDebugView: false,
        isEnabledDebugAutoplay: true
      }, { path: '/' });
      demoSettings = $.cookie('demoSettings');
      console.log('Cookie: demoSettings is created.', demoSettings);
    } else {
      if (typeof demoSettings === 'string') {
         demoSettings = JSON.parse(demoSettings);
      }
      console.log('Cookie: demoSettings is loaded.', demoSettings);
    }
    
    debug.setSettings(demoSettings);
    debug.showDebugView(debug.isEnabledDebugView());
  });
})();
