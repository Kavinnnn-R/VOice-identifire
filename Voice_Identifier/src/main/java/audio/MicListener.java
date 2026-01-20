package audio;

import javax.sound.sampled.LineUnavailableException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import features.MFCCExtractor;
import model.SpeakerMatcher;

public class MicListener {

    private AudioDispatcher dispatcher;
    private Thread audioThread;

    public synchronized void startListening() {

        // Prevent starting twice
        if (dispatcher != null) {
            System.out.println("Already listening...");
            return;
        }

        try {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                    16000, 1024, 512
            );
        } catch (LineUnavailableException e) {
            System.err.println("Microphone not available");
            e.printStackTrace();
            return;
        }

        MFCC mfcc = new MFCC(
                1024,     // buffer size
                16000,    // sample rate
                13,       // MFCC coefficients
                30,       // mel filters
                300,      // min freq
                8000      // max freq
        );

        dispatcher.addAudioProcessor(mfcc);

        dispatcher.addAudioProcessor(new AudioProcessor() {

            @Override
            public boolean process(AudioEvent audioEvent) {

                float[] mfccFeatures = mfcc.getMFCC();

                // Debug: confirm live audio input
                if (mfccFeatures != null && mfccFeatures.length > 0) {
                    System.out.println("Voice detected | MFCC[0]: " + mfccFeatures[0]);
                }

                MFCCExtractor.process(mfccFeatures);
                SpeakerMatcher.match(mfccFeatures);

                return true;
            }

            @Override
            public void processingFinished() {
                System.out.println("Microphone stopped.");
            }
        });

        audioThread = new Thread(dispatcher, "Audio Dispatcher Thread");
        audioThread.start();

        System.out.println("Listening...");
    }

    public synchronized void stopListening() {
        if (dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
            audioThread = null;
            System.out.println("Stopped listening.");
        }
    }
}
