package ao.easy.vvia.controllers;

import android.content.Context;
import android.media.AudioManager;

import ao.easy.vvia.interfaces.ResourceHandler;

public class VolumeHandler  implements ResourceHandler {
    @Override
    public void execute(Context context, int value) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = (int) ((value / 100.0) * max);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
    }
}
