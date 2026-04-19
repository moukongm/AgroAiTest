package com.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AvatarUtils {
    private static final String AVATAR_DIR = "avatars";
    private static final String AVATAR_PREFIX = "avatar_";
    private static final String HISTORY_DIR = "history_images";
    private static final String HISTORY_PREFIX = "History_";

    public static String downloadAndSaveAvatar(Context context, String avatarUrl, long userId) {
        return downloadAndSaveImage(context, avatarUrl, AVATAR_DIR, AVATAR_PREFIX + userId + ".png");
    }

    public static String downloadAndSaveHistoryImage(Context context, String imageUrl, long recordId) {
        return downloadAndSaveImage(context, imageUrl, HISTORY_DIR, HISTORY_PREFIX + recordId + ".png");
    }

    private static String downloadAndSaveImage(Context context, String imageUrl, String dirName, String filename) {
        InputStream stream = null;
        FileOutputStream fos = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageUrl);
            stream = url.openStream();
            bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap == null) return null;

            File dir = new File(context.getFilesDir(), dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, filename);
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
            if (stream != null) try { stream.close(); } catch (IOException ignored) {}
            if (bitmap != null) bitmap.recycle();
        }
    }

    public static boolean deleteAvatar(Context context, long userId) {
        String filename = AVATAR_PREFIX + userId + ".png";
        File file = new File(context.getFilesDir() + "/" + AVATAR_DIR, filename);
        return file.delete();
    }
}
