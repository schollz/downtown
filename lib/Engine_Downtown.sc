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
	var <synthBirds;
	var <synthBells;
	var <synthPulse;
	var <synthSnare;
	var <synthKick;
	var <synthPower;
	var <synthStorm;
	var <sampleBirds;
	var sampleBirdsBuffer;
	var <sampleSax;
	var sampleSaxBuffer;
	var <sampleWaves;
	var sampleWavesBuffer;
	var <sampleFootsteps;
	var sampleFootstepsBuffer;
	var <sampleBLM;
	var sampleBLMBuffer;
	var <sampleTinyWings;
	var sampleTinyWingsBuffer;

	// Define a class method when an object is created
	*new { arg context, doneCallback;
		// Return the object from the superclass (CroneEngine) .new method
		^super.new(context, doneCallback);
	}
	// Rather than defining a SynthDef, use a shorthand to allocate a function and send it to the engine to play
	// Defined as an empty method in CroneEngine
	// https://github.com/monome/norns/blob/master/sc/core/CroneEngine.sc#L31
	alloc {
		sampleTinyWingsBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleTinyWings.wav");
		sampleTinyWings = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleTinyWingsBuffer,BufRateScale.kr(sampleTinyWingsBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sampleBLMBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleBLM.wav");
		sampleBLM = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleBLMBuffer,BufRateScale.kr(sampleBLMBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sampleBirdsBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleBirds.wav");
		sampleBirds = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleBirdsBuffer,BufRateScale.kr(sampleBirdsBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sampleSaxBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleSax.wav");
		sampleSax = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleSaxBuffer,BufRateScale.kr(sampleSaxBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sampleWavesBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleWaves.wav");
		sampleWaves = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleWavesBuffer,BufRateScale.kr(sampleWavesBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);


		sampleFootstepsBuffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/sampleFootsteps.wav");
		sampleFootsteps = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sampleFootstepsBuffer,BufRateScale.kr(sampleFootstepsBuffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);


		// birds, adapted from https://twitter.com/aucotsi/status/408981450994638848
		synthBirds = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			snd = Limiter.ar(GVerb.ar(Formlet.ar(LFCub.ar(Convolution.ar(LinCongN.ar(9),LinCongN.ar(3))),LFCub.kr(Sweep.kr(LFCub.kr(TChoose.kr(SinOsc.kr(1),[1/9,1/2,1,1/11,1/5])),4)).range(1500,3999),0.01,0.1),mul:10),SinOsc.kr(SinOsc.kr(rrand(0.2,1),mul:0.1,add:0.1),mul:0.02,add:0.021)*amp_);
			snd
		}.play(target: context.xg);

		// bells, adapted from https://twitter.com/joshpar/status/100417407021092864
		synthBells = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
			snd=SinOsc.ar(LFNoise0.ar(10).range(100,1e4),0,0.05)*Decay.kr(Dust.kr(1));
			snd=GVerb.ar(snd*LFNoise1.ar(32.703),299,10,0.2,0.5,50,0,0.2,0.9);
			snd = HPF.ar(snd,20,mul:amp_);

			snd
		}.play(target: context.xg);

		// pulse, adapted from https://sccode.org/1-55m
		synthPulse = {
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
		synthSnare = {
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
		synthKick = {
			arg amp=0.0, amplag=0.02, bpm=120, hz=1300;
			var amp_, hz_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);

			snd = Limiter.ar(SinOsc.ar(9*Pulse.ar(bpm/60/4),0,Pulse.kr(bpm/60/4)),level:amp_);
	        snd = HPF.ar(snd,20);

			[snd,snd]
		}.play(target: context.xg);

		// // bongo, adapted from https://twitter.com/awhillas/status/22165574690
		// synth6 = {
		// 	arg amp=0.0, amplag=0.02, bpm=120, hz=1300;
		// 	var amp_, hz_, snd;
		// 	amp_ = Lag.ar(K2A.ar(amp), amplag);

		//     snd = Pan2.ar(
		// 	Mix.new(
		// 		Limiter.ar(
		// 		SinOsc.ar(
		// 			[50,80,120,40],
	 //                 0,
		// 			EnvGen.kr(
		// 				Env.perc(0.01,0.3),
		// 				Impulse.kr([2,2,3,1.5]*bpm/60/2)
		// 			)
		// 		))
		//     ),level:amp_);
	 //        snd = HPF.ar(snd,10);

		// 	[snd,snd]
		// }.play(target: context.xg);


		// powerlines adapted from sccode https://sccode.org/1-4VU
		synthPower = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);

		    snd = Saw.ar(50)*(LFNoise0.kr(50)>0)*SinOsc.kr(0.1, 0, 0.4, 0.45);
			snd = FreeVerb.ar(snd, SinOsc.kr(0.01, 0, 0.4, 0.5),0.9,0.6,mul:amp_);

			[snd,snd]
		}.play(target: context.xg);

		// storm adapted from https://sccode.org/1-e
		synthStorm = {
			arg amp=0.0, amplag=0.02;
			var amp_, snd;
			amp_ = Lag.ar(K2A.ar(amp), amplag);
		 	snd = Limiter.ar(
		 			Mix.new([
		 	        tanh(
		 	            GVerb.ar(
		 	                HPF.ar(
		 	                    PinkNoise.ar(0.08+LFNoise1.kr(0.3,0.02))+LPF.ar(Dust2.ar(LFNoise1.kr(0.2).range(40,50)),7000),
		 	                    400
		 	                ),
		 	                250,100,0.25,drylevel:0.3
		 	            ) * 0.6 * Line.kr(0,0.6,10)
		 	        ), GVerb.ar(
		 	                LPF.ar(
		 	                    10 * HPF.ar(PinkNoise.ar(LFNoise1.kr(3).clip(0,1)*LFNoise1.kr(2).clip(0,1) ** 1.8), 20)
		 	                    ,LFNoise1.kr(1).exprange(100,2500)
		 	                ).tanh,
		 	               270,30,0.7,drylevel:0.5
		 			) * 1.0 * SinOsc.kr(0.1,mul:3).tanh.abs * Line.kr(0,1.0,30)
		 			])
		 	    ,level:0.5) * amp_;
			snd
		}.play(target: context.xg);


		this.addCommand("bpm", "f", { arg msg;
			synthPulse.set(\bpm, msg[1]);
			synthKick.set(\bpm, msg[1]);
			synthSnare.set(\bpm, msg[1]);
		});

		this.addCommand("birds", "f", { arg msg;
			synthBirds.set(\amp, msg[1]);
		});

		this.addCommand("bells", "f", { arg msg;
			synthBells.set(\amp, msg[1]);
		});

		this.addCommand("pulse", "f", { arg msg;
			synthPulse.set(\amp, msg[1]);
		});

		this.addCommand("pulsenote", "f", { arg msg;
			synthPulse.set(\midinote, msg[1]);
		});

		this.addCommand("kick", "f", { arg msg;
			synthKick.set(\amp, msg[1]);
		});

		this.addCommand("snare", "f", { arg msg;
			synthSnare.set(\amp, msg[1]);
		});

		this.addCommand("power", "f", { arg msg;
			synthPower.set(\amp, msg[1]);
		});

		this.addCommand("power", "f", { arg msg;
			synthPower.set(\amp, msg[1]);
		});		

		this.addCommand("storm", "f", { arg msg;
			synthStorm.set(\amp, msg[1]);
		});

		this.addCommand("sampleBirds","f", { arg msg;
			sampleBirds.set(\amp, msg[1]);
		});

		this.addCommand("sampleSax","f", { arg msg;
			sampleSax.set(\amp, msg[1]);
		});

		this.addCommand("sampleWaves","f", { arg msg;
			sampleWaves.set(\amp, msg[1]);
		});

		this.addCommand("sampleFootsteps","f", { arg msg;
			sampleFootsteps.set(\amp, msg[1]);
		});

		this.addCommand("sampleBLM","f", { arg msg;
			sampleBLM.set(\amp, msg[1]);
		});

		this.addCommand("sampleTinyWings","f", { arg msg;
			sampleTinyWings.set(\amp, msg[1]);
		});




	}
	// define a function that is called when the synth is shut down
	free {
		synthBirds.free;
		synthBells.free;
		synthPulse.free;
		synthSnare.free;
		synthKick.free;
		synthPower.free;
		synthStorm.free;
		sampleBirds.free;
		sampleBirdsBuffer.free;
		sampleSax.free;
		sampleSaxBuffer.free;
		sampleWaves.free;
		sampleWavesBuffer.free;
		sampleFootsteps.free;
		sampleFootstepsBuffer.free;
		sampleBLM.free;
		sampleBLMBuffer.free;
		sampleTinyWings.free;
		sampleTinyWingsBuffer.free;
	}
}
