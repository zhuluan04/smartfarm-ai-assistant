package com.linjiu.recognize.layout.program.game.tower;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.linjiu.recognize.domain.game.tower.tower.ArrowTower;
import com.linjiu.recognize.domain.game.tower.tower.CannonTower;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;
import com.linjiu.recognize.domain.game.tower.tower.MagicTower;
import com.linjiu.recognize.domain.game.tower.Particle;
import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.tower.Tower;
import com.linjiu.recognize.domain.game.tower.tower.TowerType;
import com.linjiu.recognize.domain.game.tower.WaveManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // 回调接口
    private GameCallback gameCallback;

    private SurfaceHolder holder;

    private Thread gameThread;

    private volatile boolean running = false;

    private Paint paint;

    private Random random = new Random();

    // 游戏状态
    private int playerLives = 20;

    private int playerGold = 100;

    private int currentWave = 1;

    private int maxWaves = 10;

    // 游戏对象
    public List<Enemy> enemies = new ArrayList<>();

    private List<Tower> towers = new ArrayList<>();

    private List<Projectile> projectiles = new ArrayList<>();

    private List<Particle> particles = new ArrayList<>();

    // 路径点（敌人移动路径）
    private List<Point> path = new ArrayList<>();

    // 塔建造模式
    private boolean isBuildingMode = false;

    private TowerType buildingTowerType = null;

    // 波次管理器
    private WaveManager waveManager;

    // 触摸坐标
    private float lastTouchX, lastTouchY;


    // 修改构造函数，接收回调
    public GameSurfaceView(Context context, AttributeSet attrs, GameCallback callback) {
        super(context, attrs);
        this.gameCallback = callback;
        init();
    }

    public interface OnTowerPlaceListener {
        void onTowerPlaceRequest(int x, int y);
    }

    private OnTowerPlaceListener towerPlaceListener;

    public void setOnTowerPlaceListener(OnTowerPlaceListener listener) {
        this.towerPlaceListener = listener;
    }


    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 初始化方法
    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);

        // 初始化路径（Z字形）
        int w = getWidth();
        int h = getHeight();
        if (w == 0) w = 1080;
        if (h == 0) h = 1920;

        path.add(new Point(0, h / 4));
        path.add(new Point(w * 3 / 4, h / 4));
        path.add(new Point(w * 3 / 4, h * 3 / 4));
        path.add(new Point(0, h * 3 / 4));
        path.add(new Point(0, h)); // 终点

        waveManager = new WaveManager(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 更新路径（适配屏幕）
        path.clear();
        path.add(new Point(0, height / 4));
        path.add(new Point(width * 3 / 4, height / 4));
        path.add(new Point(width * 3 / 4, height * 3 / 4));
        path.add(new Point(0, height * 3 / 4));
        path.add(new Point(0, height));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000D / 60D; // 60 FPS
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            if (delta >= 1) {
                update();
                render();
                delta--;
            }
        }
    }

    // 更新游戏逻辑
    private void update() {
        if (waveManager.isWaveInProgress()) {
            waveManager.update();
        }

        // 更新敌人
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            // 到达终点
            if (e.hasReachedEnd()) {
                playerLives--;
                enemies.remove(i);
                if (playerLives <= 0) {
                    gameOver();
                }
            }
        }

        // 更新塔
        for (Tower tower : towers) {
            tower.update(enemies);
        }

        // 更新子弹
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update();

            // 碰撞检测
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (p.collidesWith(e)) {
                    e.takeDamage(p.getDamage());
                    projectiles.remove(i);
                    createExplosion(p.x, p.y);
                    if (e.isDead()) {
                        playerGold += e.getReward();
                        enemies.remove(j);
                    }
                    break;
                }
            }

            // 移除超界子弹
            if (p.isOutOfBound(getWidth(), getHeight())) {
                projectiles.remove(i);
            }
        }

        // 更新粒子
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) particles.remove(i);
        }
    }

    // 渲染当前游戏物体方法
    private void render() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);

                // 绘制路径
                paint.setColor(Color.GRAY);
                paint.setStrokeWidth(8);
                for (int i = 0; i < path.size() - 1; i++) {
                    Point p1 = path.get(i);
                    Point p2 = path.get(i + 1);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                }

                // 绘制敌人
                for (Enemy enemy : enemies) {
                    enemy.draw(canvas, paint);
                }

                // 绘制塔
                for (Tower tower : towers) {
                    tower.draw(canvas, paint);
                }

                // 绘制子弹
                for (Projectile projectile : projectiles) {
                    projectile.draw(canvas, paint);
                }

                // 绘制粒子
                for (Particle particle : particles) {
                    particle.draw(canvas, paint);
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // 处理触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;
                handleTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                // 可用于拖动视角（未来扩展）
                break;
        }
        return true;
    }

    // 处理触摸事件 -> 建造塔
    private void handleTouch(float x, float y) {
        if (isBuildingMode) {
            // 建造模式：尝试放置塔
            if (canPlaceTowerHere((int)x, (int)y)) {
                Tower newTower = createTower(buildingTowerType, (int)x, (int)y);
                if (playerGold >= newTower.getCost()) {
                    playerGold -= newTower.getCost();
                    towers.add(newTower);
                    exitBuildingMode();
                }
            }
        } else {
            boolean clickedTower = false;

            // 先尝试选中塔进行升级
            for (Tower tower : towers) {
                if (tower.contains(x, y)) {
                    clickedTower = true;
                    if (tower.canUpgrade() && playerGold >= tower.getUpgradeCost()) {
                        playerGold -= tower.getUpgradeCost();
                        tower.upgrade();
                    }
                    break;
                }
            }

            // 如果没点到塔，才触发建造请求
            if (!clickedTower && towerPlaceListener != null) {
                towerPlaceListener.onTowerPlaceRequest((int)x, (int)y);
            }
        }
    }


    // 建造塔防
    public void buildTower(TowerType type, int x, int y) {
        if (!canPlaceTowerHere(x, y)) return;
        Tower newTower = createTower(type, x, y);
        if (playerGold >= newTower.getCost()) {
            playerGold -= newTower.getCost();
            towers.add(newTower);
        }
    }


    // 检查是否离路径太近（防止堵路）
    private boolean canPlaceTowerHere(int x, int y) {
        // 检查是否离路径太近（防止堵路）
        for (Point p : path) {
            if (Math.hypot(x - p.x, y - p.y) < 100) {
                return false;
            }
        }
        // 检查是否与其他塔重叠
        for (Tower t : towers) {
            if (Math.hypot(x - t.getX(), y - t.getY()) < 80) {
                return false;
            }
        }
        return true;
    }

    // 绘制塔防
    private Tower createTower(TowerType type, int x, int y) {
        switch (type) {
            case ARROW:
                return new ArrowTower(x, y);
            case CANNON:
                return new CannonTower(x, y);
            case MAGIC:
                return new MagicTower(x, y);
            default:
                return new ArrowTower(x, y);
        }
    }

    // 塔防建造模式
    public void enterBuildingMode(TowerType type) {
        this.isBuildingMode = true;
        this.buildingTowerType = type;
    }

    // 离开塔防建造模式
    public void exitBuildingMode() {
        this.isBuildingMode = false;
        this.buildingTowerType = null;
    }

    public void createExplosion(float x, float y) {
        for (int i = 0; i < 10; i++) {
            particles.add(new Particle(x, y));
        }
    }

    public interface GameCallback {
        void showGameOver();
        void showVictory();
    }

    public void startNextWave() {
        if (currentWave <= maxWaves) {
            waveManager.startWave(currentWave);
            currentWave++;
        } else {
            // 胜利！
//            ((GameTowerDefenseFragment) getContext()).showVictory();
        }
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public int getPlayerLives() { return playerLives; }

    public int getPlayerGold() { return playerGold; }

    public int getCurrentWave() { return currentWave; }

    public int getMaxWaves() { return maxWaves; }

    private void gameOver() {
        running = false;
//        ((GameTowerDefenseFragment) getContext()).showGameOver();
    }

    public void resetGame() {
        playerLives = 20;
        playerGold = 100;
        currentWave = 1;
        enemies.clear();
        towers.clear();
        projectiles.clear();
        particles.clear();
        running = true;
        waveManager.reset();
    }

    public List<Point> getPath() {
        return path;
    }
}