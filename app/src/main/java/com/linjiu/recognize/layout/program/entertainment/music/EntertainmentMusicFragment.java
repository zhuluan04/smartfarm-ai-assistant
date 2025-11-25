package com.linjiu.recognize.layout.program.entertainment.music;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linjiu.recognize.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntertainmentMusicFragment extends Fragment {

    private ImageView ivAlbumCover;
    private ImageView ivNeedle;
    private TextView tvSongName;
    private TextView tvArtist;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar seekBar;
    private FloatingActionButton fabPlayPause;
    private ImageView ivPrevious;
    private ImageView ivNext;
    private ImageView ivPlayMode;
    private RecyclerView rvLyrics;

    private MediaPlayer mediaPlayer;
    private ObjectAnimator rotationAnimator;
    private ObjectAnimator needleAnimator;
    private boolean isPlaying = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LyricsAdapter lyricsAdapter;
    private int playMode = 0; // 0:列表循环 1:单曲循环 2:随机播放

    private List<Song> playlist = new ArrayList<>();
    private int currentSongIndex = 0;
    private boolean isUserSeeking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entertainment_music_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initMediaPlayer();
        initData();
        setupListeners();
        setupAnimations();
    }

    private void initViews(View view) {
        ivAlbumCover = view.findViewById(R.id.iv_album_cover);
        ivNeedle = view.findViewById(R.id.iv_needle);
        tvSongName = view.findViewById(R.id.tv_song_name);
        tvArtist = view.findViewById(R.id.tv_artist);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        seekBar = view.findViewById(R.id.seek_bar);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);
        ivPrevious = view.findViewById(R.id.iv_previous);
        ivNext = view.findViewById(R.id.iv_next);
        ivPlayMode = view.findViewById(R.id.iv_play_mode);
        rvLyrics = view.findViewById(R.id.rv_lyrics);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnPreparedListener(mp -> {
            int duration = mp.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            onSongFinished();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(getContext(), "播放出错", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void initData() {
        // 初始化播放列表
        // 注意：这里使用 assets 文件夹中的音频文件
        // 音频文件命名格式：yequ.mp3
        playlist.add(new Song("夜曲", "周杰伦", "yequ.mp3", R.drawable.ic_default_album));

        // 你可以添加更多歌曲
        // playlist.add(new Song("晴天", "周杰伦", "qingtian.mp3", R.drawable.ic_default_album));

        loadSong(currentSongIndex);
    }

    private void loadSong(int index) {
        if (index < 0 || index >= playlist.size()) return;

        Song song = playlist.get(index);
        tvSongName.setText(song.getName());
        tvArtist.setText(song.getArtist());
        ivAlbumCover.setImageResource(song.getAlbumCover());

        // 重置播放状态
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }

        // 加载音频文件
        try {
            AssetFileDescriptor afd = getActivity().getAssets().openFd(song.getFilePath());
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "加载音乐失败", Toast.LENGTH_SHORT).show();
        }

        // 重置UI
        seekBar.setProgress(0);
        tvCurrentTime.setText("00:00");

        // 加载对应歌词
        loadLyrics(song.getName());
    }

    private void loadLyrics(String songName) {
        List<LyricLine> lyrics = new ArrayList<>();

        // 根据歌曲名称加载不同的歌词
        if (songName.equals("夜曲")) {
            lyrics.add(new LyricLine(0, "夜曲"));
            lyrics.add(new LyricLine(1000, "演唱：周杰伦"));
            lyrics.add(new LyricLine(3000, ""));
            lyrics.add(new LyricLine(15000, "一段简短的旋律..."));
            lyrics.add(new LyricLine(20000, "音乐在流淌..."));
            lyrics.add(new LyricLine(30000, "感受这首歌的节奏"));
            lyrics.add(new LyricLine(40000, "让心灵随之舞动"));
            lyrics.add(new LyricLine(50000, ""));
            lyrics.add(new LyricLine(60000, "旋律在耳边回响"));
            lyrics.add(new LyricLine(70000, "沉浸在音乐中"));
            lyrics.add(new LyricLine(80000, ""));
        }

        if (lyricsAdapter == null) {
            lyricsAdapter = new LyricsAdapter(lyrics);
            rvLyrics.setLayoutManager(new LinearLayoutManager(getContext()));
            rvLyrics.setAdapter(lyricsAdapter);
        } else {
            lyricsAdapter.updateLyrics(lyrics);
        }
    }

    private void setupListeners() {
        fabPlayPause.setOnClickListener(v -> togglePlayPause());

        ivPrevious.setOnClickListener(v -> playPrevious());

        ivNext.setOnClickListener(v -> playNext());

        ivPlayMode.setOnClickListener(v -> switchPlayMode());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                isUserSeeking = false;
            }
        });
    }

    private void setupAnimations() {
        // 唱片旋转动画
        rotationAnimator = ObjectAnimator.ofFloat(ivAlbumCover, "rotation", 0f, 360f);
        rotationAnimator.setDuration(20000);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);

        // 设置唱针pivot点
        ivNeedle.post(() -> {
            ivNeedle.setPivotX(ivNeedle.getWidth() * 0.2f);
            ivNeedle.setPivotY(0);
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        if (mediaPlayer == null) return;

        try {
            mediaPlayer.start();
            isPlaying = true;
            fabPlayPause.setImageResource(R.drawable.ic_pause);

            // 启动唱片旋转
            if (rotationAnimator.isPaused()) {
                rotationAnimator.resume();
            } else {
                rotationAnimator.start();
            }

            // 唱针落下
            needleAnimator = ObjectAnimator.ofFloat(ivNeedle, "rotation", -25f, 0f);
            needleAnimator.setDuration(500);
            needleAnimator.start();

            // 更新进度
            updateProgress();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void pause() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) return;

        mediaPlayer.pause();
        isPlaying = false;
        fabPlayPause.setImageResource(R.drawable.ic_play);

        // 暂停旋转
        rotationAnimator.pause();

        // 唱针抬起
        needleAnimator = ObjectAnimator.ofFloat(ivNeedle, "rotation", 0f, -25f);
        needleAnimator.setDuration(500);
        needleAnimator.start();

        handler.removeCallbacksAndMessages(null);
    }

    private void updateProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying && !isUserSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));

                    // 更新歌词
                    if (lyricsAdapter != null) {
                        lyricsAdapter.updateCurrentTime(currentPosition);
                    }

                    updateProgress();
                }
            }
        }, 100); // 每100ms更新一次，更流畅
    }

    private void onSongFinished() {
        switch (playMode) {
            case 0: // 列表循环
                playNext();
                break;
            case 1: // 单曲循环
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(0);
                    play();
                }
                break;
            case 2: // 随机播放
                currentSongIndex = (int) (Math.random() * playlist.size());
                loadSong(currentSongIndex);
                play();
                break;
        }
    }

    private void playPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
        } else {
            currentSongIndex = playlist.size() - 1;
        }
        loadSong(currentSongIndex);
        if (isPlaying) {
            play();
        }
    }

    private void playNext() {
        if (currentSongIndex < playlist.size() - 1) {
            currentSongIndex++;
        } else {
            currentSongIndex = 0;
        }
        loadSong(currentSongIndex);
        if (isPlaying) {
            play();
        }
    }

    private void switchPlayMode() {
        playMode = (playMode + 1) % 3;
        switch (playMode) {
            case 0:
                ivPlayMode.setImageResource(R.drawable.ic_repeat);
                Toast.makeText(getContext(), "列表循环", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                ivPlayMode.setImageResource(R.drawable.ic_repeat_one);
                Toast.makeText(getContext(), "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                ivPlayMode.setImageResource(R.drawable.ic_shuffle);
                Toast.makeText(getContext(), "随机播放", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPlaying) {
            pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (rotationAnimator != null) {
            rotationAnimator.cancel();
        }

        handler.removeCallbacksAndMessages(null);
    }

    // 歌曲数据类
    private static class Song {
        private String name;
        private String artist;
        private String filePath;
        private int albumCover;

        public Song(String name, String artist, String filePath, int albumCover) {
            this.name = name;
            this.artist = artist;
            this.filePath = filePath;
            this.albumCover = albumCover;
        }

        public String getName() { return name; }
        public String getArtist() { return artist; }
        public String getFilePath() { return filePath; }
        public int getAlbumCover() { return albumCover; }
    }

    // 歌词行数据类
    private static class LyricLine {
        private int timestamp;
        private String text;

        public LyricLine(int timestamp, String text) {
            this.timestamp = timestamp;
            this.text = text;
        }

        public int getTimestamp() { return timestamp; }
        public String getText() { return text; }
    }

    // 歌词适配器
    private static class LyricsAdapter extends RecyclerView.Adapter<LyricsAdapter.LyricViewHolder> {
        private List<LyricLine> lyrics;
        private int currentPosition = -1;

        public LyricsAdapter(List<LyricLine> lyrics) {
            this.lyrics = lyrics;
        }

        public void updateLyrics(List<LyricLine> newLyrics) {
            this.lyrics = newLyrics;
            this.currentPosition = -1;
            notifyDataSetChanged();
        }

        public void updateCurrentTime(int currentTime) {
            int newPosition = -1;
            for (int i = 0; i < lyrics.size(); i++) {
                if (currentTime >= lyrics.get(i).getTimestamp()) {
                    newPosition = i;
                } else {
                    break;
                }
            }
            if (newPosition != currentPosition) {
                int oldPosition = currentPosition;
                currentPosition = newPosition;
                if (oldPosition >= 0) notifyItemChanged(oldPosition);
                if (currentPosition >= 0) notifyItemChanged(currentPosition);
            }
        }

        @NonNull
        @Override
        public LyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(0, 20, 0, 20);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return new LyricViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull LyricViewHolder holder, int position) {
            LyricLine lyric = lyrics.get(position);
            holder.textView.setText(lyric.getText());

            if (position == currentPosition) {
                holder.textView.setTextColor(0xFFDD001B);
                holder.textView.setTextSize(18);
                holder.textView.setAlpha(1.0f);
            } else {
                holder.textView.setTextColor(0xFF999999);
                holder.textView.setTextSize(14);
                holder.textView.setAlpha(0.6f);
            }
        }

        @Override
        public int getItemCount() {
            return lyrics.size();
        }

        static class LyricViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            LyricViewHolder(TextView itemView) {
                super(itemView);
                textView = itemView;
            }
        }
    }
}
