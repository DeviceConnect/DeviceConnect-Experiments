/**
 WebRTCManager.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var WebRTCManager = function(callback){
  this.callback = callback;
  this.peer = undefined;
  this.call = undefined;
};
WebRTCManager.prototype = {

  connectServer : function(apikey){
    var peer = new Peer({ key: apikey, debug: 3});
    peer.options.turn = false;
    peer.on('open', function(){
      this.callback.onopen(this.peer.id);
    }.bind(this));
    peer.on('call', function(call){
      this.call = call;
      this.callback.incoming(call.peer);
    }.bind(this));
    peer.on('close', function() {
      this.callback.onclose();
    }.bind(this));
    this.peer = peer;
  },

  getPeers : function(){
    if(typeof this.peer === 'undefined'){
      return;
    }
    this.peer.listAllPeers(function(list){
      this.callback.ongetpeerids(list);
    }.bind(this));
  },

  startCall : function(callee_id, stream){
    if(typeof this.peer === 'undefined'){
      return;
    }
    console.log(stream);
    this.call = this.peer.call(callee_id, stream);
    this.waitGetStream();
  },

  answer : function(stream){
    if(typeof this.call === 'undefined'){
      return;
    }
    this.call.answer(stream);
    this.waitGetStream();
  },

  stop : function(){
    if(typeof this.call === 'undefined'){
      return;
    }
    this.call.close();
    this.call = undefined;
  },

  waitGetStream : function(){
    this.call.on('stream', function(stream){
      this.callback.ongetstream(this.call.peer, stream);
    }.bind(this));
  },

  destroy : function(){
    if(typeof this.peer === 'undefined'){
      return;
    }
    this.peer.disconnect();
    this.peer.destroy();
    this.peer = undefined;
  }
};
