package com.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class AvatarUtils {
    private static final String AVATAR_DIR = "avatars";
    private static final String AVATAR_PREFIX = "avatar_";
    private static final String HISTORY_DIR = "history_images";
    private static final String HISTORY_PREFIX = "History_";

    /**
     * 下载头像并保存到本地
     * @param context 上下文
     * @param avatarUrl 头像URL
     * @param userId 用户ID（用于生成文件名）
     * @return 本地文件路径，失败返回 null
     */
    public static String downloadAndSaveAvatar(Context context, String avatarUrl, long userId) {
        try {
            // 1. 下载图片
            URL url = new URL(avatarUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());

            if (bitmap == null) return null;

            // 2. 创建保存目录
            File dir = new File(context.getFilesDir(), AVATAR_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 3. 生成文件名：avatar_用户ID.png
            String filename = AVATAR_PREFIX + userId + ".png";
            File file = new File(dir, filename);

            // 4. 保存到文件
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            return file.getAbsolutePath();  // 返回本地路径

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从本地加载头像
     */
    public static Bitmap loadLocalAvatar(Context context, long userId) {
        String filename = AVATAR_PREFIX + userId + ".png";
        File file = new File(context.getFilesDir() + "/" + AVATAR_DIR, filename);

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    /**
     * 删除头像
     */
    public static boolean deleteAvatar(Context context, long userId) {
        String filename = AVATAR_PREFIX + userId + ".png";
        File file = new File(context.getFilesDir() + "/" + AVATAR_DIR, filename);
        return file.delete();
    }

    /**
     * 下载识别记录图片并保存到本地
     * @param context 上下文
     * @param imageUrl 图片网络URL
     * @param recordId 记录ID（用于生成文件名）
     * @return 本地文件路径，失败返回 null
     */
    public static String downloadAndSaveHistoryImage(Context context, String imageUrl, long recordId) {
        try {
            // 1. 下载图片
            URL url = new URL(imageUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());

            if (bitmap == null) return null;

            // 2. 创建保存目录
            File dir = new File(context.getFilesDir(), HISTORY_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 3. 生成文件名：History_记录ID.png
            String filename = HISTORY_PREFIX + recordId + ".png";
            File file = new File(dir, filename);

            // 4. 保存到文件
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            return file.getAbsolutePath();  // 返回本地路径

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除识别记录图片
     */
    public static boolean deleteHistoryImage(Context context, long recordId) {
        String filename = HISTORY_PREFIX + recordId + ".png";
        File file = new File(context.getFilesDir() + "/" + HISTORY_DIR, filename);
        return file.delete();
    }
}
