package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    public static void playSound(String soundFile) {
        try {
            URL soundURL = SoundManager.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/" + soundFile);
            System.out.println("Sound URL: " + soundURL);

            if (soundURL == null) {
                System.err.println("❌ Sound file not found: " + soundFile);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();

            System.out.println("✅ Sound played: " + soundFile);

        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ Unsupported audio file: " + soundFile);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("❌ Audio line unavailable for: " + soundFile);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("❌ IO error loading: " + soundFile);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ General error playing sound: " + soundFile);
            e.printStackTrace();
        }
    }
}
