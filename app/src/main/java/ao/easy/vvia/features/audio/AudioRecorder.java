package ao.easy.vvia.features.audio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioRecorder {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final Context context;
    private final RecordingListener listener;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File wavFile;

    public interface RecordingListener {
        void onRecordingStarted(File file);
        void onRecordingStopped(File file);
        void onError(String message);
    }

    public AudioRecorder(Context context, RecordingListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /** ✅ Verifica e pede permissão */
    public boolean checkPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    /** 🎙 Inicia gravação PCM e salva como WAV */
    public void startRecording() {
        if (isRecording) {
            listener.onError("Já está gravando");
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        wavFile = new File(context.getFilesDir(), "gravacao_" + System.currentTimeMillis() + ".wav");

        isRecording = true;
        new Thread(() -> writeAudioDataToFile(bufferSize)).start();

        new Handler(Looper.getMainLooper()).post(() -> listener.onRecordingStarted(wavFile));
    }

    /** ⏹️ Para gravação */
    public void stopRecording() {
        if (!isRecording) {
            listener.onError("Nenhuma gravação em andamento");
            return;
        }

        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        new Handler(Looper.getMainLooper()).post(() -> listener.onRecordingStopped(wavFile));
    }

    private void writeAudioDataToFile(int bufferSize) {
        try (FileOutputStream fos = new FileOutputStream(wavFile)) {
            // Cabeçalho WAV temporário
            writeWavHeader(fos, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();

            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) fos.write(buffer, 0, read);
            }

            // Atualiza cabeçalho com tamanho real
            long totalAudioLen = wavFile.length() - 44;
            long totalDataLen = totalAudioLen + 36;
            updateWavHeader(wavFile, totalAudioLen, totalDataLen);

            Log.d("AudioRecorder", "Gravação WAV salva em: " + wavFile.getAbsolutePath());
        } catch (IOException e) {
            listener.onError("Erro ao gravar áudio: " + e.getMessage());
        }
    }

    /** Cria cabeçalho WAV padrão PCM 16-bit mono */
    private void writeWavHeader(FileOutputStream out, int sampleRate, int channelConfig, int audioFormat) throws IOException {
        int channels = (channelConfig == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2;
        int bitsPerSample = (audioFormat == AudioFormat.ENCODING_PCM_16BIT) ? 16 : 8;
        byte[] header = new byte[44];

        long byteRate = sampleRate * channels * bitsPerSample / 8;

        // RIFF/WAVE header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = header[5] = header[6] = header[7] = 0; // tamanho depois
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0; // PCM
        header[20] = 1; header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * bitsPerSample / 8);
        header[33] = 0;
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = header[41] = header[42] = header[43] = 0;

        out.write(header, 0, 44);
    }

    /** Atualiza cabeçalho WAV com o tamanho real do arquivo */
    private void updateWavHeader(File wavFile, long totalAudioLen, long totalDataLen) throws IOException {
        try (FileOutputStream raf = new FileOutputStream(wavFile, true)) {
            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // Atualiza tamanho total e chunk de dados
        }
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(wavFile, "rw")) {
            raf.seek(4);
            raf.writeInt((int) totalDataLen);
            raf.seek(40);
            raf.writeInt((int) totalAudioLen);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public File getRecordedFile() {
        return wavFile;
    }
}
