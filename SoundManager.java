package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    private Clip musicClip;
    private long pausedPosition;
    private boolean isCurrentlyLooping;

    public static void playSound(String soundFile) {
        // Thread untuk mencegah lag
        new Thread(() -> {
            try {
                URL soundURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + soundFile);
                if (soundURL == null) {
                    System.err.println("Suara tidak ditemukan" + soundFile);
                    return;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                System.err.println("Gagal memainkan suara " + soundFile);
                e.printStackTrace();
            }
        }).start();
    }

    public void playIntroThenLoop(String introFile, String loopFile) {
        stopMusic();

        try {
            this.isCurrentlyLooping = false;
            URL introURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + introFile);
            if (introURL == null) {
                System.err.println("Musik tidak ditemukan" + introFile);
                playLoopingMusic(loopFile);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(introURL);
            musicClip = AudioSystem.getClip();

            musicClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                    playLoopingMusic(loopFile);
                }
            });

            musicClip.open(audioIn);
            musicClip.start();

        } catch (Exception e) {
            System.err.println("Gagal memainkan musik" + introFile);
            e.printStackTrace();
        }
    }

    private void playLoopingMusic(String loopFile) {
        try {
            this.isCurrentlyLooping = true;
            URL loopURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + loopFile);
            if (loopURL == null) {
                System.err.println("Musik Looping tidak ditemukan " + loopFile);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(loopURL);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();

        } catch (Exception e) {
            System.err.println("Gagal memainkan musik looping" + loopFile);
            e.printStackTrace();
        }
    }

    public void pauseMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            this.pausedPosition = musicClip.getMicrosecondPosition();
            musicClip.stop();
            System.out.println("Music paused at " + this.pausedPosition);
        }
    }

    public void resumeMusic() {
        if (musicClip != null && !musicClip.isRunning()) {
            musicClip.setMicrosecondPosition(this.pausedPosition);

            if (isCurrentlyLooping) {
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                musicClip.start();
            }
            System.out.println("Music resumed.");
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            if (musicClip.isOpen()) musicClip.close();
        }
    }
}