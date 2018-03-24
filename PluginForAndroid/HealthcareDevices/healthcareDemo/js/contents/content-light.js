(function () {
  'use strict';

  /**
   * Device Web API Managerにアクセスするためのインスタンス。
   */
  var demoClient;

  /**
   * ダイアログ。
   */
  var modalDialog;

  /**
   * 操作を行うライト一覧。
   */
  var lightList = [];

  /**
   * ライトの電源状態.
   */
  var lightPower = false;

  /**
   * 選択された色。
   */
  var selectColor = "FFFFFF";

  /**
   * 選択された光強度。
   */
  var selectBrightness = 1.0;

  /**
   * 送信状態を保持する。
   * <ul>
   * <li> true: 送信中
   * <li> false: 待機中
   * </ul>
   */
  var sendStateFlag = false;

  /**
   * カラーピッカーの大きさ。
   */
  var colorPickerSize = 130;

  /**
   * カラーピッカーを表示するdivのサイズ.
   */
  var divSize = 160;

  var oldColorPickerPosition = {x: 0, y:0};

  /**
   * カラーピッカーを描画する。
   */
  function initColorPicker() {
    var c = $('#color-picker').get(0).getContext('2d');
    c.fillStyle = "rgb(212, 212, 212)";
    c.beginPath();
    c.arc(divSize, divSize, colorPickerSize + 2, 0, Math.PI * 2, false);
    c.fill();
    for (var r = 0; r < colorPickerSize; r+=4) {
      var rr = r * 255 / colorPickerSize;
      for (var th = 0; th < colorPickerSize; th += (colorPickerSize - 1) / r) {
        setColor(c, r, -th, rr, 0, rr * th / (colorPickerSize - 1));
        setColor(c, r,  th, rr, rr * th / (colorPickerSize - 1), 0);
        setColor(c, r, colorPickerSize * 2 - th, rr * th / (colorPickerSize - 1), rr, 0);
        setColor(c, r, colorPickerSize * 2 + th, 0, rr, rr * th / (colorPickerSize - 1));
        setColor(c, r, colorPickerSize * 4 - th, 0, rr * th / (colorPickerSize - 1), rr);
        setColor(c, r, colorPickerSize * 4 + th, rr * th / (colorPickerSize - 1), 0, rr);
      }
    }

    var brightness = $("#brightness-slider")
      .slider({ min: 0, max: 100, value: 100, focus: false })
      .on('slideStart', function() {
        if (!checkLights()) {
          showErrorDialogNoLights();
        }
      })
      .on('slide', function() {
        moveSelectBrightness(brightness.getValue(), false);
      })
      .on('change', function() {
        moveSelectBrightness(brightness.getValue(), true);
      })
      .data('slider');

    var x = $('#color-picker').position().left + divSize - 8;
    var y = $('#color-picker').position().top + divSize - 8;
    $('#color-cursor').css({left:x, top:y});
    oldColorPickerPosition.x = $('#color-picker').position().left;
    oldColorPickerPosition.y = $('#color-picker').position().top;

    var isTouch = ('ontouchstart' in window);
    var isFirefox = (navigator.userAgent.indexOf("Firefox") != -1);
    $('#color-picker').bind({
      'touchstart mousedown': function(e) {
        var px, py;
        if (isFirefox) {
          px = e.originalEvent.touches[0].pageX;
          py = e.originalEvent.touches[0].pageY - 50;
        } else if (isTouch) {
          px = event.changedTouches[0].pageX;
          py = event.changedTouches[0].pageY - 50;
        } else {
          px = e.pageX;
          py = e.pageY - 50;
        }
        if (checkTouchOutOfColorPicker(px, py)) {
          return;
        }
        e.preventDefault();

        if (!checkLights()) {
          showErrorDialogNoLights();
          return;
        }

        this.pageX = px;
        this.pageY = py;
        this.left = this.pageX;
        this.top = this.pageY;
        this.touched = true;
      },
      'touchmove mousemove': function(e) {
        if (!this.touched) {
          return;
        }
        e.preventDefault();

        var px, py;
        if (isFirefox) {
          px = e.originalEvent.touches[0].pageX;
          py = e.originalEvent.touches[0].pageY - 50;
        } else if (isTouch) {
          px = event.changedTouches[0].pageX;
          py = event.changedTouches[0].pageY - 50;
        } else {
          px = e.pageX;
          py = e.pageY - 50;
        }

        this.left = this.left - (this.pageX - px);
        this.top = this.top - (this.pageY - py);
        moveCursor(this.left, this.top);
        this.pageX = px;
        this.pageY = py;
      },
      'touchend mouseup': function(e) {
        if (!this.touched) {
          return;
        }
        sendStateFlag = false;
        moveCursor(this.left, this.top);
        this.touched = false;
      }
    });

    $(window).on('resize', function(e) {
      var dx = $('#color-picker').position().left - oldColorPickerPosition.x;
      var dy = $('#color-picker').position().top - oldColorPickerPosition.y;
      var x =  $('#color-cursor').position().left + dx;
      var y =  $('#color-cursor').position().top + dy;
      $('#color-cursor').css({left:x, top:y});

      oldColorPickerPosition.x = $('#color-picker').position().left;
      oldColorPickerPosition.y = $('#color-picker').position().top;
    });
  }

  /**
   * カラーピッカーの外側をタッチしているかの判定を行う。
   * @param x x座標
   * @param y y座標
   * @return 外側をタッチしている場合はtrue、それ以外はfalse
   */
  function checkTouchOutOfColorPicker(x, y) {
    var cx = $('#color-picker').position().left + divSize;
    var cy = $('#color-picker').position().top + divSize;
    var dx = x - cx;
    var dy = y - cy;
    var radius = Math.sqrt(dx * dx + dy * dy);
    return (radius > colorPickerSize);
  }

  /**
   * カラーピッカーのカーソルを移動する。
   * 
   * @param x x座標
   * @param y y座標
   */
  function moveCursor(x, y) {
    var cx = $('#color-picker').position().left + divSize;
    var cy = $('#color-picker').position().top + divSize;
    var dx = x - cx;
    var dy = y - cy;
    var radius = Math.sqrt(dx * dx + dy * dy);
    if (radius > colorPickerSize) {
      var theta = calcTheta(dx, dy);
      x = cx + colorPickerSize * Math.cos(theta);
      y = cy + colorPickerSize * Math.sin(theta);
    }
    $('#color-cursor').css({left:x - 10, top:y - 10});

    x -= $('#color-picker').position().left;
    y -= $('#color-picker').position().top;
    moveSelectColor(x, y);
  }

  /**
   * 指定された半径(radius)、角度(theta)にrgbの色を設定する。
   * 
   * @param canvas 描画を行うキャンバス
   * @param radius 半径
   * @param theta 角度
   * @param r 赤色成分(0 - 255)
   * @param g 緑色成分(0 - 255)
   * @param b 青色成分(0 - 255)
   */
  function setColor(canvas, radius, theta, r, g, b) {
    var x = divSize - 1 + 0.5 + radius * Math.cos(theta * Math.PI / (colorPickerSize * 3));
    var y = divSize - 1 + 0.5 - radius * Math.sin(theta * Math.PI / (colorPickerSize * 3));
    canvas.strokeStyle = "rgb(" + calcWhite(r, radius) + "," + calcWhite(g, radius) + "," + calcWhite(b, radius) + ")";
    canvas.strokeRect(x-1.5, y-1.5, 4, 4);
  }

  /**
   * 中心に近いほど、白くするので、中心からの距離を加算する。
   * 
   * @param color 色
   * @param radius 距離
   * @returns {Number} 距離を色に加算した値
   */
  function calcWhite(color, radius) {
    var c = Math.round(color + 255 - (255 * radius / colorPickerSize));
    if (c < 0) c = 0;
    if (c > 255) c = 255;
    return c;
  }

  /**
   * xy座標から角度を計算する。
   * 
   * @param x x座標
   * @param y y座標
   * @returns 角度
   */
  function calcTheta(x, y) {
    var theta = Math.atan2(y, x);
    if (theta < 0) {
      theta = Math.PI + (Math.PI + theta);
    }
    return theta;
  }

  /**
   * xy座標から距離を計算する。
   * 
   * @param x x座標
   * @param y y座標
   * @returns {Number} 距離
   */
  function calcDistance(x, y) {
    var radius = Math.round(Math.sqrt(x * x + y * y));
    if (radius > colorPickerSize - 1) {
      radius = colorPickerSize - 1;
    }
    return radius;
  }

  /**
   * 指定されたxy座標の色を取得する。
   * 
   * @param x x座標
   * @param y y座標
   * @returns {String} 色データ(#FFFFFF形式)
   */
  function getColor(x, y) {
    x -= divSize - 1;
    y -= divSize - 1;

    var theta = calcTheta(x, y);
    var radius = calcDistance(x, y);
    var pi2 = 2 * Math.PI;
    var r, g, b;
    if (pi2 - pi2 / 6 <= theta || theta <= pi2 / 6) {
      r = 255;
    } else if (pi2 / 6 < theta && theta < pi2 / 3) {
      r = 255 - 255 * (theta - pi2 / 6) / (pi2 / 6);
    } else if (2 * pi2 / 3 < theta) {
      r = 255 * (theta - (2 * pi2 / 3)) / (pi2 / 6);
    } else {
      r = 0;
    }

    if (pi2 / 6 <= theta && theta <= pi2 / 3 + pi2 / 6) {
      b = 255;
    } else if (0 < theta && theta < pi2 / 6) {
      b = 255 * theta / (pi2 / 6);
    } else if (2 * pi2 / 3 - pi2 / 6 < theta && theta < 2 * pi2 / 3) {
      b = 255 - 255 * (theta -(2 * pi2 / 3 - pi2 / 6)) / (pi2 / 6);
    } else {
      b = 0;
    }

    if (2 * pi2 / 3 - pi2 / 6 <= theta && theta <= 2 * pi2 / 3 + pi2 / 6) {
      g = 255;
    } else if (pi2 / 3 < theta && theta < pi2 / 3 + pi2 / 6) {
      g = 255 * (theta - pi2 / 3) / (pi2 / 6);
    } else if (pi2 - pi2 / 6 < theta && theta < pi2) {
      g = 255 - 255 * (theta - (pi2 - pi2 / 6)) / (pi2 / 6);
    } else {
      g = 0;
    }
    r = calcWhite(r, radius);
    g = calcWhite(g, radius);
    b = calcWhite(b, radius);
    return convertColor(r, g, b);
  }

  /**
   * 色情報をFFFFFF形式の文字列に変換する。
   * 
   * @param r 赤色成分(0-255)
   * @param g 緑色成分(0-255)
   * @param b 青色成分(0-255)
   * @returns 色情報
   */
  function convertColor(r, g, b) {
    return convert10To16(r) + convert10To16(g) + convert10To16(b);
  }

  /**
   * 10進数を16進数に変換する。
   * <p>
   * 16以下の場合には、先頭に0をつける。
   * </p>
   * @param value 10進数の数値
   * @returns 16進数の値
   */
  function convert10To16(value) {
    if (value < 16) {
      return "0" + value.toString(16);
    } else {
      return value.toString(16);
    }
  }

  /**
   * 座標から色の選択を行い、可能ならばデバイスに色変更の命令を行う。
   * 
   * @param x x座標
   * @param y y座標
   * @returns
   */
  function moveSelectColor(x, y) {
    selectColor = getColor(x, y);
    if (lightPower) {
      addLightCommand(true);
    }
  }

  /**
   * 座標からブライトネスの選択を行い、可能ならばデバイスにブライトネス変更の命令を行う。
   * 
   * @param brightness ブライトネス(0-100)
   * @param force trueの場合は強制的にブライトネスを反映する
   * @returns
   */
  function moveSelectBrightness(brightness, force) {
    if (force) {
      sendStateFlag = false;
    }
    selectBrightness =  brightness / 100.0;
    if (lightPower) {
      addLightCommand(true);
    }
  }

  /**
   * ライトの命令を追加する。
   * 
   * @param power 電源(true: 点灯、false: 消灯)
   */
  function addLightCommand(power) {
    if (!checkLights() || sendStateFlag) {
      return;
    }
    sendStateFlag = true;

    addRequest({
      power: power,
      selectColor: selectColor,
      selectBrightness: selectBrightness
    });
  }

  /**
   * ライトの色を設定する。
   * 
   * @param req リクエスト
   * @param callback コールバック
   */
  function setLightColor(req, callback) {
    var count = 0;
    for (var i = 0; i < lightList.length; i++) {
      var light = lightList[i];
      if (req.power) {
        count++;
        sendLightColor(light.serviceId, light.light.lightId, req.selectColor, req.selectBrightness, function() {
          count--;
          if (count == 0) {
            callback();
          }
        });
      } else {
        count++;
        sendTurnOff(light.serviceId, light.light.lightId, function() {
          count--;
          if (count == 0) {
            callback();
          }
        });
      }
    }
  }

  /**
   * ライトを操作するリクエストを格納するキュー。
   */
  var requestQueue = [];

  /**
   * リクエストを追加する。
   * 
   * 10個以上のリクエストが溜まっている場合には最初のリクエストから削除する。
   * 
   * @param request リクエスト
   */
  function addRequest(request) {
    requestQueue.push(request);
    if (requestQueue.length == 1) {
      sendRequest();
    } else if (requestQueue.length > 5) {
      requestQueue.splice(0, 1);
    }
  }

  /**
   * リクエストを順番に送信する。
   */
  function sendRequest() {
    if (requestQueue.length == 0) {
      return;
    }

    var request = requestQueue[0];
    setLightColor(request, function() {
      requestQueue.splice(0, 1);
      sendStateFlag = false;
      setTimeout(function() {
        sendRequest();
      }, 400);
    });
  }

  /**
   * ライトの色指定する命令を送信する。
   * @param serviceId サービスID
   * @param lightId ライトID
   * @param color 色指定(#FFFFFF形式)
   * @param brightness ブライトネス(0.0 - 1.0)
   * @param callback コールバック
   * @returns
   */
  function sendLightColor(serviceId, lightId, color, brightness, callback) {
    demoClient.request({
      "method": "POST",
      "profile": "light",
      "devices": [serviceId],
      "params": {
        "lightId": lightId,
        "color": color,
        "brightness": brightness,
      },
      "onerror": function(id, errorCode, errorMessage) {
        showErrorDialogWebAPI();
      },
      "oncomplete": callback
    });
  }

  /**
   * 消灯の命令を送信する。
   * 
   * @param serviceId サービスID
   * @param lightId ライトID
   * @returns
   */
  function sendTurnOff(serviceId, lightId, callback) {
    demoClient.request({
      "method": "DELETE",
      "profile": "light",
      "devices": [serviceId],
      "params": {
        "lightId": lightId,
      },
      "onerror": function(id, errorCode, errorMessage) {
        showErrorDialogWebAPI();
      },
      "oncomplete": callback
    });
  }

  /**
   * Light操作のコマンドを作成する。
   * 
   * @param serviceId サービスID
   * @param lightId ライトID
   * @param color 色(#FFFFFF形式)
   * @param brightness ブライトネス(0.0 - 1.0)
   * @returns
   */
  function createLightCommand(serviceId, lightId, color, brightness) {
    var builder = new dConnect.URIBuilder();
    builder.setProfile('light');
    builder.setServiceId(serviceId);
    builder.setAccessToken(accessToken);
    builder.addParameter('lightId', lightId);
    if (color) {
      builder.addParameter('color', color);
    }
    if (brightness) {
      builder.addParameter('brightness', brightness);
    }
    return builder.build();
  }

  /**
   * ライトを点灯する。
   * 
   * @param force trueの場合は強制的に点灯する
   */
  function turnOnLights(force) {
    if (!checkLights()) {
      showErrorDialogNoLights();
      return;
    }

    if (!lightPower || force) {
      lightPower = true;
      sendStateFlag = false;
      addLightCommand(true);
      $('#turn-on').css({
        'background-color': '#49B4DC',
        'color': '#FFFFFF'
      });
      $('#turn-off').css({
        'background-color': '#FFFFFF',
        'color': '#CCCCCC'
      });
    }
  }

  /**
   * ライトを消灯する。
   * 
   * @param force trueの場合は強制的に消灯する
   */
  function turnOffLights(force) {
    if (lightPower || force) {
      lightPower = false;
      sendStateFlag = false;
      addLightCommand(false);
      $('#turn-off').css({
        'background-color': '#49B4DC',
        'color': '#FFFFFF'
      });
      $('#turn-on').css({
        'background-color': '#FFFFFF',
        'color': '#CCCCCC'
      });
    }
  }

  /**
   * ライトが設定されているか確認する。
   * 
   * @return ライトが設定されている場合はtrue、それ以外はfalse
   */
  function checkLights() {
    return (lightList && lightList.length > 0);
  }

  /**
   * 通信エラーを通知するダイアログを表示する。
   */
  function showErrorDialogWebAPI() {
    showErrorDialog('エラー', '通信に失敗しました。');
  }

  /**
   * ライトが設定されていないことを通知するダイアログを表示する。
   */
  function showErrorDialogNoLights() {
    showErrorDialog('エラー', 'ライトが設定されていません。');
  }

  var isShowDialog = false;

  /**
   * エラーダイアログを表示する。
   * 
   * @param title ダイアログのタイトル
   * @param message ダイアログのメッセージ
   */
  function showErrorDialog(title, message) {
    if (isShowDialog) {
      return;
    }
    isShowDialog = true;

    var modalInstance = modalDialog.open({
      templateUrl: 'error-dialog-light.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return title;
        },
        'message': function() {
          return message;
        }
      }
    });
    modalInstance.result.then(function (result) {
      isShowDialog = false;
    });
  }

  /**
   * ライトの設定を画面に設定する。
   * 
   * @param $scope スコープ
   * @param lights ライト一覧
   */
  function setLightDevices($scope, lights) {
    lightList = lights;
    if (lightList.length == 1) {
      $scope.deviceName = lightList[0].name;
      turnOnLights(true)
      return true;
    } else if (lightList.length > 1) {
      $scope.deviceName = lightList[0].name + " その他(" + (lightList.length - 1) + ")";
      turnOnLights(true)
      return true;
    } else {
      return false;
    }
  }

  /**
   * ライトを検索して登録する。
   */
  function discoverLights($scope, $location, lightService) {
    lightService.discover(demoClient, {
      oncomplete: function(lights) {
        if (setLightDevices($scope, lights)) {
          $scope.$apply();
        }
      },
      onerror: function(errorCode, errorMessage) {
        $location.path('/error/' + errorCode);
      }
    });
  }

  var LightController = function ($scope, $modal, $window, $location, demoWebClient, lightService) {
    demoClient = demoWebClient;
    modalDialog = $modal;

    $scope.title = 'ライト制御';
    initColorPicker();

    selectColor = "FFFFFF";
    selectBrightness = 1.0;

    if (!setLightDevices($scope, lightService.lights)) {
      $scope.deviceName = "ライト未設定";
      turnOffLights(true);
      discoverLights($scope, $location, lightService);
    }

    $scope.lightOn = "On";
    $scope.lightOff = "Off";
    $scope.settingAll = function() {
      demoClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings/light');
          });
        },

        onerror: function(errorCode, errorMessage) {
          $scope.$apply(function() {
            $location.path('/error/' + errorCode);
          });
        }
      });
    }
    $scope.discoverLight = function() {
      $location.path('/light/select');
    }
    $scope.turnOn = function() {
      turnOnLights(false);
    }
    $scope.turnOff = function() {
      turnOffLights(false);
    }
    $scope.back = function() {
      $location.path('/');
    };
  };

  angular.module('demoweb')
    .controller('LightController', 
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'lightService', LightController]);
})();
