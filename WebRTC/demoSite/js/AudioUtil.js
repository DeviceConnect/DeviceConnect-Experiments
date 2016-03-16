var AudioUtil = AudioUtil || {};

(function(audio) {
    var ctx = new (window.AudioContext || window.webkitAudioContext);

    var AudioDevice = function() {
        this._initialDelaySec = 0;
        this._scheduledTime = 0;
        this._channel = undefined;
        this._sampleRate = undefined;
        this._audioFormat = undefined;
        this._url = undefined;
        this._ws = undefined;
    };

    AudioDevice.prototype.playChunk = function(audioSrc, scheduledTime) {
        if (audioSrc.start) {
            audioSrc.start(scheduledTime);
        } else {
            audioSrc.noteOn(scheduledTime);
        }
    };

    AudioDevice.prototype.playAudioStream = function(audioFloat32) {
        var audioBuf = ctx.createBuffer(this._channel, audioFloat32.length, this._sampleRate);
        var audioSrc = ctx.createBufferSource();
        var currentTime = ctx.currentTime;

        audioBuf.getChannelData(0).set(audioFloat32);

        audioSrc.buffer = audioBuf;
        audioSrc.connect(ctx.destination);

        if (currentTime < this._scheduledTime) {
            this.playChunk(audioSrc, this._scheduledTime);
            this._scheduledTime += audioBuf.duration;
        } else {
            this.playChunk(audioSrc, currentTime);
            this._scheduledTime = currentTime + audioBuf.duration + this._initialDelaySec;
        }
    };

    AudioDevice.prototype.channel = function(channel) {
        if (channel < 1 || channel > 2) {
            throw new Error("channel is invalid. channel=" + channel); 
        }
        this._channel = channel;
        return this;
    };

    AudioDevice.prototype.sampleRate = function(sampleRate) {
        if (sampleRate <= 0) {
            throw new Error("sampleRate is invalid. sampleRate=" + sampleRate); 
        }
        this._sampleRate = sampleRate;
        return this;
    };

    AudioDevice.prototype.audioFormat = function(audioFormat) {
        if (audioFormat <= 0 || audioFormat > 3) {
            throw new Error("audioFormat is invalid. audioFormat=" + audioFormat); 
        }
        this._audioFormat = audioFormat;
        return this;
    };
    
    AudioDevice.prototype.url = function(url) {
        this._url = url;
        return this;
    };

    AudioDevice.prototype.onopen = function(onopen) {
        this._onopen = onopen;
        return this;
    };

    AudioDevice.prototype.onerror = function(onerror) {
        this._onerror = onerror;
        return this;
    };

    AudioDevice.prototype.onclose = function(onclose) {
        this._onclose = onclose;
        return this;
    };

    AudioDevice.prototype.close = function() {
        if (this._ws) {
            this._ws.close(4500, "Finished the AudioDevice.");
        }
        this._ws = undefined;
    };

    AudioDevice.prototype.connect = function() {
        if (this._ws) {
            throw new Error("websocket is already opened."); 
        }

        if (this._url == undefined) {
            throw new Error("url is not set."); 
        }

        if (this._channel == undefined) {
            throw new Error("channel is not set."); 
        }

        if (this._sampleRate == undefined) {
            throw new Error("sampleRate is not set."); 
        }

        if (this._audioFormat == undefined) {
            throw new Error("audioFormat is not set."); 
        }

        var self = this;

        this._ws = new WebSocket(this._url);
        this._ws.binaryType = "arraybuffer";
        this._ws.onopen = function() {
            if (typeof(self._onopen) == 'function') {
                self._onopen();
            }
        };

        this._ws.onerror = function(e) {
            if (typeof(self._onerror) == 'function') {
                self._onerror();
            }
        };
        
        this._ws.onclose = function() {
            if (typeof(self._onclose) == 'function') {
                self._onclose();
            }
        };

        this._ws.onmessage = function(evt) {
            if (evt.data.constructor !== ArrayBuffer) {
                throw "expecting ArrayBuffer";
            }

            var audioFloat32;
            switch (self._audioFormat) {
            case audio.AudioDevice.PCM_8BIT: {
                var data = new Int8Array(evt.data);
                var len = data.length;
                audioFloat32 = new Float32Array(len);
                for (var i = 0; i < len; i++) {
                    audioFloat32[i] = data[i] / 256.0;
                }
            }   break;
            case audio.AudioDevice.PCM_16BIT: {
                var data = new Int16Array(evt.data);
                var len = data.length;
                audioFloat32 = new Float32Array(len);
                for (var i = 0; i < len; i++) {
                    audioFloat32[i] = data[i] / 32768.0;
                }
            }   break;
            case audio.AudioDevice.PCM_FLOAT:
                audioFloat32 = new Float32Array(evt.data);
                break;
            default:
                console.log("Unknown audioFormat: " + self._audioFormat);
                return;
            }

            self.playAudioStream(audioFloat32);
        };
    };

audio.AudioDevice = AudioDevice;
audio.AudioDevice.PCM_8BIT = 1;
audio.AudioDevice.PCM_16BIT = 2;
audio.AudioDevice.PCM_FLOAT = 3;

})(AudioUtil);
