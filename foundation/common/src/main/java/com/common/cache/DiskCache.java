package com.common.cache;

import com.google.gson.Gson;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DiskCache<T> {

    private static final String TAG = "DiskCache";

    private final DiskLruCache diskLruCache;
    private final Gson gson;
    private final Type type;

    public DiskCache(File cacheDir, long maxSize, Type type) {
        this.type = type;
        this.gson = new Gson();
        try {
            this.diskLruCache = DiskLruCache.open(cacheDir, 1, 1, maxSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public T get(String key) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            String hashkey = md5(key);
            snapshot = diskLruCache.get(hashkey);
            if(snapshot == null) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(snapshot.getInputStream(0)));
            try {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                return gson.fromJson(json.toString(), type);
            } finally {
                reader.close();
                snapshot.close();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void setCache(String key, T value) {
        DiskLruCache.Editor editor = null;
        try {
            String hashKey = md5(key);
            editor = diskLruCache.edit(hashKey);
            if(editor == null) {
                return;
            }

            String json = gson.toJson(value);
            editor.set(0, json);
            editor.commit();
        } catch (Exception e) {
            if(editor != null) {
                try {
                    editor.abort();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void remove(String key) {
        try {
            diskLruCache.remove(md5(key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        try {
            diskLruCache.delete();
        } catch (IOException e) {

        }
    }

    private String md5(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b :digest) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        }catch (NoSuchAlgorithmException e) {
            return String.valueOf(key.hashCode());
        }
    }


}
