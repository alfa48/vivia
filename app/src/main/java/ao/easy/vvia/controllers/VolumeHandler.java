package ao.easy.vvia.controllers;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import ao.easy.vvia.interfaces.ResourceHandler;

public class VolumeHandler  implements ResourceHandler {
    @Override
    public void execute(Context context, double value) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = (int) ((value / 100.0) * max);
        Log.d("Action", "Setting volume. Value: " + value + ", Max: " + max + ", New Volume: " + newVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
    }
}
