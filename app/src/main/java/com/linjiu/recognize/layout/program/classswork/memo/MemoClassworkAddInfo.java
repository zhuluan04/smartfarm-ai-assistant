package com.linjiu.recognize.layout.program.classswork.memo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.linjiu.recognize.R;
import com.linjiu.recognize.helper.MyDbHelper;

import java.io.File;

public class MemoClassworkAddInfo extends Fragment {

    private EditText edit_title, edit_content;
    private Button btn_camera, btn_photo, btn_save;
    private ImageView img_preview;

    private String tmpPath;      // 拍照临时路径
    private String displayPath;  // 最终显示/保存的路径

    private MyDbHelper dbHelper;
    private SQLiteDatabase db;

    // 拍照结果回调
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    if (tmpPath != null) {
                        displayPath = tmpPath;
                        Glide.with(this).load(displayPath).into(img_preview);
                    }
                }
            }
    );

    // 图库结果回调
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        displayPath = getRealPathFromURI(uri);
                        Glide.with(this).load(uri).into(img_preview);
                    }
                }
            }
    );

    /**
     * 获取URI对应的真实路径
     */
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = requireContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.memo_classwork_add_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edit_title = view.findViewById(R.id.editText_title);
        edit_content = view.findViewById(R.id.editText_content);
        btn_camera = view.findViewById(R.id.button_camera);
        btn_photo = view.findViewById(R.id.button_photo);
        btn_save = view.findViewById(R.id.button_save);
        img_preview = view.findViewById(R.id.imageView_preview);

        dbHelper = new MyDbHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 检查是否为编辑模式
        Bundle args = getArguments();
        long memoId = -1;
        if (args != null) {
            memoId = args.getLong("memo_id", -1);
            if (memoId != -1) {
                edit_title.setText(args.getString("title", ""));
                edit_content.setText(args.getString("content", ""));
                displayPath = args.getString("imgpath", null);
                if (displayPath != null && !displayPath.isEmpty()) {
                    Glide.with(this).load(displayPath).into(img_preview);
                }
                btn_save.setText("更新");
            }
        }

        setupClickListeners(memoId);
    }

    private void setupClickListeners(long memoId) {
        // 拍照
        btn_camera.setOnClickListener(v -> {
            // 检查相机权限
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.CAMERA}, 100);
                return;
            }

            try {
                // 生成唯一文件名
                String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";

                File imgDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (imgDir != null && !imgDir.exists()) imgDir.mkdirs();

                File imgFile = new File(imgDir, fileName);
                tmpPath = imgFile.getAbsolutePath();

                Uri photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        imgFile
                );

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraLauncher.launch(intent);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "无法打开相机", Toast.LENGTH_SHORT).show();
            }
        });

        // 图库选择
        btn_photo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        // 保存按钮
        btn_save.setOnClickListener(v -> {
            String title = edit_title.getText().toString().trim();
            String content = edit_content.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "请输入标题", Toast.LENGTH_SHORT).show();
                return;
            }

            Time time = new Time();
            time.setToNow();
            String mtime = time.year + "/" + (time.month + 1) + "/" + time.monthDay;

            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("content", content);
            values.put("imgpath", displayPath);
            values.put("mtime", mtime);

            long result;
            if (memoId == -1) {
                result = db.insert("tb_memory", null, values);
            } else {
                result = db.update("tb_memory", values, "_id=?", new String[]{String.valueOf(memoId)});
            }

            if (result != -1) {
                Toast.makeText(requireContext(),
                        memoId == -1 ? "保存成功" : "更新成功", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
