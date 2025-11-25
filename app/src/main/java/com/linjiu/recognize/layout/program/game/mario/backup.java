package com.linjiu.recognize.layout.program.game.mario;

import android.annotation.SuppressLint;
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
import android.view.animation.Animation;
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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class backup extends Fragment {
    private static final String TAG = "GameMarioFragment";

    // ---------------------------------- mario ---------------------------------------
    private AnimationDrawable currentMarioWalkAnimation;
    private Drawable standDrawable;
    private boolean isMarioWalking = false;
    private JoystickView.Direction marioLastDirection = JoystickView.Direction.CENTER;
    private AnimationDrawable attackAnimation;
    private boolean isMarioAttacking = false;
    private ImageView mario;
    private ProgressBar marioHpBar;
    private float marioSpeed = 8.0f;
    private int marioHp = 100;
    private int marioMaxHp = 100;
    private AnimationDrawable marioHurtAnimation;
    private AnimationDrawable marioDieAnimation;
    private boolean isMarioHurt = false;
    private boolean isMarioDead = false;
    private boolean isMarioInvincible = false;
    private int marioWidth, marioHeight;

    // ---------------------------------- 新增游戏系统 ---------------------------------------
    private TextView scoreText, levelText, comboText, timerText;
    private TextView expText, playerLevelText;
    private int gameScore = 0;
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

    // ---------------------------------- 屏幕和音频 ---------------------------------------
    private int screenWidth, screenHeight;
    private MediaPlayer bgmPlayer;
    private boolean isBgmPlaying = false;
    private boolean isMuted = false;
    private Random random = new Random();

    // ---------------------------------- AI和碰撞 ---------------------------------------
    private Handler goblinAIHandler;
    private Runnable goblinAIRunnable;
    private static final long GOBLIN_UPDATE_INTERVAL = 50;
    private static final float ATTACK_DISTANCE = 150f;
    private static final float COLLISION_DISTANCE = 80f;
    private static final float PUSH_FORCE = 15f;

    // ---------------------------------- goblin ---------------------------------------
    private ImageView goblin;
    private ProgressBar goblinHpBar;
    private Drawable goblinStandDrawable;
    private AnimationDrawable goblinAttackAnimation;
    private boolean isGoblinWalking = false;
    private boolean isGoblinAttacking = false;
    private JoystickView.Direction goblinLastDirection = JoystickView.Direction.DOWN;
    private int goblinWidth, goblinHeight;
    private float goblinSpeed = 6.0f;
    private int goblinHp = 100;
    private int goblinMaxHp = 100;
    private boolean isGoblinHurt = false;
    private boolean isGoblinDead = false;
    private boolean isGoblinInvincible = false;
    private AnimationDrawable currentGoblinWalkAnimation;
    private long lastGoblinAttackTime = 0;
    private static final long GOBLIN_ATTACK_COOLDOWN = 2000;

    // Goblin AI增强
    private enum GoblinState { CHASING, ATTACKING, RETREATING, CIRCLING }
    private GoblinState goblinState = GoblinState.CHASING;
    private long stateChangeTime = 0;
    private float circleAngle = 0;

    private boolean gameRunning = true;


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

    // ---------------------------------- Fragment 生命周期 ---------------------------------------
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            View view = inflater.inflate(R.layout.fragment_game_mario, container, false);
            if (view == null) {
                Log.e(TAG, "Failed to inflate fragment_game_mario layout");
                return null;
            }
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(requireContext(), "Failed to load game layout", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isAdded()) {
            Log.e(TAG, "Fragment not attached to activity");
            return;
        }

        // 获取屏幕尺寸
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        try {
            // 初始化UI
            initializeUI(view);

            // 初始化角色
            initializeCharacters(view);

            // 启动游戏系统
            startGameSystems();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing game", e);
            Toast.makeText(requireContext(), "Failed to initialize game", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------- 初始化方法 ---------------------------------------
    @SuppressLint("WrongViewCast")
    private void initializeUI(View view) {
        // 游戏UI
        scoreText = view.findViewById(R.id.tv_score);
        levelText = view.findViewById(R.id.tv_level);
        comboText = view.findViewById(R.id.tv_combo);
        timerText = view.findViewById(R.id.tv_timer);
        expText = view.findViewById(R.id.tv_exp);
        playerLevelText = view.findViewById(R.id.tv_player_level);

        // 控制按钮
        JoystickView joystick = view.findViewById(R.id.joystick);
        ImageButton attack = view.findViewById(R.id.btn_attack);
        ImageButton muteBtn = view.findViewById(R.id.btn_mute);
        ImageButton specialBtn = view.findViewById(R.id.btn_special);
        ImageButton dashBtn = view.findViewById(R.id.btn_dash);

        if (scoreText == null || levelText == null || comboText == null || timerText == null ||
                expText == null || playerLevelText == null || joystick == null ||
                attack == null || muteBtn == null || specialBtn == null || dashBtn == null) {
            Log.e(TAG, "One or more UI components not found in layout");
            Toast.makeText(requireContext(), "UI initialization failed", Toast.LENGTH_SHORT).show();
            return;
        }

        muteBtn.setOnClickListener(v -> toggleMute(muteBtn));
        // 特殊技能按钮
        specialBtn.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (comboCount >= 5 && currentTime - lastSpecialAttackTime > SPECIAL_ATTACK_COOLDOWN) {
                useSpecialAttack();
                lastSpecialAttackTime = currentTime;
            } else {
                long cooldownRemaining = (lastSpecialAttackTime + SPECIAL_ATTACK_COOLDOWN - currentTime) / 1000;
                if (cooldownRemaining > 0) {
                    Toast.makeText(requireContext(), "技能冷却中: " + cooldownRemaining + "秒", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "需要5连击才能使用特殊技能！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 冲刺按钮
        dashBtn.setOnClickListener(v -> performDash());

        // 摇杆控制
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onMove(float xPercent, float yPercent) {
                if (!isAdded() || !gameRunning || isMarioDead) return;
                moveMario(xPercent, yPercent);
                boolean isMoving = Math.abs(xPercent) > 0.1f || Math.abs(yPercent) > 0.1f;
                updateMarioAnimation(isMoving);
                if (isMoving && isSpeedBoosted) {
                    createSpeedTrail();
                }
            }

            @Override
            public void onDirectionChanged(JoystickView.Direction dir) {
                if (!isAdded()) return;
                marioLastDirection = dir;
                updateMarioDirection(dir);
            }
        });

        attack.setOnClickListener(v -> {
            if (!isAdded() || !gameRunning || isMarioAttacking || isMarioDead || isGoblinDead) return;
            performMarioAttack();
        });

        updateUI();
        generateObstacles();
    }

    private void initializeCharacters(View view) {
        // 马里奥
        mario = view.findViewById(R.id.iv_mario);
        marioHpBar = view.findViewById(R.id.hp_mario);
        if (mario == null || marioHpBar == null) {
            Log.e(TAG, "Mario or Mario HP bar not found in layout");
            return;
        }
        marioHpBar.setMax(marioMaxHp);
        marioHpBar.setProgress(marioHp);

        // 哥布林
        goblin = view.findViewById(R.id.iv_goblin);
        goblinHpBar = view.findViewById(R.id.hp_goblin);
        if (goblin == null || goblinHpBar == null) {
            Log.e(TAG, "Goblin or Goblin HP bar not found in layout");
            return;
        }
        goblinHpBar.setMax(goblinMaxHp);
        goblinHpBar.setProgress(goblinHp);

        // 初始化图片
        standDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.mario_stand);
        if (standDrawable == null) {
            Log.e(TAG, "Mario stand drawable not found");
            standDrawable = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_help); // Fallback
        }
        mario.setImageDrawable(standDrawable);

        goblinStandDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.goblin_stand);
        if (goblinStandDrawable == null) {
            Log.e(TAG, "Goblin stand drawable not found");
            goblinStandDrawable = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_help); // Fallback
        }
        goblin.setImageDrawable(goblinStandDrawable);

        // 获取尺寸
        mario.post(() -> {
            marioWidth = mario.getWidth();
            marioHeight = mario.getHeight();
        });

        goblin.post(() -> {
            goblinWidth = goblin.getWidth();
            goblinHeight = goblin.getHeight();
            updateGoblinHpBarPosition();
        });
    }

    private void startGameSystems() {
        try {
            playBackgroundMusic();
            startGoblinAI();
            startPowerUpSystem();
            startGameTimer();
            startParticleSystem();
            gameStartTime = System.currentTimeMillis();
            timerRunning = true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting game systems", e);
            Toast.makeText(requireContext(), "Failed to start game systems", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------- 游戏系统方法 ---------------------------------------
    private void startGameTimer() {
        gameTimer = new Handler();
        gameTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerRunning) {
                    updateGameTimer();
                    updateStatusEffects();
                    updateUI();
                    gameTimer.postDelayed(this, 1000);
                }
            }
        };
        gameTimer.post(gameTimerRunnable);
    }

    private void updateGameTimer() {
        if (!timerRunning) return;
        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        int minutes = (int) (elapsedTime / 60000);
        int seconds = (int) ((elapsedTime % 60000) / 1000);
        timerText.setText(String.format("%02d:%02d", minutes, seconds));

        // 每30秒提升难度
        int newLevel = (int) (elapsedTime / 30000) + 1;
        if (newLevel != gameLevel) {
            levelUp(newLevel);
        }
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
        playSound(R.raw.level_up_sound);
    }

    private void updateStatusEffects() {
        long currentTime = System.currentTimeMillis();

        if (isSpeedBoosted && currentTime > speedBoostEndTime) {
            isSpeedBoosted = false;
            currentSpeedMultiplier = 1.0f;
            Toast.makeText(requireContext(), "速度增益结束", Toast.LENGTH_SHORT).show();
        }

        if (isAttackBoosted && currentTime > attackBoostEndTime) {
            isAttackBoosted = false;
            marioAttackDamage = 15;
            Toast.makeText(requireContext(), "攻击增益结束", Toast.LENGTH_SHORT).show();
        }

        if (isShielded && currentTime > shieldEndTime) {
            isShielded = false;
            Toast.makeText(requireContext(), "护盾失效", Toast.LENGTH_SHORT).show();
        }

        if (comboCount > 0 && currentTime - lastAttackTime > COMBO_TIMEOUT) {
            comboCount = 0;
        }
    }

    // ---------------------------------- 道具系统 ---------------------------------------
    private void startPowerUpSystem() {
        powerUpSpawner = new Handler();
        powerUpSpawnRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && !isGoblinDead) {
                    spawnPowerUp();
                    powerUpSpawner.postDelayed(this, POWERUP_SPAWN_INTERVAL);
                }
            }
        };
        powerUpSpawner.postDelayed(powerUpSpawnRunnable, POWERUP_SPAWN_INTERVAL);
    }

    private void spawnPowerUp() {
        if (powerUps.size() >= 3) return;

        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type type = types[random.nextInt(types.length)];

        float x, y;
        int attempts = 0;
        do {
            x = random.nextFloat() * (screenWidth - 100);
            y = random.nextFloat() * (screenHeight - 100);
            attempts++;
        } while (attempts < 10 && (getDistance(x, y, mario.getX(), mario.getY()) < 200 ||
                getDistance(x, y, goblin.getX(), goblin.getY()) < 200 ||
                checkObstacleCollision(x, y, 80, 80)));

        ImageView powerUpView = new ImageView(requireContext());
        powerUpView.setImageResource(getPowerUpDrawable(type));
        powerUpView.setLayoutParams(new ViewGroup.LayoutParams(80, 80));
        powerUpView.setX(x);
        powerUpView.setY(y);

        // 添加闪烁效果
        powerUpView.setAlpha(0.8f);
        powerUpView.animate()
                .alpha(1.0f)
                .setDuration(500)
                .start();

        ViewGroup container = (ViewGroup) getView();
        if (container != null) {
            container.addView(powerUpView);
            PowerUp powerUp = new PowerUp(type, powerUpView, x, y);
            powerUps.add(powerUp);

            powerUpView.postDelayed(() -> {
                if (!powerUp.collected) {
                    removePowerUp(powerUp);
                    powerUps.remove(powerUp);
                }
            }, 12000);
        }
    }

    private void checkPowerUpCollision() {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (!powerUp.collected) {
                float distance = getDistance(
                        mario.getX() + marioWidth / 2f, mario.getY() + marioHeight / 2f,
                        powerUp.x + 40, powerUp.y + 40
                );

                if (distance < 60) {
                    collectPowerUp(powerUp);
                    iterator.remove();
                }
            }
        }
    }

    private void collectPowerUp(PowerUp powerUp) {
        powerUp.collected = true;
        long currentTime = System.currentTimeMillis();

        switch (powerUp.type) {
            case SPEED_BOOST:
                isSpeedBoosted = true;
                currentSpeedMultiplier = 1.8f;
                speedBoostEndTime = currentTime + 10000;
                Toast.makeText(requireContext(), "获得速度增益！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "SPEED UP!", true);
                break;

            case ATTACK_BOOST:
                isAttackBoosted = true;
                marioAttackDamage = 30;
                attackBoostEndTime = currentTime + 12000;
                Toast.makeText(requireContext(), "获得攻击增益！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "POWER UP!", true);
                break;

            case HEALTH_POTION:
                int healAmount = Math.min(40, marioMaxHp - marioHp);
                marioHp += healAmount;
                marioHpBar.setProgress(marioHp);
                Toast.makeText(requireContext(), "恢复 " + healAmount + " 点生命值！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "+" + healAmount + " HP", true);
                break;

            case SHIELD:
                isShielded = true;
                shieldEndTime = currentTime + 8000;
                Toast.makeText(requireContext(), "获得护盾保护！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "SHIELD!", true);
                break;

            case EXPERIENCE_ORB:
                int expGain = 25 + gameLevel * 5;
                gainExperience(expGain);
                Toast.makeText(requireContext(), "获得 " + expGain + " 经验值！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "+" + expGain + " EXP", true);
                break;

            case FREEZE_TIME:
                goblinSpeed = 0;
                Toast.makeText(requireContext(), "时间冻结！敌人被冻结3秒！", Toast.LENGTH_SHORT).show();
                createDamageNumber(powerUp.x, powerUp.y, "TIME FREEZE!", true);
                mario.postDelayed(() -> goblinSpeed = 6.0f + (gameLevel - 1) * 0.5f, 3000);
                break;
        }

        playSound(R.raw.powerup_collect_sound);
        gameScore += 100;
        startCameraShake(3f, 300);
        removePowerUp(powerUp);
    }

    private void removePowerUp(PowerUp powerUp) {
        ViewGroup container = (ViewGroup) getView();
        if (container != null && powerUp.imageView != null) {
            container.removeView(powerUp.imageView);
        }
    }

    // ---------------------------------- 粒子系统 ---------------------------------------
    private void startParticleSystem() {
        particleHandler = new Handler();
        particleUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    updateParticles();
                    updateCameraShake();
                    particleHandler.postDelayed(this, 50);
                }
            }
        };
        particleHandler.post(particleUpdateRunnable);
    }

    private void createDamageNumber(float x, float y, String damage, boolean isHeal) {
        TextView damageText = new TextView(requireContext());
        damageText.setText(damage);
        damageText.setTextSize(18);
        damageText.setTextColor(isHeal ? 0xFF00FF00 : 0xFFFF0000);
        damageText.setX(x);
        damageText.setY(y);

        ViewGroup container = (ViewGroup) getView();
        if (container != null) {
            container.addView(damageText);
            ParticleEffect effect = new ParticleEffect(ParticleEffect.Type.DAMAGE_NUMBER, x, y, damage, 1500);
            effect.textView = damageText;
            particles.add(effect);
        }
    }

    private void createSpeedTrail() {
        if (System.currentTimeMillis() % 100 < 50) {
            float x = mario.getX() + marioWidth / 2f;
            float y = mario.getY() + marioHeight / 2f;

            ImageView trailView = new ImageView(requireContext());
            trailView.setImageResource(R.drawable.speed_trail);
            trailView.setAlpha(0.6f);
            trailView.setX(x - 20);
            trailView.setY(y - 20);
            trailView.setLayoutParams(new ViewGroup.LayoutParams(40, 40));

            ViewGroup container = (ViewGroup) getView();
            if (container != null) {
                container.addView(trailView);
                ParticleEffect effect = new ParticleEffect(ParticleEffect.Type.SPEED_TRAIL, x, y, 500);
                effect.imageView = trailView;
                particles.add(effect);
            }
        }
    }

    private void createDashEffect(float x, float y) {
        ImageView dashEffect = new ImageView(requireContext());
        dashEffect.setImageResource(R.drawable.dash_effect);
        dashEffect.setX(x);
        dashEffect.setY(y);
        dashEffect.setLayoutParams(new ViewGroup.LayoutParams(marioWidth + 20, marioHeight + 20));

        ViewGroup container = (ViewGroup) getView();
        if (container != null) {
            container.addView(dashEffect);
            ParticleEffect effect = new ParticleEffect(ParticleEffect.Type.EXPLOSION, x, y, 800);
            effect.imageView = dashEffect;
            particles.add(effect);
        }
    }

    private void updateParticles() {
        Iterator<ParticleEffect> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ParticleEffect particle = iterator.next();
            long elapsed = System.currentTimeMillis() - particle.startTime;

            if (elapsed > particle.duration) {
                ViewGroup container = (ViewGroup) getView();
                if (container != null) {
                    if (particle.isText && particle.textView != null) {
                        container.removeView(particle.textView);
                    } else if (!particle.isText && particle.imageView != null) {
                        container.removeView(particle.imageView);
                    }
                }
                iterator.remove();
            } else {
                float progress = (float) elapsed / particle.duration;

                if (particle.isText && particle.textView != null) {
                    particle.y += particle.velocityY;
                    particle.textView.setY(particle.y);
                    particle.textView.setAlpha(1.0f - progress);
                } else if (!particle.isText && particle.imageView != null) {
                    particle.imageView.setAlpha(1.0f - progress);

                    if (particle.type == ParticleEffect.Type.EXPLOSION) {
                        float scale = 1.0f + progress * 0.5f;
                        particle.imageView.setScaleX(scale);
                        particle.imageView.setScaleY(scale);
                    }
                }
            }
        }
    }

    // ---------------------------------- 摄像机震动 ---------------------------------------
    private void startCameraShake(float intensity, long duration) {
        cameraShaking = true;
        shakeIntensity = intensity;
        shakeEndTime = System.currentTimeMillis() + duration;
    }

    private void updateCameraShake() {
        if (!cameraShaking) return;

        if (System.currentTimeMillis() > shakeEndTime) {
            cameraShaking = false;
            ViewGroup container = (ViewGroup) getView();
            if (container != null) {
                container.setTranslationX(0);
                container.setTranslationY(0);
            }
        } else {
            ViewGroup container = (ViewGroup) getView();
            if (container != null) {
                float shakeX = (random.nextFloat() - 0.5f) * shakeIntensity * 2;
                float shakeY = (random.nextFloat() - 0.5f) * shakeIntensity * 2;
                container.setTranslationX(shakeX);
                container.setTranslationY(shakeY);
            }
        }
    }

    // ---------------------------------- 障碍物系统 ---------------------------------------
    private void generateObstacles() {
        ViewGroup container = (ViewGroup) getView();
        if (container == null) return;

        int obstacleCount = 3 + random.nextInt(3);

        for (int i = 0; i < obstacleCount; i++) {
            Obstacle.Type type = Obstacle.Type.values()[random.nextInt(Obstacle.Type.values().length)];

            float x, y;
            do {
                x = random.nextFloat() * (screenWidth - 120);
                y = random.nextFloat() * (screenHeight - 120);
            } while (getDistance(x, y, mario.getX(), mario.getY()) < 200 ||
                    getDistance(x, y, goblin.getX(), goblin.getY()) < 200);

            ImageView obstacleView = new ImageView(requireContext());
            obstacleView.setImageResource(getObstacleDrawable(type));
            obstacleView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            obstacleView.setX(x);
            obstacleView.setY(y);

            container.addView(obstacleView);
            obstacles.add(new Obstacle(type, obstacleView, x, y, 100, 100));
        }
    }

    private boolean checkObstacleCollision(float x, float y, float width, float height) {
        for (Obstacle obstacle : obstacles) {
            if (x < obstacle.x + obstacle.width && x + width > obstacle.x &&
                    y < obstacle.y + obstacle.height && y + height > obstacle.y) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------- 经验值系统 ---------------------------------------
    private void gainExperience(int exp) {
        experience += exp;

        if (experience >= experienceToNextLevel) {
            playerLevel++;
            experience -= experienceToNextLevel;
            experienceToNextLevel += 50;

            marioMaxHp += 10;
            marioHp = Math.min(marioHp + 10, marioMaxHp);
            marioHpBar.setMax(marioMaxHp);
            marioHpBar.setProgress(marioHp);
            marioAttackDamage += 2;

            Toast.makeText(requireContext(), "角色升级！Lv." + playerLevel + " 生命值+10, 攻击力+2", Toast.LENGTH_LONG).show();
            playSound(R.raw.level_up_sound);

            createDamageNumber(mario.getX(), mario.getY() - 50, "LEVEL UP!", true);
            startCameraShake(5f, 500);
        }
    }

    // ---------------------------------- 技能系统 ---------------------------------------
    private void performDash() {
        if (!gameRunning || isMarioDead || isMarioHurt) return;

        float dashDistance = 120f;
        float dashX = 0, dashY = 0;

        switch (marioLastDirection) {
            case UP: dashY = -dashDistance; break;
            case DOWN: dashY = dashDistance; break;
            case LEFT: dashX = -dashDistance; break;
            case RIGHT: dashX = dashDistance; break;
            default: dashX = dashDistance; break;
        }

        float newX = mario.getX() + dashX;
        float newY = mario.getY() + dashY;

        newX = Math.max(0, Math.min(newX, screenWidth - marioWidth));
        newY = Math.max(0, Math.min(newY, screenHeight - marioHeight));

        if (!checkObstacleCollision(newX, newY, marioWidth, marioHeight)) {
            mario.setX(newX);
            mario.setY(newY);
            marioHpBar.setX(newX);
            marioHpBar.setY(newY - 20);

            isMarioInvincible = true;
            mario.postDelayed(() -> isMarioInvincible = false, 1000);

            createDashEffect(mario.getX(), mario.getY());

            if (isInAttackRange()) {
                goblinHp -= 10;
                goblinHpBar.setProgress(Math.max(goblinHp, 0));
                pushGoblinAway();
                createDamageNumber(goblin.getX(), goblin.getY(), "10", false);

                if (goblinHp <= 0) {
                    playGoblinDieAnimation();
                }
            }

            playSound(R.raw.dash_sound);
            Toast.makeText(requireContext(), "冲刺攻击！", Toast.LENGTH_SHORT).show();
        }
    }

    private void useSpecialAttack() {
        if (comboCount < 5) return;

        comboCount = 0;

        float distance = getDistance(
                mario.getX() + marioWidth / 2f, mario.getY() + marioHeight / 2f,
                goblin.getX() + goblinWidth / 2f, goblin.getY() + goblinHeight / 2f
        );

        if (distance < ATTACK_DISTANCE * 2f && !isGoblinDead) {
            int specialDamage = 80 + playerLevel * 10;
            goblinHp -= specialDamage;
            goblinHpBar.setProgress(Math.max(goblinHp, 0));
            gameScore += specialDamage * 5;
            totalDamageDealt += specialDamage;

            for (int i = 0; i < 3; i++) {
                pushGoblinAway();
            }

            createDashEffect(goblin.getX(), goblin.getY());
            createDamageNumber(goblin.getX(), goblin.getY() - 50, "SPECIAL: " + specialDamage, false);

            Toast.makeText(requireContext(), "终极技能！伤害: " + specialDamage, Toast.LENGTH_LONG).show();

            startCameraShake(15f, 800);
            playSound(R.raw.special_attack_sound);
            gainExperience(20);

            if (goblinHp <= 0) {
                playGoblinDieAnimation();
            }
        }
    }

    // ---------------------------------- 战斗系统 ---------------------------------------
    private void performMarioAttack() {
        Toast.makeText(requireContext(), "攻击!", Toast.LENGTH_SHORT).show();
        playMarioAttackAnimation();

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime < COMBO_TIMEOUT) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        lastAttackTime = currentTime;

        maxCombo = Math.max(maxCombo, comboCount);

        if (isInAttackRange()) {
            int baseDamage = marioAttackDamage + (playerLevel - 1) * 2;
            int actualDamage = baseDamage + (comboCount - 1) * 3;

            playGoblinHurtAnimation();
            goblinHp -= actualDamage;
            goblinHpBar.setProgress(Math.max(goblinHp, 0));

            int scoreGain = actualDamage * comboCount * gameLevel;
            gameScore += scoreGain;

            gainExperience(actualDamage / 2);
            totalDamageDealt += actualDamage;

            pushGoblinAway();
            createDamageNumber(goblin.getX(), goblin.getY() - 30, String.valueOf(actualDamage), false);

            if (comboCount > 3) {
                createDamageNumber(goblin.getX() + 50, goblin.getY() - 50, comboCount + " HIT COMBO!", true);
                startCameraShake(comboCount * 2f, 200);
            }

            Toast.makeText(requireContext(),
                    comboCount + " 连击！伤害: " + actualDamage + " (+"+scoreGain+"分)", Toast.LENGTH_SHORT).show();

            if (goblinHp <= 0) {
                playGoblinDieAnimation();
            }
        }
    }

    private void performGoblinAttack() {
        if (isGoblinAttacking || isGoblinDead) return;
        isGoblinAttacking = true;

        playSound(R.raw.goblin_attack_sound);

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

            if (!isMarioDead && !isMarioInvincible && isInAttackRange()) {
                if (isShielded) {
                    Toast.makeText(requireContext(), "护盾抵挡了攻击！", Toast.LENGTH_SHORT).show();
                    createDamageNumber(mario.getX(), mario.getY() - 30, "BLOCKED!", true);
                    return;
                }

                int damage = 25 + (gameLevel - 1) * 3;
                marioHp -= damage;
                marioHpBar.setProgress(Math.max(marioHp, 0));
                totalDamageTaken += damage;

                createDamageNumber(mario.getX(), mario.getY() - 30, "-" + damage, false);
                comboCount = 0;

                if (marioHp <= 0) {
                    playMarioDieAnimation();
                } else {
                    playMarioHurtAnimation();
                    pushMarioAway();
                }

                startCameraShake(8f, 400);
                Toast.makeText(requireContext(), "哥布林攻击！伤害: " + damage, Toast.LENGTH_SHORT).show();
            }
        }, attackDuration);
    }

    // ---------------------------------- 碰撞和移动系统 ---------------------------------------
    private boolean isInAttackRange() {
        if (isGoblinDead) return false;

        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float distance = getDistance(marioX, marioY, goblinX, goblinY);
        return distance < ATTACK_DISTANCE;
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

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

            newX = Math.max(0, Math.min(newX, screenWidth - goblin.getWidth()));
            newY = Math.max(0, Math.min(newY, screenHeight - goblin.getHeight()));

            goblin.setX(newX);
            goblin.setY(newY);
            updateGoblinHpBarPosition();
        }
    }

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

            newX = Math.max(0, Math.min(newX, screenWidth - marioWidth));
            newY = Math.max(0, Math.min(newY, screenHeight - marioHeight));

            mario.setX(newX);
            mario.setY(newY);
            marioHpBar.setX(newX);
            marioHpBar.setY(newY - 20);
        }
    }

    private void handleCollision() {
        if (isMarioDead || isGoblinDead) return;

        float marioX = mario.getX() + marioWidth / 2f;
        float marioY = mario.getY() + marioHeight / 2f;
        float goblinX = goblin.getX() + goblinWidth / 2f;
        float goblinY = goblin.getY() + goblinHeight / 2f;

        float distance = getDistance(marioX, marioY, goblinX, goblinY);

        if (distance < COLLISION_DISTANCE) {
            float dx = goblinX - marioX;
            float dy = goblinY - marioY;

            if (distance > 0) {
                dx /= distance;
                dy /= distance;

                float separationDistance = COLLISION_DISTANCE - distance;
                float separationForce = separationDistance * 0.5f;

                float marioNewX = mario.getX() - dx * separationForce;
                float marioNewY = mario.getY() - dy * separationForce;
                float goblinNewX = goblin.getX() + dx * separationForce;
                float goblinNewY = goblin.getY() + dy * separationForce;

                marioNewX = Math.max(0, Math.min(marioNewX, screenWidth - marioWidth));
                marioNewY = Math.max(0, Math.min(marioNewY, screenHeight - marioHeight));
                goblinNewX = Math.max(0, Math.min(goblinNewX, screenWidth - goblinWidth));
                goblinNewY = Math.max(0, Math.min(goblinNewY, screenHeight - goblinHeight));

                mario.setX(marioNewX);
                mario.setY(marioNewY);
                goblin.setX(goblinNewX);
                goblin.setY(goblinNewY);

                marioHpBar.setX(marioNewX);
                marioHpBar.setY(marioNewY - 20);
                updateGoblinHpBarPosition();
            }
        }
    }

    private void moveMario(float xPercent, float yPercent) {
        if (isMarioDead || isMarioHurt) return;

        float actualSpeed = marioSpeed * currentSpeedMultiplier;
        float deltaX = xPercent * actualSpeed;
        float deltaY = yPercent * actualSpeed;

        float newX = mario.getX() + deltaX;
        float newY = mario.getY() + deltaY;

        newX = Math.max(0, Math.min(newX, screenWidth - marioWidth));
        newY = Math.max(0, Math.min(newY, screenHeight - marioHeight));

        if (!checkObstacleCollision(newX, newY, marioWidth, marioHeight)) {
            mario.setX(newX);
            mario.setY(newY);
            marioHpBar.setX(newX);
            marioHpBar.setY(newY - 20);
        }
    }

    // ---------------------------------- AI系统 ---------------------------------------
    private void startGoblinAI() {
        if (goblinAIHandler != null) return;
        goblinAIHandler = new Handler();
        goblinAIRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && !isGoblinDead) {
                    enhanceGoblinAI();
                    handleCollision();
                    checkPowerUpCollision();
                }
                goblinAIHandler.postDelayed(this, GOBLIN_UPDATE_INTERVAL);
            }
        };
        goblinAIHandler.post(goblinAIRunnable);
    }

    private void stopGoblinAI() {
        if (goblinAIHandler != null && goblinAIRunnable != null) {
            goblinAIHandler.removeCallbacks(goblinAIRunnable);
            goblinAIHandler = null;
            goblinAIRunnable = null;
        }
    }

    private void enhanceGoblinAI() {
        if (mario == null || goblin == null || !isAdded() || goblinHp <= 0 || isMarioDead) return;

        float marioCenterX = mario.getX() + marioWidth / 2f;
        float marioCenterY = mario.getY() + marioHeight / 2f;
        float goblinCenterX = goblin.getX() + goblinWidth / 2f;
        float goblinCenterY = goblin.getY() + goblinHeight / 2f;

        float dx = marioCenterX - goblinCenterX;
        float dy = marioCenterY - goblinCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        long currentTime = System.currentTimeMillis();

        switch (goblinState) {
            case CHASING:
                if (distance < ATTACK_DISTANCE && currentTime - lastGoblinAttackTime > GOBLIN_ATTACK_COOLDOWN) {
                    goblinState = GoblinState.ATTACKING;
                    performGoblinAttack();
                    lastGoblinAttackTime = currentTime;
                } else if (goblinHp < goblinMaxHp * 0.3f) {
                    goblinState = GoblinState.CIRCLING;
                    stateChangeTime = currentTime;
                }
                break;

            case ATTACKING:
                if (currentTime - lastGoblinAttackTime > 500) {
                    goblinState = GoblinState.RETREATING;
                    stateChangeTime = currentTime;
                }
                break;

            case RETREATING:
                if (currentTime - stateChangeTime > 1000) {
                    goblinState = GoblinState.CHASING;
                }
                break;

            case CIRCLING:
                if (currentTime - stateChangeTime > 3000) {
                    goblinState = GoblinState.CHASING;
                }
                break;
        }

        executeGoblinMovement(dx, dy, distance);
    }

    private void executeGoblinMovement(float dx, float dy, float distance) {
        float moveX = 0, moveY = 0;

        switch (goblinState) {
            case CHASING:
                if (distance > COLLISION_DISTANCE) {
                    moveX = dx / distance * goblinSpeed;
                    moveY = dy / distance * goblinSpeed;
                }
                break;

            case RETREATING:
                if (distance < 200) {
                    moveX = -dx / distance * goblinSpeed * 0.8f;
                    moveY = -dy / distance * goblinSpeed * 0.8f;
                }
                break;

            case CIRCLING:
                circleAngle += 0.1f;
                float circleRadius = 120;
                float targetX = mario.getX() + (float) Math.cos(circleAngle) * circleRadius;
                float targetY = mario.getY() + (float) Math.sin(circleAngle) * circleRadius;

                float circleDx = targetX - goblin.getX();
                float circleDy = targetY - goblin.getY();
                float circleDistance = (float) Math.sqrt(circleDx * circleDx + circleDy * circleDy);

                if (circleDistance > 5) {
                    moveX = circleDx / circleDistance * goblinSpeed;
                    moveY = circleDy / circleDistance * goblinSpeed;
                }
                break;
        }

        if (Math.abs(moveX) > 0.1f || Math.abs(moveY) > 0.1f) {
            float newX = goblin.getX() + moveX;
            float newY = goblin.getY() + moveY;

            newX = Math.max(0, Math.min(newX, screenWidth - goblin.getWidth()));
            newY = Math.max(0, Math.min(newY, screenHeight - goblin.getHeight()));

            goblin.setX(newX);
            goblin.setY(newY);
            updateGoblinHpBarPosition();

            JoystickView.Direction newDirection = determineGoblinDirection(moveX, moveY);
            if (newDirection != goblinLastDirection) {
                goblinLastDirection = newDirection;
                updateGoblinDirection(newDirection);
            }
            updateGoblinAnimation(true);
        } else {
            updateGoblinAnimation(false);
        }
    }

    // ---------------------------------- UI更新 ---------------------------------------
    private void updateUI() {
        if (!isAdded()) return;
        if (scoreText != null) scoreText.setText("分数: " + gameScore);
        if (levelText != null) levelText.setText("游戏等级: " + gameLevel);
        if (playerLevelText != null) playerLevelText.setText("角色 Lv." + playerLevel);
        if (expText != null) expText.setText("经验: " + experience + "/" + experienceToNextLevel);

        if (comboText != null) {
            if (comboCount > 1) {
                comboText.setText(comboCount + " 连击!");
                comboText.setVisibility(View.VISIBLE);
                if (comboCount >= 10) comboText.setTextColor(0xFFFFD700);
                else if (comboCount >= 5) comboText.setTextColor(0xFFFF4500);
                else comboText.setTextColor(0xFFFF0000);
            } else {
                comboText.setVisibility(View.GONE);
            }
        }
    }

    // ---------------------------------- 动画系统 ---------------------------------------
    private void updateGoblinHpBarPosition() {
        if (goblinHpBar == null || goblin == null) return;
        float x = goblin.getX();
        float y = goblin.getY() - goblinHpBar.getHeight() - 4;
        goblinHpBar.setX(x);
        goblinHpBar.setY(y);
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

    private void updateGoblinDirection(JoystickView.Direction dir) {
        if (dir == JoystickView.Direction.LEFT) {
            goblin.setScaleX(-1);
        } else if (dir == JoystickView.Direction.RIGHT) {
            goblin.setScaleX(1);
        }
    }

    private void updateGoblinAnimation(boolean isMovingNow) {
        if (isGoblinWalking == isMovingNow || isGoblinAttacking || isGoblinHurt) return;
        isGoblinWalking = isMovingNow;

        if (isGoblinWalking) {
            if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
                currentGoblinWalkAnimation.stop();
            }
            int animResId = getGoblinWalkAnimationResId(goblinLastDirection);
            Drawable drawable = ContextCompat.getDrawable(requireContext(), animResId);
            if (drawable != null) drawable = drawable.mutate();
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

    private void updateMarioAnimation(boolean isMovingNow) {
        if (isMarioWalking == isMovingNow || isMarioAttacking || isMarioHurt) return;
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
            mario.setImageDrawable(standDrawable);
        }
    }

    private void updateMarioDirection(JoystickView.Direction marioDir) {
        if (marioDir == JoystickView.Direction.LEFT) mario.setScaleX(-1);
        else if (marioDir == JoystickView.Direction.RIGHT) mario.setScaleX(1);
    }

    // ---------------------------------- 受伤和死亡动画 ---------------------------------------
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

        goblin.postDelayed(() -> isGoblinInvincible = false, 1500);
    }

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

        mario.postDelayed(() -> isMarioInvincible = false, 1500);
    }

    private void playGoblinDieAnimation() {
        if (isGoblinDead) return;
        isGoblinDead = true;
        gameRunning = false;
        timerRunning = false;

        if (currentGoblinWalkAnimation != null && currentGoblinWalkAnimation.isRunning()) {
            currentGoblinWalkAnimation.stop();
        }

        int dieAnimResId = getGoblinDieAnimationResId(goblinLastDirection);
        AnimationDrawable dieAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), dieAnimResId);
        goblin.setImageDrawable(dieAnimation);
        dieAnimation.start();

        int duration = getAnimationDuration(dieAnimation);

        goblin.postDelayed(() -> {
            goblin.setVisibility(View.GONE);
            goblinHpBar.setVisibility(View.GONE);

            survivalTime = System.currentTimeMillis() - gameStartTime;
            long minutes = survivalTime / 60000;
            long seconds = (survivalTime % 60000) / 1000;

            int victoryBonus = gameLevel * 1000 + playerLevel * 500;
            gameScore += victoryBonus;
            updateUI();

            String stats = String.format(
                    "🎉 胜利！🎉\n\n" +
                            "📊 战斗统计:\n" +
                            "• 最终分数: %d\n" +
                            "• 角色等级: Lv.%d\n" +
                            "• 生存时间: %02d:%02d\n" +
                            "• 造成伤害: %d\n" +
                            "• 承受伤害: %d\n" +
                            "• 最高连击: %d\n" +
                            "• 等级奖励: %d\n\n" +
                            "🏆 表现评价: %s",
                    gameScore, playerLevel, minutes, seconds,
                    totalDamageDealt, totalDamageTaken, maxCombo, victoryBonus,
                    getPerformanceRating()
            );

            Toast.makeText(requireContext(), stats, Toast.LENGTH_LONG).show();

            if (bgmPlayer != null && bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }

            playSound(R.raw.victory_sound);
            startCameraShake(10f, 1000);
            createDamageNumber(screenWidth / 2f, screenHeight / 2f, "VICTORY!", true);
        }, duration);
    }

    private void playMarioDieAnimation() {
        if (isMarioDead) return;
        isMarioDead = true;
        gameRunning = false;
        timerRunning = false;

        if (currentMarioWalkAnimation != null && currentMarioWalkAnimation.isRunning()) {
            currentMarioWalkAnimation.stop();
        }

        int dieAnimResId = getMarioDieAnimationResId(marioLastDirection);
        AnimationDrawable dieAnimation = (AnimationDrawable) ContextCompat.getDrawable(requireContext(), dieAnimResId);
        mario.setImageDrawable(dieAnimation);
        dieAnimation.start();

        int duration = getAnimationDuration(dieAnimation);

        mario.postDelayed(() -> {
            mario.setVisibility(View.GONE);
            marioHpBar.setVisibility(View.GONE);

            survivalTime = System.currentTimeMillis() - gameStartTime;
            long minutes = survivalTime / 60000;
            long seconds = (survivalTime % 60000) / 1000;

            String stats = String.format(
                    "💀 失败 💀\n\n" +
                            "📊 战斗统计:\n" +
                            "• 最终分数: %d\n" +
                            "• 角色等级: Lv.%d\n" +
                            "• 生存时间: %02d:%02d\n" +
                            "• 造成伤害: %d\n" +
                            "• 承受伤害: %d\n" +
                            "• 最高连击: %d\n\n" +
                            "💡 提示: %s",
                    gameScore, playerLevel, minutes, seconds,
                    totalDamageDealt, totalDamageTaken, maxCombo,
                    getGameTip()
            );

            Toast.makeText(requireContext(), stats, Toast.LENGTH_LONG).show();

            if (bgmPlayer != null && bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }

            playSound(R.raw.defeat_sound);
        }, duration);
    }

    // ---------------------------------- 攻击动画 ---------------------------------------
    private void playMarioAttackAnimation() {
        if (isMarioAttacking || isMarioHurt || isMarioDead) return;
        isMarioAttacking = true;

        playSound(R.raw.mario_attack_sound);

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
            if (!isMarioDead && !isMarioHurt) {
                if (isMarioWalking) updateMarioAnimation(true);
                else mario.setImageDrawable(standDrawable);
            }
        }, attackDuration);
    }

    // ---------------------------------- 资源获取方法 ---------------------------------------
    private int getPowerUpDrawable(PowerUp.Type type) {
        switch (type) {
            case SPEED_BOOST: return R.drawable.powerup_speed;
            case ATTACK_BOOST: return R.drawable.powerup_attack;
            case HEALTH_POTION: return R.drawable.powerup_health;
            case SHIELD: return R.drawable.powerup_shield;
            case EXPERIENCE_ORB: return R.drawable.powerup_exp;
            case FREEZE_TIME: return R.drawable.powerup_freeze;
            default: return R.drawable.powerup_health;
        }
    }

    private int getObstacleDrawable(Obstacle.Type type) {
        switch (type) {
            case ROCK: return R.drawable.obstacle_rock;
            case TREE: return R.drawable.obstacle_tree;
            case WALL: return R.drawable.obstacle_wall;
            default: return R.drawable.obstacle_rock;
        }
    }

    private int getGoblinWalkAnimationResId(JoystickView.Direction goblinDir) {
        switch (goblinDir) {
            case UP: return R.drawable.goblin_walk_up;
            case DOWN: return R.drawable.goblin_walk_down;
            case LEFT: return R.drawable.goblin_walk_left;
            case RIGHT: default: return R.drawable.goblin_walk_right;
        }
    }

    private int getGoblinAttackAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_attack_up;
            case DOWN: return R.drawable.goblin_attack_down;
            case LEFT: return R.drawable.goblin_attack_left;
            case RIGHT: default: return R.drawable.goblin_attack_right;
        }
    }

    private int getGoblinHurtAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_hurt_up;
            case DOWN: return R.drawable.goblin_hurt_down;
            case LEFT: return R.drawable.goblin_hurt_left;
            case RIGHT: default: return R.drawable.goblin_hurt_right;
        }
    }

    private int getGoblinDieAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.goblin_die_up;
            case DOWN: return R.drawable.goblin_die_down;
            case LEFT: return R.drawable.goblin_die_left;
            case RIGHT: default: return R.drawable.goblin_die_right;
        }
    }

    private int getMarioWalkAnimationResId(JoystickView.Direction marioDir) {
        switch (marioDir) {
            case UP: return R.drawable.mario_walk_up;
            case DOWN: return R.drawable.mario_walk_down;
            case LEFT: return R.drawable.mario_walk_left;
            case RIGHT: default: return R.drawable.mario_walk_right;
        }
    }

    private int getMarioAttackAnimationResId(JoystickView.Direction marioDir) {
        if (marioDir == JoystickView.Direction.CENTER) marioDir = JoystickView.Direction.RIGHT;
        switch (marioDir) {
            case UP: return R.drawable.mario_attack_up;
            case DOWN: return R.drawable.mario_attack_down;
            case LEFT: return R.drawable.mario_attack_left;
            case RIGHT: default: return R.drawable.mario_attack_right;
        }
    }

    private int getMarioHurtAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.mario_hurt_up;
            case DOWN: return R.drawable.mario_hurt_down;
            case LEFT: return R.drawable.mario_hurt_left;
            case RIGHT: default: return R.drawable.mario_hurt_right;
        }
    }

    private int getMarioDieAnimationResId(JoystickView.Direction dir) {
        switch (dir) {
            case UP: return R.drawable.mario_die_up;
            case DOWN: return R.drawable.mario_die_down;
            case LEFT: return R.drawable.mario_die_left;
            case RIGHT: default: return R.drawable.mario_die_right;
        }
    }

    // ---------------------------------- 工具方法 ---------------------------------------
    private int getAnimationDuration(AnimationDrawable animation) {
        int duration = 0;
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            duration += animation.getDuration(i);
        }
        return duration;
    }

    private String getPerformanceRating() {
        if (gameScore >= 10000 && maxCombo >= 10) return "🌟 完美表现！";
        if (gameScore >= 7000 && maxCombo >= 7) return "🔥 优秀表现！";
        if (gameScore >= 5000) return "👍 良好表现！";
        if (gameScore >= 3000) return "😊 不错的表现";
        return "💪 继续加油！";
    }

    private String getGameTip() {
        if (maxCombo < 3) return "尝试连续攻击获得连击加成！";
        if (totalDamageTaken > totalDamageDealt) return "多利用冲刺和道具来避免受伤！";
        if (playerLevel == 1) return "击败敌人可以获得经验值升级！";
        return "收集道具能大幅提升战斗能力！";
    }

    private void playSound(int soundRes) {
        if (!isMuted) {
            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), soundRes);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            }
        }
    }

    // ---------------------------------- 音频系统 ---------------------------------------
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

    private void playBackgroundMusic() {
        if (bgmPlayer != null) return;

        bgmPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = requireContext().getResources().openRawResourceFd(R.raw.game_bgm);
            if (afd == null) {
                Log.e(TAG, "Background music resource not found");
                return;
            }
            bgmPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            bgmPlayer.setLooping(true);
            bgmPlayer.setVolume(isMuted ? 0f : 0.5f, isMuted ? 0f : 0.5f);

            bgmPlayer.setOnPreparedListener(mp -> {
                if (isAdded()) {
                    mp.start();
                    isBgmPlaying = true;
                }
            });

            bgmPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                return true;
            });

            bgmPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Failed to play background music", e);
            if (bgmPlayer != null) {
                bgmPlayer.release();
                bgmPlayer = null;
            }
        }
    }

    // ---------------------------------- 清理资源 ---------------------------------------
    @Override
    public void onPause() {
        super.onPause();
        // Pause game and background music
        gameRunning = false;
        timerRunning = false;
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
            isBgmPlaying = false;
        }
        // Stop handlers to prevent updates while paused
        if (gameTimer != null && gameTimerRunnable != null) {
            gameTimer.removeCallbacks(gameTimerRunnable);
        }
        if (powerUpSpawner != null && powerUpSpawnRunnable != null) {
            powerUpSpawner.removeCallbacks(powerUpSpawnRunnable);
        }
        if (particleHandler != null && particleUpdateRunnable != null) {
            particleHandler.removeCallbacks(particleUpdateRunnable);
        }
        stopGoblinAI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && gameRunning) {
            // Resume game systems
            startGameSystems();
            if (bgmPlayer != null && !isBgmPlaying && !isMuted) {
                bgmPlayer.start();
                isBgmPlaying = true;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gameRunning = false;
        timerRunning = false;

        // 清理音频资源
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

        if (gameTimer != null && gameTimerRunnable != null) {
            gameTimer.removeCallbacks(gameTimerRunnable);
            gameTimer = null;
            gameTimerRunnable = null;
        }

        if (powerUpSpawner != null && powerUpSpawnRunnable != null) {
            powerUpSpawner.removeCallbacks(powerUpSpawnRunnable);
            powerUpSpawner = null;
            powerUpSpawnRunnable = null;
        }

        if (particleHandler != null && particleUpdateRunnable != null) {
            particleHandler.removeCallbacks(particleUpdateRunnable);
            particleHandler = null;
            particleUpdateRunnable = null;
        }

        // 停止AI
        stopGoblinAI();

        if (getView() != null) {
            ViewGroup container = (ViewGroup) getView();
            for (PowerUp powerUp : powerUps) {
                if (powerUp.imageView != null) {
                    container.removeView(powerUp.imageView);
                }
            }
            for (Obstacle obstacle : obstacles) {
                if (obstacle.imageView != null) {
                    container.removeView(obstacle.imageView);
                }
            }
            for (ParticleEffect particle : particles) {
                if (particle.isText && particle.textView != null) {
                    container.removeView(particle.textView);
                } else if (!particle.isText && particle.imageView != null) {
                    container.removeView(particle.imageView);
                }
            }
        }

        // 停止所有计时器
        if (gameTimer != null && gameTimerRunnable != null) {
            gameTimer.removeCallbacks(gameTimerRunnable);
        }

        if (powerUpSpawner != null && powerUpSpawnRunnable != null) {
            powerUpSpawner.removeCallbacks(powerUpSpawnRunnable);
        }

        if (particleHandler != null && particleUpdateRunnable != null) {
            particleHandler.removeCallbacks(particleUpdateRunnable);
        }

        // 清理道具
        for (PowerUp powerUp : powerUps) {
            removePowerUp(powerUp);
        }
        powerUps.clear();

        // 清理障碍物
        ViewGroup container = (ViewGroup) getView();
        if (container != null) {
            for (Obstacle obstacle : obstacles) {
                if (obstacle.imageView != null) {
                    container.removeView(obstacle.imageView);
                }
            }
        }
        obstacles.clear();

        // 清理粒子效果
        if (container != null) {
            for (ParticleEffect particle : particles) {
                if (particle.isText && particle.textView != null) {
                    container.removeView(particle.textView);
                } else if (!particle.isText && particle.imageView != null) {
                    container.removeView(particle.imageView);
                }
            }
        }
        particles.clear();

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}