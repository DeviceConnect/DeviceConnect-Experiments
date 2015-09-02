/**
 AllPatch.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var ComicPatch = function(){};
ComicPatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  var edges = [{edge:8,  value:5},
               {edge:16, value:10},
               {edge:32, value:100},
               {edge:64, value:180},
               {edge:128,value:200},
               {edge:200,value:255}];

  for (var y = 0; y < height; y++) {
    var offsetY = y * width;
    for (var x = 0; x < width; x++) {
      var offsetXY = (offsetY + x) * 4;
      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];
      var rr = 0;
      var gg = 0;
      var bb = 0;
      var rrggbb = 0;
      var counter = 0;
      for (var yy = y - 1; yy <= y + 1; yy++){
        if (yy < 0 || height <= yy) {continue;}
        var offsetYY = yy * width;
        for (var xx = x - 1; xx <= x + 1; xx++){
          if (xx < 0 || width <= xx) {continue;}
          var offsetXXYY = (offsetYY + xx) * 4;
          rrggbb +=
              Math.abs(r - originalData[offsetXXYY + 0]) +
              Math.abs(g - originalData[offsetXXYY + 1]) +
              Math.abs(b - originalData[offsetXXYY + 2]) ;
          counter++;
        }
      }

      var baseR = originalData[offsetXY + 0];
      var baseG = originalData[offsetXY + 1];
      var baseB = originalData[offsetXY + 2];

      var ave = rrggbb / counter;

      var value = 0;
      for(var i = 0, l = edges.length ; i < l ; i++){
        if(ave <= edges[i].edge){
          value = edges[i].value;
          break;
        }
      }

      modifyData[offsetXY + 0] = baseR - value;
      modifyData[offsetXY + 1] = baseG - value;
      modifyData[offsetXY + 2] = baseB - value;
      modifyData[offsetXY + 3] = 256;
    }
  }
  return modify;
};

var EmptyPatch = function(){};
EmptyPatch.prototype.process = function(context, original){
  return original;
};

var GrayScalePatch = function(){};
GrayScalePatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  for (var y = 0; y < height; y++) {
    var offsetY = y * width;
    for (var x = 0; x < width; x++) {
      var offsetXY = (offsetY + x) * 4;

      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];

      var gray = (r+g+b)/3;

      modifyData[offsetXY + 0] =
      modifyData[offsetXY + 1] =
      modifyData[offsetXY + 2] = gray;
      modifyData[offsetXY + 3] = 255;
    }
  }
  return modify;
};

var MosaicPatch = function(){};
MosaicPatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  var interval = 16;

  for (var y = 0; y < height; y += interval) {
    var offsetY = y * width;
    for (var x = 0; x < width; x += interval) {
      //get sampling colors
      var offsetXY = (offsetY + x) * 4;
      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];

      var xlimit = interval;
      if((x + interval) > width){
        xlimit = width - x;
      }

      var ylimit = interval;
      if((y + interval) > height){
        ylimit = height - y ;
      }

      for (var yy = 0; yy < ylimit ; yy++){
        var offsetYY = (offsetY + x) + yy * width;
        for(var xx = 0; xx < xlimit ; xx++){
          var offset = (offsetYY + xx)*4;
          modifyData[offset + 0] = r;
          modifyData[offset + 1] = g;
          modifyData[offset + 2] = b;
          modifyData[offset + 3] = 255;
        }
      }
    }
  }
  return modify;
};

var OutlineWhitePatch = function(){};
OutlineWhitePatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  for (var y = 0; y < height; y++) {
    var offsetY = y * width;
    for (var x = 0; x < width; x++) {
      var offsetXY = (offsetY + x) * 4;

      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];
      var rr = 0;
      var gg = 0;
      var bb = 0;
      var rrggbb = 0;

      var counter = 0;
      for (var yy = y - 1; yy <= y + 1; yy++){
        if (yy < 0 || height <= yy) {continue;}
        var offsetYY = yy * width;
        for (var xx = x - 1; xx <= x + 1; xx++){
          if (xx < 0 || width <= xx) {continue;}
          var offsetXXYY = (offsetYY + xx) * 4;
          rrggbb +=
              Math.abs(r - originalData[offsetXXYY + 0]) +
              Math.abs(g - originalData[offsetXXYY + 1]) +
              Math.abs(b - originalData[offsetXXYY + 2]) ;
          counter++;
        }
      }
      var rgb = rrggbb / counter > 32 ? rrggbb / counter : 0;
      modifyData[offsetXY + 0] =
      modifyData[offsetXY + 1] =
      modifyData[offsetXY + 2] = 255 - rgb;
      modifyData[offsetXY + 3] = 255;
    }
  }
  return modify;
};

var OutlinePatch = function(){};
OutlinePatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  for (var y = 0; y < height; y++) {
    var offsetY = y * width;
    for (var x = 0; x < width; x++) {
      var offsetXY = (offsetY + x) * 4;
      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];
      var rr = 0;
      var gg = 0;
      var bb = 0;
      var rrggbb = 0;

      var counter = 0;
      for (var yy = y - 1; yy <= y + 1; yy++){
        if (yy < 0 || height <= yy) {continue;}
        var offsetYY = yy * width;
        for (var xx = x - 1; xx <= x + 1; xx++){
          if (xx < 0 || width <= xx) {continue;}
          var offsetXXYY = (offsetYY + xx) * 4;
          rrggbb +=
              Math.abs(r - originalData[offsetXXYY + 0]) +
              Math.abs(g - originalData[offsetXXYY + 1]) +
              Math.abs(b - originalData[offsetXXYY + 2]) ;
          counter++;
        }
      }
      var rgb = rrggbb / counter;
      modifyData[offsetXY + 0] =
      modifyData[offsetXY + 1] =
      modifyData[offsetXY + 2] = rgb;
      modifyData[offsetXY + 3] = 255;
    }
  }
  return modify;
};

var ReversePatch = function(){};
ReversePatch.prototype.process = function(context, original){
  var modify = context.createImageData(original);
  var originalData = original.data;
  var modifyData   = modify.data;
  var width = original.width;
  var height = original.height;

  for (var y = 0; y < height; y++) {
    var offsetY = y * width;
    for (var x = 0; x < width; x++) {
      var offsetXY = (offsetY + x) * 4;

      var r = originalData[offsetXY + 0];
      var g = originalData[offsetXY + 1];
      var b = originalData[offsetXY + 2];

      modifyData[offsetXY + 0] = 255 - r;
      modifyData[offsetXY + 1] = 255 - b;
      modifyData[offsetXY + 2] = 255 - g;
      modifyData[offsetXY + 3] = 255;
    }
  }
  return modify;
};
