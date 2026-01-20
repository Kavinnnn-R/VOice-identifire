package features;

public class MFCCExtractor {

    public static void process(float[] mfcc) {
        if (mfcc != null && mfcc.length > 0) {
            System.out.println("MFCC frame received");
        }
    }
}
