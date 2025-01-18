// AudioPlayer.java

package application;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AudioPlayer {

    private MediaPlayer backgroundMusic;
    private MediaPlayer enemyDeathSound;
    private MediaPlayer playerDeathSound;

    public AudioPlayer() {
        // Extract and load audio files
        loadBackgroundMusic("/sprites/ost1.mp3");
        loadSoundEffects();
    }

    // Extract a resource file to the system's temporary directory
    private File extractResource(String resourcePath) throws Exception {
        InputStream resourceStream = getClass().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new Exception("Resource not found: " + resourcePath);
        }
        // Create a temporary file
        File tempFile = File.createTempFile("audio", ".tmp");
        tempFile.deleteOnExit(); // Automatically delete the file on exit
        Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    // Load and initialize background music
    public void loadBackgroundMusic(String filePath) {
        try {
            File audioFile = extractResource(filePath); // Extract to temp file
            Media media = new Media(audioFile.toURI().toString()); // Load as a Media object
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Loop the music
        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
        }
    }

    // Load sound effects for enemy death and player death
    private void loadSoundEffects() {
        try {
            // Load enemy death sound
            File enemyDeathFile = extractResource("/sprites/enemydeath.mp3");
            Media enemyDeathMedia = new Media(enemyDeathFile.toURI().toString());
            enemyDeathSound = new MediaPlayer(enemyDeathMedia);

            // Load player death sound
            File playerDeathFile = extractResource("/sprites/death.mp3");
            Media playerDeathMedia = new Media(playerDeathFile.toURI().toString());
            playerDeathSound = new MediaPlayer(playerDeathMedia);

        } catch (Exception e) {
            System.out.println("Error loading sound effects: " + e.getMessage());
        }
    }

    // Play background music
    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    // Stop background music
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    // Play enemy death sound
    public void playEnemyDeathSound() {
        if (enemyDeathSound != null) {
            enemyDeathSound.stop(); // Stop any currently playing sound
            enemyDeathSound.play();
        }
    }

    // Play player death sound
    public void playPlayerDeathSound() {
        if (playerDeathSound != null) {
            playerDeathSound.stop(); // Stop any currently playing sound
            playerDeathSound.play();
        }
    }

    // Set volume for background music
    public void setVolume(double volume) {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
        }
    }
}
