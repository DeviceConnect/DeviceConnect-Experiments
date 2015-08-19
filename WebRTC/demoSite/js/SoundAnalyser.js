/**
 SoundAnalyser.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var SoundAnalyser = function(audioContext){
  this.context = audioContext;
  this.canvasContext = undefined;
  this.analyser = undefined;
  this.animId = undefined;
  this.canvasWidth = undefined;
  this.canvasHeight = undefined;
};
SoundAnalyser.prototype = {

  createTag : function(){
    this.analyser = this.context.createAnalyser();
    this.analyser.fftSize = 128;
    this.canvasWidth = 266;//NOTICE: hardcording
    this.canvasHeight = 64;//NOTICE: hardcording
		var canvas = $('<canvas>', {width: this.canvasWidth, height: this.canvasHeight});
    this.canvasContext = canvas[0].getContext('2d');
		return canvas;
	},

  getAnalyser : function(){
    return this.analyser;
  },

  start : function(){
    this.animId = requestAnimationFrame(
      this.sourceRender.bind(this)
    );
  },

  stop : function(){
    cancelAnimationFrame(this.animId);
    requestAnimationFrame(function(){
      this.canvasContext.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
    }.bind(this));
  },

  sourceRender : function(){
    var spectrums = new Uint8Array(this.analyser.frequencyBinCount);
    this.analyser.getByteFrequencyData(spectrums);
    this.canvasContext.clearRect(0, 0, 300, 150);
    var lineWidth = 1;
    var sample = Math.floor(spectrums.length / this.canvasWidth);//64
    var len = this.canvasWidth / 2;
    var value = 0;
    var s = Math.floor(300 / spectrums.length);
    for(var i = 0; i < 300; i++){
      this.canvasContext.beginPath();
      this.canvasContext.strokeStyle = '#25799f';
      this.canvasContext.moveTo(i,148);
      value = spectrums[Math.floor(i/s)]/4;
      this.canvasContext.lineTo(i,148 - value);
      this.canvasContext.lineWidth = lineWidth;
      this.canvasContext.stroke();
    }
    this.animId = requestAnimationFrame(this.sourceRender.bind(this));
  }

};
