package com.detection.data;

import android.content.Context;

import com.common.storage.database.AppDatabase;
import com.common.storage.database.DetectionRecord;
import com.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class UserLocalDataSource {

    private static final int MAX_RECORDS = 10;

    public void saveToLocalDatabase(String diseaseName, String localImagePath, Context context) {
        try {
            AppDatabase db = AppDatabase.Companion.getInstance(context);
            LogUtils.INSTANCE.d("ljx", "开始保存到数据库...");

            // 先获取当前记录数量
            int currentCount = db.detectionDao().getCount();
            LogUtils.INSTANCE.d("ljx", "当前记录数量: " + currentCount);

            // 如果已经达到最大数量，删除最早的记录
            if (currentCount >= MAX_RECORDS) {
                List<DetectionRecord> allRecords = db.detectionDao().getAllRecords();
                if (!allRecords.isEmpty()) {
                    // 按识别时间排序，删除最早的
                    db.detectionDao().delete(allRecords.get(0));
                    LogUtils.INSTANCE.d("ljx", "已删除最早的记录");
                }
            }

            // 创建新记录
            DetectionRecord record = new DetectionRecord(diseaseName, 0, localImagePath, System.currentTimeMillis());
            db.detectionDao().insert(record);

            LogUtils.INSTANCE.d("ljx", "已保存识别记录到本地: " + diseaseName + ", 本地路径: " + localImagePath);
        } catch (Exception e) {
            LogUtils.INSTANCE.d("ljx", "保存识别记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public List<DetectionRecord> getLocalRecords(Context context) {
        try {
            AppDatabase db = AppDatabase.Companion.getInstance(context);
            List<DetectionRecord> records = db.detectionDao().getAllRecords();
            LogUtils.INSTANCE.d("ljx", "读取本地记录数量: " + (records != null ? records.size() : 0));
            return records;
        } catch (Exception e) {
            LogUtils.INSTANCE.d("ljx", "读取本地识别记录失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
