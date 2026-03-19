package com.common.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * 全局线程工具类
 * 提供主线程切换、IO/CPU 线程任务分发等便捷方法，简化开发。
 */
object ThreadUtils {

    // 绑定到主线程的 Handler
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 判断当前是否在主线程
     */
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**
     * 在主线程执行任务
     * @param runnable 要执行的任务
     */
    fun runOnUiThread(runnable: Runnable) {
        if (isMainThread()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    /**
     * 延时在主线程执行任务
     * @param runnable 要执行的任务
     * @param delayMillis 延时时间（毫秒）
     */
    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
        mainHandler.postDelayed(runnable, delayMillis)
    }

    /**
     * 移除主线程队列中未执行的指定任务
     */
    fun removeUiThread(runnable: Runnable) {
        mainHandler.removeCallbacks(runnable)
    }

    /**
     * 提交 IO 密集型任务（如网络请求、大文件读写、数据库操作）
     */
    fun executeByIo(runnable: Runnable) {
        ThreadPoolManager.ioPool.execute(runnable)
    }

    /**
     * 提交 IO 密集型任务并返回 Future，便于获取结果或取消任务
     */
    fun <T> submitByIo(callable: Callable<T>): Future<T> {
        return ThreadPoolManager.ioPool.submit(callable)
    }

    /**
     * 提交 CPU 密集型任务（如图片处理、复杂数据计算、大量 JSON 解析）
     */
    fun executeByCpu(runnable: Runnable) {
        ThreadPoolManager.cpuPool.execute(runnable)
    }

    /**
     * 提交 CPU 密集型任务并返回 Future
     */
    fun <T> submitByCpu(callable: Callable<T>): Future<T> {
        return ThreadPoolManager.cpuPool.submit(callable)
    }

    /**
     * 提交单线程任务（保证按提交顺序先后执行，避免并发冲突）
     */
    fun executeBySingle(runnable: Runnable) {
        ThreadPoolManager.singlePool.execute(runnable)
    }

    /**
     * 在后台线程执行延时任务
     * @param delay 延时时间
     * @param unit 时间单位，默认为毫秒
     */
    fun executeDelayed(runnable: Runnable, delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        ThreadPoolManager.scheduledPool.schedule(runnable, delay, unit)
    }

    /**
     * 在后台线程执行周期性任务
     * @param initialDelay 首次执行的延时时间
     * @param period 两次执行的间隔时间
     * @param unit 时间单位
     */
    fun executeAtFixedRate(runnable: Runnable, initialDelay: Long, period: Long, unit: TimeUnit) {
        ThreadPoolManager.scheduledPool.scheduleAtFixedRate(runnable, initialDelay, period, unit)
    }
}
