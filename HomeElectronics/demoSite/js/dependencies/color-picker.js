/**
 color-picker.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var ColorPicker = function(callback){
  this.callback = callback;
  this.colorPickerId = '#color-picker';
  this.colorCursorId = '#color-cursor';
  /** カラーピッカーを表示するdivのサイズ. */
  this.divSize = 160;
  /** カラーピッカーの大きさ。*/
  this.colorPickerSize = 130;
  this.oldColorPickerPosition = {x: 0, y:0};
  /** 選択された色。 */
  this.selectColor = "FFFFFF";
  this.yOffset = 80;
};

ColorPicker.prototype = {

  drawPicker : function() {
    this.drawColorPicker($(this.colorPickerId)[0]);
    this.initColorPickerListener();
  },

  drawColorPicker : function(canvas){
    var c = canvas.getContext('2d');
    c.fillStyle = "rgb(212, 212, 212)";
    c.beginPath();
    c.arc(this.divSize, this.divSize, this.colorPickerSize + 2, 0, Math.PI * 2, false);
    c.fill();
    for (var r = 0; r < this.colorPickerSize; r+=4) {
      var rr = r * 255 / this.colorPickerSize;
      for (var th = 0; th < this.colorPickerSize; th += (this.colorPickerSize - 1) / r) {
        this.setColor(c, r, -th, rr, 0, rr * th / (this.colorPickerSize - 1));
        this.setColor(c, r,  th, rr, rr * th / (this.colorPickerSize - 1), 0);
        this.setColor(c, r, this.colorPickerSize * 2 - th, rr * th / (this.colorPickerSize - 1), rr, 0);
        this.setColor(c, r, this.colorPickerSize * 2 + th, 0, rr, rr * th / (this.colorPickerSize - 1));
        this.setColor(c, r, this.colorPickerSize * 4 - th, 0, rr * th / (this.colorPickerSize - 1), rr);
        this.setColor(c, r, this.colorPickerSize * 4 + th, rr * th / (this.colorPickerSize - 1), 0, rr);
      }
    }
  },

  initColorPickerListener : function(){
    var x = $(this.colorPickerId).position().left + this.divSize - 8;
    var y = $(this.colorPickerId).position().top + this.divSize - 8;

    $(this.colorCursorId).css({left:x, top:y});

    this.oldColorPickerPosition.x = $(this.colorPickerId).position().left;
    this.oldColorPickerPosition.y = $(this.colorPickerId).position().top;

    var isTouch = ('ontouchstart' in window);
    var isFirefox = (navigator.userAgent.indexOf("Firefox") != -1);
    $(this.colorPickerId).bind({
      'touchstart mousedown': function(e) {
        var element = e.currentTarget;
        var px, py;
        if (isFirefox) {
          px = e.originalEvent.touches[0].pageX;
          py = e.originalEvent.touches[0].pageY - this.yOffset;
        } else if (isTouch) {
          px = event.changedTouches[0].pageX;
          py = event.changedTouches[0].pageY - this.yOffset;
        } else {
          px = e.pageX;
          py = e.pageY - this.yOffset;
        }
        if (this.checkTouchOutOfColorPicker(px, py)) {
          return;
        }
        e.preventDefault();
        element.pageX = px;
        element.pageY = py;
        element.left = element.pageX;
        element.top = element.pageY;
        element.touched = true;
      }.bind(this),
      'touchmove mousemove': function(e) {
        var element = e.currentTarget;
        if (!element.touched) {
          return;
        }
        e.preventDefault();
        var px, py;
        if (isFirefox) {
          px = e.originalEvent.touches[0].pageX;
          py = e.originalEvent.touches[0].pageY - this.yOffset;
        } else if (isTouch) {
          px = event.changedTouches[0].pageX;
          py = event.changedTouches[0].pageY - this.yOffset;
        } else {
          px = e.pageX;
          py = e.pageY - this.yOffset;
        }
        if (this.checkTouchOutOfColorPicker(px, py)) {
          return;
        }
        element.left = element.left - (element.pageX - px);
        element.top = element.top - (element.pageY - py);
        this.moveCursor(element.left, element.top);
        element.pageX = px;
        element.pageY = py;
      }.bind(this),
      'touchend mouseup': function(e) {
        var element = e.currentTarget;
        if (!element.touched) {
          return;
        }
        this.moveCursor(element.left, element.top);
        element.touched = false;
      }.bind(this)
    });
  },

  /**
   * カラーピッカーの外側をタッチしているかの判定を行う。
   * @param x x座標
   * @param y y座標
   * @return 外側をタッチしている場合はtrue、それ以外はfalse
   */
  checkTouchOutOfColorPicker : function(x, y) {
    var cx = $(this.colorPickerId).position().left + this.divSize;
    var cy = $(this.colorPickerId).position().top + this.divSize;
    var dx = x - cx;
    var dy = y - cy;
    var radius = Math.sqrt(dx * dx + dy * dy);
    return (radius > this.colorPickerSize);
  },

  /**
   * カラーピッカーのカーソルを移動する。
   *
   * @param x x座標
   * @param y y座標
   */
  moveCursor : function(x, y, prevent) {
    var cx = $(this.colorPickerId).position().left + this.divSize;
    var cy = $(this.colorPickerId).position().top + this.divSize;
    var dx = x - cx;
    var dy = y - cy;
    var radius = Math.sqrt(dx * dx + dy * dy);
    if (radius > this.colorPickerSize) {
      var theta = this.calcTheta(dx, dy);
      x = cx + this.olorPickerSize * Math.cos(theta);
      y = cy + this.colorPickerSize * Math.sin(theta);
    }
    $(this.colorCursorId).css({left:x - 10, top:y - 10});
    x -= $(this.colorPickerId).position().left;
    y -= $(this.colorPickerId).position().top;
    if(!prevent){
      this.moveSelectColor(x, y);
    }
  },

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
  setColor : function(canvas, radius, theta, r, g, b) {
    var x = this.divSize - 1 + 0.5 + radius * Math.cos(theta * Math.PI / (this.colorPickerSize * 3));
    var y = this.divSize - 1 + 0.5 - radius * Math.sin(theta * Math.PI / (this.colorPickerSize * 3));
    canvas.strokeStyle = "rgb(" + this.calcWhite(r, radius) + "," + this.calcWhite(g, radius) + "," + this.calcWhite(b, radius) + ")";
    canvas.strokeRect(x-1.5, y-1.5, 4, 4);
  },

  /**
   * 中心に近いほど、白くするので、中心からの距離を加算する。
   *
   * @param color 色
   * @param radius 距離
   * @returns {Number} 距離を色に加算した値
   */
  calcWhite : function(color, radius) {
    var c = Math.round(color + 255 - (255 * radius / this.colorPickerSize));
    if (c < 0) c = 0;
    if (c > 255) c = 255;
    return c;
  },

  /**
   * xy座標から角度を計算する。
   *
   * @param x x座標
   * @param y y座標
   * @returns 角度
   */
  calcTheta : function(x, y) {
    var theta = Math.atan2(y, x);
    if (theta < 0) {
      theta = Math.PI + (Math.PI + theta);
    }
    return theta;
  },

  /**
   * xy座標から距離を計算する。
   *
   * @param x x座標
   * @param y y座標
   * @returns {Number} 距離
   */
  calcDistance : function(x, y) {
    var radius = Math.round(Math.sqrt(x * x + y * y));
    if (radius > this.colorPickerSize - 1) {
      radius = this.colorPickerSize - 1;
    }
    return radius;
  },

  /**
   * 指定されたxy座標の色を取得する。
   *
   * @param x x座標
   * @param y y座標
   * @returns {String} 色データ(#FFFFFF形式)
   */
  getColor : function(x, y) {
    x -= this.divSize - 1;
    y -= this.divSize - 1;

    var theta = this.calcTheta(x, y);
    var radius = this.calcDistance(x, y);
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
    r = this.calcWhite(r, radius);
    g = this.calcWhite(g, radius);
    b = this.calcWhite(b, radius);
    return this.convertColor(r, g, b);
  },

  /**
   * 色情報をFFFFFF形式の文字列に変換する。
   *
   * @param r 赤色成分(0-255)
   * @param g 緑色成分(0-255)
   * @param b 青色成分(0-255)
   * @returns 色情報
   */
  convertColor : function(r, g, b) {
    return this.convert10To16(r) + this.convert10To16(g) + this.convert10To16(b);
  },

  /**
   * 10進数を16進数に変換する。
   * <p>
   * 16以下の場合には、先頭に0をつける。
   * </p>
   * @param value 10進数の数値
   * @returns 16進数の値
   */
  convert10To16 : function(value) {
    if (value < 16) {
      return "0" + value.toString(16);
    } else {
      return value.toString(16);
    }
  },

  /**
   * 座標から色の選択を行い、可能ならばデバイスに色変更の命令を行う。
   *
   * @param x x座標
   * @param y y座標
   * @returns
   */
  moveSelectColor : function(x, y) {
    var color = this.getColor(x, y);
    this.callback(color);
  }

};
