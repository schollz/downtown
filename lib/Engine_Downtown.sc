// Engine_Downtown
// sctweet loops
// find more at
// https://twitter.com/search?q=SinOsc%20(%23supercollider%20OR%20%23sc%20OR%20%23sctweet)&src=typed_query&f=live

// testing in supercollider ide
// ({
// 	var amp=0.4;
// 	var amplag = 0.02;
// 	var bpm=120;
// 		var amp_,  snd;
// 		amp_ = Lag.ar(K2A.ar(amp), amplag);
//      // <-- INSERT SOUND CODE HERE -->
//      // snd = ??????????
// 	[snd.scope,snd]
// }.play)

// Inherit methods from CroneEngine
Engine_Downtown : CroneEngine {
	// Define a getter for the synth variable
	var <synth1;
	var <synth2;
	var <synth3;
	var <synth4;
	var <synth5;
	var <synth6;

	// Define a class method when an object is created
	*new { arg context, doneCallback;
		// Return the object from the superclass (CroneEngine) .new method
		^super.new(context, doneCallback);
	}
	// Rather than defining a SynthDef, use a shorthand to allocate a function and send it to the engine to play
	// Defined as an empty method in CroneEngine
	// https://github.com/monome/norns/blob/master/sc/core/CroneEngine.sc#L31
	alloc {
		// birds, adapted from https://twitter.com/aucotsi/status/408981450994638848
		synth1 = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			snd = Limiter.ar(GVerb.ar(Formlet.ar(LFCub.ar(Convolution.ar(LinCongN.ar(5),LinCongN.ar(5))),LFCub.ar(Sweep.ar(LFCub.ar(1/9),3)).range(1999,3999),0.01,0.1),mul:10),0.02*amp_);

			snd
		}.play(target: context.xg);

		// bells, adapted from https://twitter.com/joshpar/status/100417407021092864
		synth2 = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			snd=SinOsc.ar(LFNoise0.ar(10).range(100,1e4),0,0.05)*Decay.kr(Dust.kr(1));
			snd=GVerb.ar(snd*LFNoise1.ar(32.703),299,10,0.2,0.5,50,0,0.2,0.9,mul:amp_);
			snd = HPF.ar(snd,20);

			snd
		}.play(target: context.xg);

		// bass, adapted from https://sccode.org/1-55m
		synth3 = {
			arg amp=0.0, amplag=0.02, bpm=120, midinote=24;
			var amp_, hz_, snd, pulse, bass, lfo;	
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			hz_ = midinote.midicps;

			pulse = Decay2.ar(Impulse.ar(bpm/60), 0.01, 1)*SinOsc.ar(hz_,mul:0.25);
			bass = Splay.ar(SinOscFB.ar(hz_, 1.5));

	        lfo = SinOsc.kr(SinOsc.kr(0.1,mul:0.05).abs,add:1);
			snd = Compander.ar(bass, pulse, 0.02, 1, 0.05, 0.01, 0.2);
	        snd = MoogFF.ar(Mix.new([snd*lfo,bass*(1-lfo)]),1300,0.5,mul:amp_);

			[snd,snd]
		}.play(target: context.xg);

		// drumbeat, adapted from https://twitter.com/aucotsi/status/400603496140906496
		synth4 = {
			arg amp=0.0, amplag=0.02, bpm=120, hz=1300;
			var amp_, hz_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			hz_ = Lag.ar(K2A.ar(hz), amplag);

			snd = IFFT(PV_BrickWall(FFT(Buffer.alloc(context.server,512),WhiteNoise.ar*Pulse.ar(4*(bpm/60),1e-4*TChoose.kr(SinOsc.kr(0.5),[0.25,0.5,1,2,3,4,5,6,7,8,9,10]))),SinOsc.ar(bpm/60/8,mul:0.05).abs+0.01));
	        snd = Slew.ar(snd,3000,1000,mul:amp_);
	        snd = Pan2.ar(snd,SinOsc.kr(bpm/60/8,mul:0.5));
	        snd = HPF.ar(snd,20);

	        snd
		}.play(target: context.xg);

		// kick, adapted from https://twitter.com/aucotsi/status/400603496140906496
		synth5 = {
			arg amp=0.0, amplag=0.02, bpm=120, hz=1300;
			var amp_, hz_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);

			snd = Limiter.ar(SinOsc.ar(9*Pulse.ar(bpm/60/4),0,Pulse.kr(bpm/60/4)),level:amp_);
	        snd = HPF.ar(snd,20);

			[snd,snd]
		}.play(target: context.xg);

		// bongo, adapted from https://twitter.com/awhillas/status/22165574690
		synth6 = {
			arg amp=0.0, amplag=0.02, bpm=120, hz=1300;
			var amp_, hz_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);

		    snd = Pan2.ar(
			Mix.new(
				Limiter.ar(
				SinOsc.ar(
					[50,80,120,40],
	                 0,
					EnvGen.kr(
						Env.perc(0.01,0.3),
						Impulse.kr([2,2,3,1.5]*bpm/60/2)
					)
				))
		    ),level:amp_);
	        snd = HPF.ar(snd,10);

			[snd,snd]
		}.play(target: context.xg);


		this.addCommand("bpm", "f", { arg msg;
			synth3.set(\bpm, msg[1]);
			synth4.set(\bpm, msg[1]);
			synth5.set(\bpm, msg[1]);
			synth6.set(\bpm, msg[1]);
		});

		this.addCommand("amp1", "f", { arg msg;
			synth1.set(\amp, msg[1]);
		});

		this.addCommand("amp2", "f", { arg msg;
			synth2.set(\amp, msg[1]);
		});

		this.addCommand("amp3", "f", { arg msg;
			synth3.set(\amp, msg[1]);
		});

		this.addCommand("midinote", "f", { arg msg;
			synth3.set(\midinote, msg[1]);
		});

		this.addCommand("amp4", "f", { arg msg;
			synth4.set(\amp, msg[1]);
		});

		this.addCommand("amp5", "f", { arg msg;
			synth5.set(\amp, msg[1]);
		});

		this.addCommand("amp6", "f", { arg msg;
			synth6.set(\amp, msg[1]);
		});
	}
	// define a function that is called when the synth is shut down
	free {
		synth1.free;
		synth2.free;
		synth3.free;
		synth4.free;
		synth5.free;
		synth6.free;
	}
}
