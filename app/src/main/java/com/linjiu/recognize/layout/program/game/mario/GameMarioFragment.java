package com.linjiu.recognize.layout.program.game.mario;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMarioFragment extends Fragment {
    private static final String TAG = "GameMarioFragment";

    // ---------------------------------- mario ---------------------------------------

    // 当前mario运动
    private AnimationDrawable currentMarioWalkAnimation;

    // 马里奥站 still
    private Drawable standDrawable;

    // mario是否运动
    private boolean isMarioWalking = false;

    // mario运动方向
    private JoystickView.Direction marioLastDirection = JoystickView.Direction.CENTER;

    private AnimationDrawable attackAnimation;

    private boolean isMarioAttacking = false; // 防止连点打断

    // 主角
    private ImageView mario;

    // mario血条hud
    private ProgressBar marioHpBar;

    private float marioSpeed = 8.0f; // 速度调大点，手感更好

    // mario血量
    private int marioHp = 100;

    // Mario 受伤 & 死亡
    private AnimationDrawable marioHurtAnimation;

    private AnimationDrawable marioDieAnimation;

    // mario是否受伤
    private boolean isMarioHurt = false;

    // mario是否死亡
    private boolean isMarioDead = false;

    // 马里奥无敌
    private boolean isMarioInvincible = false; // 短暂无敌

    // 缓存屏幕宽高
    private int screenWidth, screenHeight;

    private int marioWidth, marioHeight;

    private MediaPlayer bgmPlayer; // 背景音乐播放器

    private boolean isBgmPlaying = false; // 是否正在播放

    private boolean isMuted = false;      // 是否静音（新增）

    private Handler goblinAIHandler;

    private Runnable goblinAIRunnable;

    // goblin攻击频率
    private static final long GOBLIN_UPDATE_INTERVAL = 50; // 50ms更新一次，约20FPS

    // 攻击触发距离
    private static final float ATTACK_DISTANCE = 150f;     // 攻击触发距离（像素）

    // 碰撞距离（新增）
    private static final float COLLISION_DISTANCE = 80f;   // 角色碰撞距离

    // 推开力度（新增）
    private static final float PUSH_FORCE = 15f;

    // ---------------------------------- goblin ---------------------------------------

    // 哥布林
    private ImageView goblin;

    // goblin血条hud
    private ProgressBar goblinHpBar;

    private Drawable goblinStandDrawable;

    // goblin攻击动画
    private AnimationDrawable goblinAttackAnimation;

    // goblin是否walk
    private boolean isGoblinWalking = false;

    // goblin是否攻击
    private boolean isGoblinAttacking = false;

    // goblin的运动方向
    private JoystickView.Direction goblinLastDirection = JoystickView.Direction.DOWN; // 默认朝下

    // goblin宽高
    private int goblinWidth, goblinHeight;

    private float goblinSpeed = 6.0f; // 比马里奥慢一点

    // goblin的血条值
    private int goblinHp = 100;

    // goblin是否受伤
    private boolean isGoblinHurt = false;

    // goblin是否死亡
    private boolean isGoblinDead = false;

    // goblin无敌
    private boolean isGoblinInvincible = false; // 短暂无敌

    // 当前goblin运动
    private AnimationDrawable currentGoblinWalkAnimation;

    // goblin攻击冷却时间（新增）
    private long lastGoblinAttackTime = 0;

    private static final long GOBLIN_ATTACK_COOLDOWN = 2000; // 2秒攻击间隔

    // 游戏状态（新增）
    private boolean gameRunning = true;

    private int goblinMaxHp = 100;

    // 文本标签
    private TextView scoreText, levelText, comboText, timerText;

    private TextView expText, playerLevelText;

    // 游戏分数
    private int gameScore = 0;

    // 游戏等级
    private int gameLevel = 1;

    private int comboCount = 0;

    private long lastAttackTime = 0;

    private static final long COMBO_TIMEOUT = 3000;

    // 游戏计时器
    private Handler gameTimer;
    private Runnable gameTimerRunnable;
    private long gameStartTime;
    private boolean timerRunning = false;

    // 道具系统
    private List<PowerUp> powerUps = new ArrayList<>();
    private Handler powerUpSpawner;
    private Runnable powerUpSpawnRunnable;
    private static final long POWERUP_SPAWN_INTERVAL = 15000;

    // 环境障碍物系统
    private List<Obstacle> obstacles = new ArrayList<>();

    // 粒子效果系统
    private List<ParticleEffect> particles = new ArrayList<>();
    private Handler particleHandler;
    private Runnable particleUpdateRunnable;

    // 摄像机震动效果
    private boolean cameraShaking = false;
    private float shakeIntensity = 0;
    private long shakeEndTime = 0;

    // 技能冷却系统
    private long lastSpecialAttackTime = 0;
    private static final long SPECIAL_ATTACK_COOLDOWN = 8000;

    // 经验值系统
    private int experience = 0;
    private int playerLevel = 1;
    private int experienceToNextLevel = 100;

    // 成就系统
    private int totalDamageDealt = 0;

    private int totalDamageTaken = 0;

    private int maxCombo = 0;


    private long survivalTime = 0;


    // Mario状态增强
    private boolean isSpeedBoosted = false;

    private boolean isAttackBoosted = false;

    private boolean isShielded = false;

    private long speedBoostEndTime = 0;

    private long attackBoostEndTime = 0;

    private long shieldEndTime = 0;

    private int marioAttackDamage = 15;

    private float currentSpeedMultiplier = 1.0f;


    // ---------------------------------- 内部类定义 ---------------------------------------
    public static class PowerUp {
        public enum Type { SPEED_BOOST, ATTACK_BOOST, HEALTH_POTION, SHIELD, EXPERIENCE_ORB, FREEZE_TIME }

        public Type type;
        public ImageView imageView;
        public float x, y;
        public long spawnTime;
        public boolean collected = false;

        public PowerUp(Type type, ImageView imageView, float x, float y) {
            this.type = type;
            this.imageView = imageView;
            this.x = x;
            this.y = y;
            this.spawnTime = System.currentTimeMillis();
        }
    }


    public static class Obstacle {
        public enum Type { ROCK, TREE, WALL }

        public Type type;
        public ImageView imageView;
        public float x, y, width, height;
        public boolean destructible;
        public int health;

        public Obstacle(Type type, ImageView imageView, float x, float y, float width, float height) {
            this.type = type;
            this.imageView = imageView;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.destructible = (type != Type.WALL);
            this.health = destructible ? 30 : Integer.MAX_VALUE;
        }
    }

    public static class ParticleEffect {
        public enum Type { DAMAGE_NUMBER, HEAL_EFFECT, EXPLOSION, SPEED_TRAIL }

        public Type type;
        public TextView textView;
        public ImageView imageView;
        public float x, y, velocityX, velocityY;
        public long startTime, duration;
        public String text;
        public boolean isText;

        public ParticleEffect(Type type, float x, float y, String text, long duration) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.text = text;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.isText = true;
            this.velocityY = -2f;
        }

        public ParticleEffect(Type type, float x, float y, long duration) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.isText = false;
        }
    }

    // 创建时
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return inflater.inflate(R.layout.fragment_game_mario, container, false);
    }

    // 创建场景
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 方向轮盘
        JoystickView joystick = view.findViewById(R.id.joystick);

        // 攻击键
        ImageButton attack = view.findViewById(R.id.btn_attack);

        // 音频静音键
        ImageButton muteBtn = view.findViewById(R.id.btn_mute);

        muteBtn.setOnClickListener(v -> toggleMute(muteBtn));

        // 马里奥
        mario = view.findViewById(R.id.iv_mario);
        marioHpBar = view.findViewById(R.id.hp_mario);

        // 播放背景音乐
        playBackgroundMusic();

        // 获取屏幕尺寸
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        // 初始化马里奥站立图
        standDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.mario_stand);
        mario.setImageDrawable(standDrawable);

        // 等布局完成后获取主角实际宽高
        mario.post(() -> {
            marioWidth = mario.getWidth();
            marioHeight = mario.getHeight();
        });

        // 获取goblin
        goblin = view.findViewById(R.id.iv_goblin);
        goblinHpBar = view.findViewById(R.id.hp_goblin);
        // 初始化哥布林站立图
        goblinStandDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.goblin_stand);
        goblin.setImageDrawable(goblinStandDrawable);

        // 等布局完成后获取哥布林实际宽高
        goblin.post(() -> {
            goblinWidth = goblin.getWidth();
            goblinHeight = goblin.getHeight();
            // 初始化哥布林血条位置（跟随哥布林）
        });
        goblin.post(this::updateGoblinHpBarPosition);

        // 启动哥布林AI
        startGoblinAI();

        // mario移动轮盘监听攻击事件
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onMove(float xPercent, float yPercent) {
                if (!gameRunning || isMarioDead) return; // 游戏结束或死亡时不能移动
                moveMario(xPercent, yPercent);

                // 判断是否在移动（加入死区避免抖动）
                boolean isMoving = Math.abs(xPercent) > 0.1f || Math.abs(yPercent) > 0.1f;
                updateMarioAnimation(isMoving);
            }

            // 方向改变时
            @Override
            public void onDirectionChanged(JoystickView.Direction dir) {
                Log.d(TAG, "Direction: " + dir);
                marioLastDirection = dir;
                // 把mario改变的方向传递下去
                updateMarioDirection(dir);
            }
        });

        // 监听攻击
        attack.setOnClickListener(v -> {
            if (!gameRunning || isMarioAttacking || isMarioDead || isGoblinDead) return; // 游戏结束时不能攻击
            Toast.makeText(requireContext(), "攻击!", Toast.LENGTH_SHORT).show();
            // 播放mario攻击动画
            playMarioAttackAnimation();

            // 检测哥布林是否被攻击
            if (isInAttackRange()) {
                // 播放哥布林受伤动画
                playGoblinHurtAnimation();
                goblinHp -= 15; // 增加伤害
                goblinHpBar.setProgress(Math.max(goblinHp, 0));

                // 击退效果
                pushGoblinAway();

                if (goblinHp <= 0) {
                    // 播放哥布林死亡动画
                    playGoblinDieAnimation();
                }
            }
        });
    }

    private void levelUp(int newLevel) {
        gameLevel = newLevel;

        // 提升Goblin属性
        goblinMaxHp += 20;
        goblinHp = goblinMaxHp;
        goblinHpBar.setMax(goblinMaxHp);
        goblinHpBar.setProgress(goblinHp);
        goblinSpeed += 0.5f;

        Toast.makeText(requireContext(), "等级提升！Lv." + gameLevel, Toast.LENGTH_SHORT).show();
//        playSound(R.raw.level_up_sound);
    }

    // 检测是否在攻击范围内（新增）
    private boolean isInAttackRange() {
        if (isGoblinDead) return false;

        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float distance = getDistance(marioX, marioY, goblinX, goblinY);
        return distance < ATTACK_DISTANCE;
    }

    // 计算距离（新增）
    private float getDistance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // 击退哥布林（新增）
    private void pushGoblinAway() {
        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float dx = goblinX - marioX;
        float dy = goblinY - marioY;
        float distance = getDistance(marioX, marioY, goblinX, goblinY);

        if (distance > 0) {
            float pushX = (dx / distance) * PUSH_FORCE;
            float pushY = (dy / distance) * PUSH_FORCE;

            float newX = goblin.getX() + pushX;
            float newY = goblin.getY() + pushY;

            // 边界检测
            newX = Math.max(0, Math.min(newX, screenWidth - goblin.getWidth()));
            newY = Math.max(0, Math.min(newY, screenHeight - goblin.getHeight()));

            goblin.setX(newX);
            goblin.setY(newY);
            updateGoblinHpBarPosition();
        }
    }

    // 检测碰撞并处理（新增）
    private void handleCollision() {
        if (isMarioDead || isGoblinDead) return;

        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float distance = getDistance(marioX, marioY, goblinX, goblinY);

        if (distance < COLLISION_DISTANCE) {
            // 计算分离向量
            float dx = goblinX - marioX;
            float dy = goblinY - marioY;

            if (distance > 0) {
                // 标准化方向向量
                dx /= distance;
                dy /= distance;

                // 计算需要分离的距离
                float separationDistance = COLLISION_DISTANCE - distance;
                float separationForce = separationDistance * 0.5f;

                // 分别推开两个角色
                float marioNewX = mario.getX() - dx * separationForce;
                float marioNewY = mario.getY() - dy * separationForce;
                float goblinNewX = goblin.getX() + dx * separationForce;
                float goblinNewY = goblin.getY() + dy * separationForce;

                // 边界检测
                marioNewX = Math.max(0, Math.min(marioNewX, screenWidth - marioWidth));
                marioNewY = Math.max(0, Math.min(marioNewY, screenHeight - marioHeight));
                goblinNewX = Math.max(0, Math.min(goblinNewX, screenWidth - goblinWidth));
                goblinNewY = Math.max(0, Math.min(goblinNewY, screenHeight - goblinHeight));

                mario.setX(marioNewX);
                mario.setY(marioNewY);
                goblin.setX(goblinNewX);
                goblin.setY(goblinNewY);

                // 更新血条位置
                marioHpBar.setX(marioNewX);
                marioHpBar.setY(marioNewY - 20);
                updateGoblinHpBarPosition();
            }
        }
    }

    // 更新哥布林血条位置
    private void updateGoblinHpBarPosition() {
        if (goblinHpBar == null || goblin == null) return;
        float x = goblin.getX();
        float y = goblin.getY() - goblinHpBar.getHeight() - 4; // 血条在Goblin上方
        goblinHpBar.setX(x);
        goblinHpBar.setY(y);
    }

    // 开启Goblin AI
    private void startGoblinAI() {
        if (goblinAIHandler != null) return; // 防止重复启动
        goblinAIHandler = new Handler();
        goblinAIRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && !isGoblinDead) {
                    updateGoblinAI();
                    handleCollision(); // 每帧检测碰撞
                }
                goblinAIHandler.postDelayed(this, GOBLIN_UPDATE_INTERVAL);
            }
        };
        goblinAIHandler.post(goblinAIRunnable);
    }

    // 停止Goblin运动
    private void stopGoblinAI() {
        if (goblinAIHandler != null && goblinAIRunnable != null) {
            goblinAIHandler.removeCallbacks(goblinAIRunnable);
            goblinAIHandler = null;
            goblinAIRunnable = null;
        }
    }

    // 在 updateGoblinAI 中使用 setX/setY
    private void updateGoblinAI() {
        if (mario == null || goblin == null || !isAdded() || goblinHp <= 0 || isMarioDead) return;

        float marioCenterX = mario.getX() + marioWidth / 2f;
        float marioCenterY = mario.getY() + marioHeight / 2f;
        float goblinCenterX = goblin.getX() + goblinWidth / 2f;
        float goblinCenterY = goblin.getY() + goblinHeight / 2f;

        float dx = marioCenterX - goblinCenterX;
        float dy = marioCenterY - goblinCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 攻击判定（增加冷却时间）
        long currentTime = System.currentTimeMillis();
        if (distance < ATTACK_DISTANCE && !isGoblinAttacking &&
                currentTime - lastGoblinAttackTime > GOBLIN_ATTACK_COOLDOWN) {
            performGoblinAttack();
            lastGoblinAttackTime = currentTime;
            return;
        }

        // 移动逻辑（避免重叠）
        if (distance > COLLISION_DISTANCE) {
            float moveX = dx / distance * goblinSpeed;
            float moveY = dy / distance * goblinSpeed;

            float newX = goblin.getX() + moveX;
            float newY = goblin.getY() + moveY;

            // 边界检测
            newX = Math.max(0, Math.min(newX, screenWidth - goblin.getWidth()));
            newY = Math.max(0, Math.min(newY, screenHeight - goblin.getHeight()));

            goblin.setX(newX);
            goblin.setY(newY);
            updateGoblinHpBarPosition(); // 血条跟随

            JoystickView.Direction newDirection = determineGoblinDirection(dx, dy);
            if (newDirection != goblinLastDirection) {
                goblinLastDirection = newDirection;
                // 修改goblin运动方向
                updateGoblinDirection(newDirection);
            }
            updateGoblinAnimation(true);
        } else {
            updateGoblinAnimation(false);
        }
    }

    private JoystickView.Direction determineGoblinDirection(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) return JoystickView.Direction.RIGHT;
            else return JoystickView.Direction.LEFT;
        } else {
            if (dy > 0) return JoystickView.Direction.DOWN;
            else return JoystickView.Direction.UP;
        }
    }

    // 修改goblin的方向
    private void updateGoblinDirection(JoystickView.Direction dir) {
        if (dir == JoystickView.Direction.LEFT) {
            goblin.setScaleX(-1);
        } else if (dir == JoystickView.Direction.RIGHT) {
            goblin.setScaleX(1);
        }
    }

    // 修改goblin的动画
    private void updateGoblinAnimation(boolean isMovingNow) {
        if (isGoblinWalking == isMovingNow || isGoblinAttacking || isGoblinHurt) return; // 攻击或受伤时不切换动画
        isGoblinWalking = isMovingNow;

        if (isGoblinWalking) {
            if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
                currentGoblinWalkAnimation.stop();
            }
            int animResId = getGoblinWalkAnimationResId(goblinLastDirection);
            Drawable drawable = ContextCompat.getDrawable(requireContext(), animResId);
            if (drawable != null) drawable = drawable.mutate(); // 创建独立状态
            if (drawable instanceof AnimationDrawable) {
                currentGoblinWalkAnimation = (AnimationDrawable) drawable;
                goblin.setImageDrawable(currentGoblinWalkAnimation);
                currentGoblinWalkAnimation.start();
            } else {
                Log.e(TAG, "Goblin walk animation is not AnimationDrawable!");
                goblin.setImageDrawable(goblinStandDrawable);
            }
        } else {
            if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
                currentGoblinWalkAnimation.stop();
            }
            goblin.setImageDrawable(goblinStandDrawable);
            currentGoblinWalkAnimation = null;
        }
    }

    // 根据goblin的方向获取动画资源ID
    private int getGoblinWalkAnimationResId(JoystickView.Direction goblinDir) {
        switch (goblinDir) {
            case UP: return R.drawable.goblin_walk_up;
            case DOWN: return R.drawable.goblin_walk_down;
            case LEFT: return R.drawable.goblin_walk_left;
            case RIGHT: default: return R.drawable.goblin_walk_right;
        }
    }

    // 哥布林攻击
    private void performGoblinAttack() {
        if (isGoblinAttacking || isGoblinDead) return;
        isGoblinAttacking = true;

        // 播放攻击音效
        if (!isMuted) {
            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.goblin_attack_sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            }
        }

        if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
            currentGoblinWalkAnimation.stop();
        }

        int attackAnimResId = getGoblinAttackAnimationResId(goblinLastDirection);
        goblinAttackAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), attackAnimResId);
        goblin.setImageDrawable(goblinAttackAnimation);
        goblinAttackAnimation.start();

        int attackDuration = getAnimationDuration(goblinAttackAnimation);

        goblin.postDelayed(() -> {
            isGoblinAttacking = false;
            if (goblinAttackAnimation != null) {
                goblinAttackAnimation.stop();
            }

            if (!isGoblinDead) {
                if (isGoblinWalking) updateGoblinAnimation(true);
                else goblin.setImageDrawable(goblinStandDrawable);
            }

            // Mario受伤判定（只有在攻击范围内才会受伤）
            if (!isMarioDead && !isMarioInvincible && isInAttackRange()) {
                marioHp -= 20; // 增加伤害
                marioHpBar.setProgress(Math.max(marioHp, 0));

                if (marioHp <= 0) {
                    playMarioDieAnimation();
                } else {
                    playMarioHurtAnimation(); // 播放受伤动画并短暂无敌
                    // 击退效果
                    pushMarioAway();
                }
                Toast.makeText(requireContext(), "哥布林攻击了马里奥！", Toast.LENGTH_SHORT).show();
            }
        }, attackDuration);
    }

    // 击退Mario（新增）
    private void pushMarioAway() {
        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float dx = marioX - goblinX;
        float dy = marioY - goblinY;
        float distance = getDistance(marioX, marioY, goblinX, goblinY);

        if (distance > 0) {
            float pushX = (dx / distance) * PUSH_FORCE;
            float pushY = (dy / distance) * PUSH_FORCE;

            float newX = mario.getX() + pushX;
            float newY = mario.getY() + pushY;

            // 边界检测
            newX = Math.max(0, Math.min(newX, screenWidth - marioWidth));
            newY = Math.max(0, Math.min(newY, screenHeight - marioHeight));

            mario.setX(newX);
            mario.setY(newY);
            // 更新血条位置
            marioHpBar.setX(newX);
            marioHpBar.setY(newY - 20);
        }
    }

    // Goblin受伤动画 + 无敌帧
    private void playGoblinHurtAnimation() {
        if (isGoblinHurt || isGoblinDead) return;
        isGoblinHurt = true;
        isGoblinInvincible = true;

        if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
            currentGoblinWalkAnimation.stop();
        }

        int hurtAnimResId = getGoblinHurtAnimationResId(goblinLastDirection);
        AnimationDrawable hurtAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), hurtAnimResId);
        goblin.setImageDrawable(hurtAnimation);
        hurtAnimation.start();

        int duration = getAnimationDuration(hurtAnimation);

        goblin.postDelayed(() -> {
            isGoblinHurt = false;
            if (!isGoblinDead) {
                if (isGoblinWalking) updateGoblinAnimation(true);
                else goblin.setImageDrawable(goblinStandDrawable);
            }
        }, duration);

        // 设置无敌时间，防止连续掉血（这里设为1.5秒）
        goblin.postDelayed(() -> isGoblinInvincible = false, 1500);
    }

    // 播放goblin死亡动画
    private void playGoblinDieAnimation() {
        if (isGoblinDead) return;
        isGoblinDead = true;
        gameRunning = false; // 游戏结束

        // 停止走路动画
        if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
            currentGoblinWalkAnimation.stop();
        }

        // 根据死亡前的方向获取对应死亡动画
        int dieAnimResId = getGoblinDieAnimationResId(goblinLastDirection);
        AnimationDrawable dieAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), dieAnimResId);
        goblin.setImageDrawable(dieAnimation);

        // 播放死亡动画
        assert dieAnimation != null;
        dieAnimation.start();

        // 计算动画总时长
        int duration = getAnimationDuration(dieAnimation);

        // 动画播放完成后再消失
        goblin.postDelayed(() -> {
            goblin.setVisibility(View.GONE);
            goblinHpBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "胜利！哥布林被击败!", Toast.LENGTH_LONG).show();
            // 停止背景音乐
            if (bgmPlayer != null && bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }
        }, duration);
    }

    // Mario受伤动画 + 无敌帧
    private void playMarioHurtAnimation() {
        if (isMarioHurt || isMarioDead) return;
        isMarioHurt = true;
        isMarioInvincible = true;

        if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
            currentMarioWalkAnimation.stop();
        }

        int hurtAnimResId = getMarioHurtAnimationResId(marioLastDirection);
        AnimationDrawable hurtAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), hurtAnimResId);
        mario.setImageDrawable(hurtAnimation);
        hurtAnimation.start();

        int duration = getAnimationDuration(hurtAnimation);

        mario.postDelayed(() -> {
            isMarioHurt = false;
            if (!isMarioDead) {
                if (isMarioWalking) updateMarioAnimation(true);
                else mario.setImageDrawable(standDrawable);
            }
        }, duration);

        // 设置无敌时间，防止连续掉血（这里设为1.5秒）
        mario.postDelayed(() -> isMarioInvincible = false, 1500);
    }

    // 播放mario死亡动画
    private void playMarioDieAnimation() {
        if (isMarioDead) return;
        isMarioDead = true;
        gameRunning = false; // 游戏结束

        // 停止走路动画
        if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
            currentMarioWalkAnimation.stop();
        }

        // 根据死亡前的方向获取对应死亡动画
        int dieAnimResId = getMarioDieAnimationResId(marioLastDirection);
        AnimationDrawable dieAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), dieAnimResId);
        mario.setImageDrawable(dieAnimation);

        // 播放死亡动画
        dieAnimation.start();

        // 计算动画总时长
        int duration = getAnimationDuration(dieAnimation);

        // 动画播放完成后再消失
        mario.postDelayed(() -> {
            mario.setVisibility(View.GONE);
            marioHpBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "失败！马里奥被击败!", Toast.LENGTH_LONG).show();
            // 停止背景音乐
            if (bgmPlayer != null && bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }
        }, duration);
    }

    // 获取goblin受伤动画方向
    private int getGoblinHurtAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_hurt_up;
            case DOWN: return R.drawable.goblin_hurt_down;
            case LEFT: return R.drawable.goblin_hurt_left;
            case RIGHT: default: return R.drawable.goblin_hurt_right;
        }
    }

    // 获取mario受伤动画方向
    private int getMarioHurtAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.mario_hurt_up;
            case DOWN: return R.drawable.mario_hurt_down;
            case LEFT: return R.drawable.mario_hurt_left;
            case RIGHT: default: return R.drawable.mario_hurt_right;
        }
    }

    // 获取mario死亡动画方向
    private int getMarioDieAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.mario_die_up;
            case DOWN: return R.drawable.mario_die_down;
            case LEFT: return R.drawable.mario_die_left;
            case RIGHT: default: return R.drawable.mario_die_right;
        }
    }

    // 获取goblin死亡动画方向
    private int getGoblinDieAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_die_up;
            case DOWN: return R.drawable.goblin_die_down;
            case LEFT: return R.drawable.goblin_die_left;
            case RIGHT: default: return R.drawable.goblin_die_right;
        }
    }

    // 获取goblin攻击动画方向
    private int getGoblinAttackAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_attack_up;
            case DOWN: return R.drawable.goblin_attack_down;
            case LEFT: return R.drawable.goblin_attack_left;
            case RIGHT: default: return R.drawable.goblin_attack_right;
        }
    }

    // 静音键
    private void toggleMute(ImageButton muteBtn) {
        if (bgmPlayer == null) return;

        isMuted = !isMuted;
        if (isMuted) {
            bgmPlayer.setVolume(0f, 0f);
            muteBtn.setImageResource(R.drawable.ic_volume_off);
        } else {
            bgmPlayer.setVolume(0.5f, 0.5f);
            muteBtn.setImageResource(R.drawable.ic_volume_on);
        }
        Toast.makeText(requireContext(), isMuted ? "静音" : "音效已开启", Toast.LENGTH_SHORT).show();
    }

    // 播放背景音乐
    private void playBackgroundMusic() {
        if (bgmPlayer != null) return;

        bgmPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = requireContext().getResources().openRawResourceFd(R.raw.game_bgm);
            if (afd == null) return;
            bgmPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            bgmPlayer.setLooping(true);
            bgmPlayer.setVolume(isMuted ? 0f : 0.5f, isMuted ? 0f : 0.5f);

            bgmPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isBgmPlaying = true;
            });

            bgmPlayer.setOnErrorListener((mp, what, extra) -> true);
            bgmPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "播放背景音乐失败", e);
            if (bgmPlayer != null) {
                bgmPlayer.release();
                bgmPlayer = null;
            }
        }
    }

    // 播放mario攻击动画
    private void playMarioAttackAnimation() {
        if (isMarioAttacking || isMarioHurt || isMarioDead) return;  // 受伤/死亡时不能攻击
        isMarioAttacking = true;

        // 攻击音效
        if (!isMuted) {
            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.mario_attack_sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            }
        }

        if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
            currentMarioWalkAnimation.stop();
        }

        int attackAnimResId = getMarioAttackAnimationResId(marioLastDirection);
        attackAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), attackAnimResId);
        mario.setImageDrawable(attackAnimation);
        attackAnimation.start();

        int attackDuration = getAnimationDuration(attackAnimation);

        mario.postDelayed(() -> {
            isMarioAttacking = false;
            if (attackAnimation != null) {
                attackAnimation.stop();
            }
            if (!isMarioDead && !isMarioHurt) {   // 受伤/死亡不恢复到行走或站立动画
                if (isMarioWalking) updateMarioAnimation(true);
                else mario.setImageDrawable(standDrawable);
            }
        }, attackDuration);
    }

    // 获取动画播放时间
    private int getAnimationDuration(AnimationDrawable animation) {
        int duration = 0;
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            duration += animation.getDuration(i);
        }
        return duration;
    }

    private void updateMarioAnimation(boolean isMovingNow) {
        if (isMarioWalking == isMovingNow || isMarioAttacking || isMarioHurt) return; // 攻击或受伤时不切换动画
        isMarioWalking = isMovingNow;

        if (isMarioWalking) {
            if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
                currentMarioWalkAnimation.stop();
            }
            int animResId = getMarioWalkAnimationResId(marioLastDirection);
            currentMarioWalkAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), animResId);
            mario.setImageDrawable(currentMarioWalkAnimation);
            currentMarioWalkAnimation.start();
        } else {
            if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
                currentMarioWalkAnimation.stop();
            }
            // 播放停止后重置为原始状态
            mario.setImageDrawable(standDrawable);
        }
    }

    // 获取mario攻击动画id
    private int getMarioAttackAnimationResId(JoystickView.Direction marioDir) {
        if (marioDir == JoystickView.Direction.CENTER) marioDir = JoystickView.Direction.RIGHT;
        switch (marioDir) {
            case UP: return R.drawable.mario_attack_up;
            case DOWN: return R.drawable.mario_attack_down;
            case LEFT: return R.drawable.mario_attack_left;
            case RIGHT: default: return R.drawable.mario_attack_right;
        }
    }

    // 获取mario运动方向
    private int getMarioWalkAnimationResId(JoystickView.Direction marioDir) {
        switch (marioDir) {
            case UP: return R.drawable.mario_walk_up;
            case DOWN: return R.drawable.mario_walk_down;
            case LEFT: return R.drawable.mario_walk_left;
            case RIGHT: default: return R.drawable.mario_walk_right;
        }
    }

    // 马里奥运动（修正版，统一用 setX/setY）
    private void moveMario(float xPercent, float yPercent) {
        if (isMarioDead || isMarioHurt) return; // 死亡或受伤时不能移动

        float deltaX = xPercent * marioSpeed;
        float deltaY = yPercent * marioSpeed;

        float newX = mario.getX() + deltaX;
        float newY = mario.getY() + deltaY;

        // 边界检测
        newX = Math.max(0, Math.min(newX, screenWidth - marioWidth));
        newY = Math.max(0, Math.min(newY, screenHeight - marioHeight));

        mario.setX(newX);
        mario.setY(newY);
        // 同步血条位置
        marioHpBar.setX(newX);
        marioHpBar.setY(newY - 20); // 血条在角色上方
    }

    private void updateMarioDirection(JoystickView.Direction marioDir) {
        if (marioDir == JoystickView.Direction.LEFT) mario.setScaleX(-1);
        else if (marioDir == JoystickView.Direction.RIGHT) mario.setScaleX(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gameRunning = false; // 停止游戏

        if (bgmPlayer != null) {
            try {
                if (bgmPlayer.isPlaying()) bgmPlayer.stop();
                bgmPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "释放BGM播放器时出错", e);
            } finally {
                bgmPlayer = null;
                isBgmPlaying = false;
            }
        }
        stopGoblinAI();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}