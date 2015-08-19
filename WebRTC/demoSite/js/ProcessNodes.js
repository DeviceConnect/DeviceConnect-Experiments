/**
 ProcessNodes.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var LowPassNode = function(audioContext,onSelect){
  this.context = audioContext;
  this.callback = onSelect;
  this.filter = undefined;
  this.frequencyValue = undefined;
  this.resonanceValue = undefined;
};
LowPassNode.prototype = {

  createSelectTag : function(){
    return $('<input>', {type: 'button', value: 'LowPass', on:{click:this.pressButton.bind(this)}});
	},

  pressButton : function(){
    this.callback(this);
  },

  setInput : function(input){
    this.disconnect();

    this.filter = this.context.createBiquadFilter();
    this.filter.type = 'lowpass';
    this.filter.frequency.value = 2000;
    this.filter.Q.value = 10;

    input.connect(this.filter);
  },

  setOutput : function(output){
    this.filter.connect(output);
  },

  needMix : function(){
    return false;
  },

  disconnect : function(){
    if(this.filter){
      this.filter.disconnect();
      this.filter = undefined;
    }
  },

  createOperationTag : function(){
    var titleWidth = 80;
    var valueWidth = 50;

    var frequencyTags = this.makeSliderTag('Frequency',this.onChangeFrequency.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:350,max:20000,step:100,value:2000});
    this.frequencyValue = frequencyTags.variety;

    var resonanceTags = this.makeSliderTag('Resonance',this.onChangeResonance.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:1,max:20,step:1,value:10});
    this.resonanceValue = resonanceTags.variety;

    return $("<div>",{height:50}).append(frequencyTags.root).append(resonanceTags.root);
	},

  onChangeFrequency : function(object){
    this.frequencyValue.text(object.target.value);
    this.filter.frequency.value = object.target.value;
  },

  onChangeResonance : function(object){
    this.resonanceValue.text(object.target.value);
    this.filter.Q.value = object.target.value;
  },

  makeSliderTag : function(text,callback,sizes){
    var rootTag = $('<div>',{width:300});
    rootTag.append($('<div>',{width:sizes.titleWidth,css:{float:"left"}}).text(text+':　'));
    varietyTag = $('<div>',{width:sizes.valueWidth,css:{float:"left"}});
    varietyTag.text(sizes.value);
    rootTag.append(varietyTag);
    rootTag.append($('<input>',{type:"range",css:{float:"left"},
      min:sizes.min,max:sizes.max,step:sizes.step,value:sizes.value,
      on:{input:callback.bind(this)}}));
    return {root:rootTag,variety:varietyTag};
  }

};

var HighPassNode = function(audioContext,onSelect){
  this.context = audioContext;
  this.callback = onSelect;
  this.filter = undefined;
  this.frequencyValue = undefined;
  this.resonanceValue = undefined;
};
HighPassNode.prototype = {

  createSelectTag : function(){
    return $('<input>', {type: 'button', value: 'HighPass', on:{click:this.pressButton.bind(this)}});
	},

  pressButton : function(){
    this.callback(this);
  },

  setInput : function(input){
    this.disconnect();

    this.filter = this.context.createBiquadFilter();
    this.filter.type = 'highpass';
    this.filter.frequency.value = 2000;
    this.filter.Q.value = 10;

    input.connect(this.filter);
  },

  setOutput : function(output){
    this.filter.connect(output);
  },

  needMix : function(){
    return false;
  },

  disconnect : function(){
    if(this.filter){
      this.filter.disconnect();
      this.filter = undefined;
    }
  },

  createOperationTag : function(){
    var titleWidth = 80;
    var valueWidth = 50;

    var frequencyTags = this.makeSliderTag('Frequency',this.onChangeFrequency.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:350,max:20000,step:100,value:2000});
    this.frequencyValue = frequencyTags.variety;

    var resonanceTags = this.makeSliderTag('Resonance',this.onChangeResonance.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:1,max:20,step:1,value:10});
    this.resonanceValue = resonanceTags.variety;

    return $("<div>",{height:50}).append(frequencyTags.root).append(resonanceTags.root);
	},

  onChangeFrequency : function(object){
    this.frequencyValue.text(object.target.value);
    this.filter.frequency.value = object.target.value;
  },

  onChangeResonance : function(object){
    this.resonanceValue.text(object.target.value);
    this.filter.Q.value = object.target.value;
  },

  makeSliderTag : function(text,callback,sizes){
    var rootTag = $('<div>',{width:300});
    rootTag.append($('<div>',{width:sizes.titleWidth,css:{float:"left"}}).text(text+':　'));
    varietyTag = $('<div>',{width:sizes.valueWidth,css:{float:"left"}});
    varietyTag.text(sizes.value);
    rootTag.append(varietyTag);
    rootTag.append($('<input>',{type:"range",css:{float:"left"},
      min:sizes.min,max:sizes.max,step:sizes.step,value:sizes.value,
      on:{input:callback.bind(this)}}));
    return {root:rootTag,variety:varietyTag};
  }

};

var PeakingNode = function(audioContext,onSelect){
  this.context = audioContext;
  this.callback = onSelect;
  this.filter = undefined;
  this.frequencyValue = undefined;
  this.widthValue = undefined;
  this.gainValue = undefined;
};
PeakingNode.prototype = {

  createSelectTag : function(){
    return $('<input>', {type: 'button', value: 'Peaking', on:{click:this.pressButton.bind(this)}});
	},

  pressButton : function(){
    this.callback(this);
  },

  setInput : function(input){
    this.disconnect();

    this.filter = this.context.createBiquadFilter();
    this.filter.type = 'peaking';
    this.filter.frequency.value = 2000;
    this.filter.Q.value = 5;
    this.filter.gain.value = 10;

    input.connect(this.filter);
  },

  setOutput : function(output){
    this.filter.connect(output);
  },

  needMix : function(){
    return false;
  },

  disconnect : function(){
    if(this.filter){
      this.filter.disconnect();
      this.filter = undefined;
    }
  },

  createOperationTag : function(){
    var titleWidth = 80;
    var valueWidth = 50;

    var frequencyTags = this.makeSliderTag('Frequency',this.onChangeFrequency.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:350,max:20000,step:100,value:2000});
    this.frequencyValue = frequencyTags.variety;

    var widthTags = this.makeSliderTag('Width',this.onChangeWidth.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:1,max:20,step:1,value:5});
    this.widthValue = widthTags.variety;

    var gainTags = this.makeSliderTag('Gain',this.onChangeGain.bind(this),
      {titleWidth:titleWidth,valueWidth:valueWidth,
        min:-40,max:40,step:1,value:10});
    this.gainValue = gainTags.variety;

    return $("<div>",{height:75}).append(frequencyTags.root).append(widthTags.root).append(gainTags.root);
	},

  onChangeFrequency : function(object){
    this.frequencyValue.text(object.target.value);
    this.filter.frequency.value = object.target.value;
  },

  onChangeWidth : function(object){
    this.widthValue.text(object.target.value);
    this.filter.Q.value = object.target.value;
  },

  onChangeGain : function(object){
    this.gainValue.text(object.target.value);
    this.filter.gain.value = object.target.value;
  },

  makeSliderTag : function(text,callback,sizes){
    var rootTag = $('<div>',{width:300});
    rootTag.append($('<div>',{width:sizes.titleWidth,css:{float:"left"}}).text(text+':　'));
    varietyTag = $('<div>',{width:sizes.valueWidth,css:{float:"left"}});
    varietyTag.text(sizes.value);
    rootTag.append(varietyTag);
    rootTag.append($('<input>',{type:"range",css:{float:"left"},
      min:sizes.min,max:sizes.max,step:sizes.step,value:sizes.value,
      on:{input:callback.bind(this)}}));
    return {root:rootTag,variety:varietyTag};
  }

};
