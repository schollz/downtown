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

	// samples
	var <sample1;
	var  sample1Buffer;
	var <sample2;
	var  sample2Buffer;
	var <sample3;
	var  sample3Buffer;
	var <sample4;
	var  sample4Buffer;
	var <sample5;
	var  sample5Buffer;
	var <sample6;
	var  sample6Buffer;
	var <sample7;
	var  sample7Buffer;
	var <sample8;
	var  sample8Buffer;

	// Define a class method when an object is created
	*new { arg context, doneCallback;
		// Return the object from the superclass (CroneEngine) .new method
		^super.new(context, doneCallback);
	}
	// Rather than defining a SynthDef, use a shorthand to allocate a function and send it to the engine to play
	// Defined as an empty method in CroneEngine
	// https://github.com/monome/norns/blob/master/sc/core/CroneEngine.sc#L31
	alloc {

		sample1Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample1 = {
			arg amp=0.0, amplag=0.02, t_trig=0;
			PlayBuf.ar(2,sample1Buffer,BufRateScale.kr(sample1Buffer),trigger:t_trig,loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample2Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample2 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample2Buffer,BufRateScale.kr(sample2Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample3Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample3 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample3Buffer,BufRateScale.kr(sample3Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample4Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample4 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample4Buffer,BufRateScale.kr(sample4Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample5Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample5 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample5Buffer,BufRateScale.kr(sample5Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample6Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample6 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample6Buffer,BufRateScale.kr(sample6Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample7Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample7 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample7Buffer,BufRateScale.kr(sample7Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
		}.play(target: context.xg);

		sample8Buffer = Buffer.read(context.server,"/home/we/dust/code/downtown/samples/silence.wav");
		sample8 = {
			arg amp=0.0, amplag=0.02;
			PlayBuf.ar(2,sample8Buffer,BufRateScale.kr(sample8Buffer),loop:1)*Lag.ar(K2A.ar(amp), amplag)
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

		this.addCommand("powerline", "f", { arg msg;
			synthPower.set(\amp, msg[1]);
		});		

		this.addCommand("storm", "f", { arg msg;
			synthStorm.set(\amp, msg[1]);
		});

		this.addCommand("sample1","f", { arg msg;
			sample1.set(\amp, msg[1]);
		});

		this.addCommand("sample1file","s", { arg msg;
			sample1Buffer.free;
			sample1Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample1reset","f", { arg msg;
			sample1.set(\t_trig,-1);
			sample1.set(\t_trig,1);
		});

		this.addCommand("sample2","f", { arg msg;
			sample2.set(\amp, msg[1]);
		});

		this.addCommand("sample2file","s", { arg msg;
			sample2Buffer.free;
			sample2Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample3","f", { arg msg;
			sample3.set(\amp, msg[1]);
		});

		this.addCommand("sample3file","s", { arg msg;
			sample3Buffer.free;
			sample3Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample4","f", { arg msg;
			sample4.set(\amp, msg[1]);
		});

		this.addCommand("sample4file","s", { arg msg;
			sample4Buffer.free;
			sample4Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample5","f", { arg msg;
			sample5.set(\amp, msg[1]);
		});

		this.addCommand("sample5file","s", { arg msg;
			sample5Buffer.free;
			sample5Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample6","f", { arg msg;
			sample6.set(\amp, msg[1]);
		});

		this.addCommand("sample6file","s", { arg msg;
			sample6Buffer.free;
			sample6Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample7","f", { arg msg;
			sample7.set(\amp, msg[1]);
		});

		this.addCommand("sample7file","s", { arg msg;
			sample7Buffer.free;
			sample7Buffer = Buffer.read(context.server,msg[1]);
		});

		this.addCommand("sample8","f", { arg msg;
			sample8.set(\amp, msg[1]);
		});

		this.addCommand("sample8file","s", { arg msg;
			sample8Buffer.free;
			sample8Buffer = Buffer.read(context.server,msg[1]);
		});

	}
	// define a function that is called when the synth is shut down
	free {
		// synths
		synthBirds.free;
		synthBells.free;
		synthPulse.free;
		synthSnare.free;
		synthKick.free;
		synthPower.free;
		synthStorm.free;

		// sample buffers
		sample1.free;
		sample1Buffer.free;
		sample2.free;
		sample2Buffer.free;
		sample3.free;
		sample3Buffer.free;
		sample4.free;
		sample4Buffer.free;
		sample5.free;
		sample5Buffer.free;
		sample6.free;
		sample6Buffer.free;
		sample7.free;
		sample7Buffer.free;
		sample8.free;
		sample8Buffer.free;
	}
}
