package com.linjiu.recognize.domain.game.tower;

import com.linjiu.recognize.domain.game.tower.enemy.Enemy;
import com.linjiu.recognize.layout.program.game.tower.GameSurfaceView;

import java.util.List;

// 每一波敌人的数量管理
public class WaveManager {

    private GameSurfaceView gameView;

    private boolean waveInProgress = false;

    private int enemiesToSpawn = 0;

    private int enemiesSpawned = 0;

    private long lastSpawnTime = 0;

    private int spawnInterval = 1000; // ms

    public WaveManager(GameSurfaceView gameView) {
        this.gameView = gameView;
    }

    public void startWave(int waveNumber) {
        waveInProgress = true;
        enemiesToSpawn = 5 + waveNumber * 2;
        enemiesSpawned = 0;
        lastSpawnTime = System.currentTimeMillis();
    }

    public void update() {
        if (!waveInProgress) return;

        if (enemiesSpawned < enemiesToSpawn &&
                System.currentTimeMillis() - lastSpawnTime > spawnInterval) {
            spawnEnemy();
            enemiesSpawned++;
            lastSpawnTime = System.currentTimeMillis();
        }

        if (enemiesSpawned >= enemiesToSpawn && gameView.enemies.isEmpty()) {
            waveInProgress = false;
            gameView.startNextWave();
        }
    }

    // 产生敌人
    private void spawnEnemy() {
        List<android.graphics.Point> path = gameView.getPath();
        int health = 50 + gameView.getCurrentWave() * 10;
        int reward = 10 + gameView.getCurrentWave();
        float speed = 2f + gameView.getCurrentWave() * 0.2f;
        Enemy enemy = new Enemy(path, health, reward, speed);
        gameView.addEnemy(enemy);
    }

    public boolean isWaveInProgress() {
        return waveInProgress;
    }

    public void reset() {
        waveInProgress = false;
        enemiesSpawned = 0;
    }
}