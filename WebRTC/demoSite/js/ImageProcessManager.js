/**
 ImageProcessManager.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var ImageProcessManager = function(){
	this.originalContext = undefined;
	this.modifyContext = undefined;
	this.original = undefined;
	this.modify = undefined;
	this.video = undefined;
	this.mainProcessor = undefined;
	this.processors = {'none':new EmptyPatch()};
};
ImageProcessManager.prototype = {

  setup : function(video, original, modify){
		original.attr('width',original[0].clientWidth);
		original.attr('height',original[0].clientHeight);

		modify.attr('width',modify[0].clientWidth);
		modify.attr('height',modify[0].clientHeight);

		this.video = video[0];
		this.original = original[0];
		this.modify = modify[0];

		this.originalContext = this.original.getContext("2d");
		this.modifyContext   = this.modify.getContext("2d");
		this.video.addEventListener("playing", this.onPlayingVideo.bind(this), false);
		this.changeProcessor('none');
	},

	resize : function(){
		var original = $(this.original);
		original.attr('width',original[0].clientWidth);
		original.attr('height',original[0].clientHeight);

		var modify = $(this.modify);
		modify.attr('width',modify[0].clientWidth);
		modify.attr('height',modify[0].clientHeight);

		this.originalContext = this.original.getContext("2d");
		this.modifyContext   = this.modify.getContext("2d");

		var processors = $.map(this.processors, function( value, index ) { return value;});
		for (var i = 0, l = processors.length; i < l; i++) {
			if(typeof processors[i].clear === 'function'){
				processors[i].clear();
			}
		}
  },

	putProcessor : function(name, processor){
		this.processors[name] = processor;
	},

	changeProcessor : function(name){
	  this.mainProcessor = this.processors[name];
	},

	onPlayingVideo : function(e){
	  this.draw();
	},

  draw : function() {
		this.originalContext.clearRect(0,0,this.original.clientWidth,this.original.clientHeight);

		var sizes = this.calcDestinationSizes(this.original, this.video);

		this.originalContext.drawImage(this.video,
			0,0,this.video.videoWidth,this.video.videoHeight,
			sizes.x,sizes.y,sizes.width,sizes.height);

		try {
			var originalImageData = this.originalContext.getImageData(0, 0, this.original.clientWidth, this.original.clientHeight);
			var modifyImageData   = this.mainProcessor.process(this.originalContext, originalImageData, this.original);
			this.modifyContext.putImageData(modifyImageData, 0, 0);
		} catch(e) { console.log(e);}
	  if (!("requestAnimationFrame" in window)) {
	  	window.requestAnimationFrame = window.msRequestAnimationFrame ||
																		 window.webkitRequestAnimationFrame ||
	  																 window.oRequestAnimationFrame ||
	  															 	 window.mozCancelRequestAnimationFrame ;
	  }
		requestAnimationFrame(this.draw.bind(this));
	},

	calcDestinationSizes : function(canvas, video){
		var oAsp = canvas.clientWidth / canvas.clientHeight;
		var vAsp = video.videoWidth / video.videoHeight;
		var diff = oAsp - vAsp;

		var destX = 0;
		var destY = 0;
		var destWidth = 0;
		var destHeight = 0;
		if(diff < 0){
			destWidth = canvas.clientWidth;
			destHeight = destWidth / vAsp;
			destY = (canvas.clientHeight - destHeight) / 2;
		} else if(diff > 0){
			destHeight = canvas.clientHeight;
			destWidth = destHeight * vAsp;
			destX = (canvas.clientWidth - destWidth) / 2;
		} else {
			destWidth = canvas.clientWidth;
			destHeight = canvas.clientWidth;
		}
		return {x:destX,y:destY,width:destWidth,height:destHeight};
	},

	createSelectTag : function(){
    var keys = $.map(this.processors, function( value, index ) { return index;});
		var root = $('<div>');
		for (var i = 0, l = keys.length; i < l; i++) {
		    var elem = $('<input>',{type:'button', value : keys[i],on:{click:this.onSelectProcess.bind(this)}});
		    root.append(elem);
		}
		return root;
	},

	onSelectProcess : function(object){
		this.changeProcessor(object.target.value);
	}

};
