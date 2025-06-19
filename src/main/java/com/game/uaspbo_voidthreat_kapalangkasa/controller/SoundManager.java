package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    // Instance variable to hold the currently playing background music clip.
    // This needs to be non-static to be controlled by a specific game instance.
    private Clip musicClip;

    /**
     * Plays a short, one-off sound effect. This is your original static method.
     * @param soundFile The name of the sound file in the assets folder.
     */
    public static void playSound(String soundFile) {
        // Using a new thread for each SFX can prevent lag in the main game loop.
        new Thread(() -> {
            try {
                URL soundURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + soundFile);
                if (soundURL == null) {
                    System.err.println("❌ Sound file not found: " + soundFile);
                    return;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                System.err.println("Error playing sound: " + soundFile);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Plays an introductory music file once, then switches to a looping music file.
     * @param introFile The music file to play once (e.g., "bgm2.wav").
     * @param loopFile The music file to play continuously afterwards (e.g., "bgm1.wav").
     */
    public void playIntroThenLoop(String introFile, String loopFile) {
        stopMusic(); // Ensure no other music is playing.

        try {
            URL introURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + introFile);
            if (introURL == null) {
                System.err.println("❌ Intro music file not found: " + introFile + ". Playing loop directly.");
                playLoopingMusic(loopFile); // Fallback to loop if intro is missing.
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(introURL);
            musicClip = AudioSystem.getClip();

            // Add a LineListener to be notified when the clip's state changes.
            musicClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    // This event fires when the clip finishes playing.
                    event.getLine().close();
                    playLoopingMusic(loopFile);
                }
            });

            musicClip.open(audioIn);
            musicClip.start(); // Play the intro clip once.

        } catch (Exception e) {
            System.err.println("❌ Error playing intro music: " + introFile);
            e.printStackTrace();
        }
    }

    /**
     * A private helper method to play the main looping background music.
     * @param loopFile The music file to play on a loop.
     */
    private void playLoopingMusic(String loopFile) {
        try {
            URL loopURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + loopFile);
            if (loopURL == null) {
                System.err.println("❌ Looping music file not found: " + loopFile);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(loopURL);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY); // Set the clip to loop forever.
            musicClip.start();

        } catch (Exception e) {
            System.err.println("❌ Error playing looping music: " + loopFile);
            e.printStackTrace();
        }
    }

    /**
     * Stops the currently playing background music and releases the clip's resources.
     */
    public void stopMusic() {
        if (musicClip != null && musicClip.isOpen()) {
            musicClip.stop();
            musicClip.close();
        }
    }
}