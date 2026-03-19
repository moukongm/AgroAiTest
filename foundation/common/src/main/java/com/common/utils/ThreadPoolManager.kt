package com.common.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 全局线程池管理工具
 * 针对不同业务场景提供不同特性的线程池，保证线程安全、复用，防止 OOM 和线程饥饿。
 */
object ThreadPoolManager {
    private const val TAG = "ThreadPoolManager"

    // 获取 CPU 核心数
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    
    // CPU 密集型线程池参数
    private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private const val KEEP_ALIVE_SECONDS = 30L

    /**
     * IO 密集型线程池（网络请求、文件读写、数据库操作）
     * 特点：核心线程数为 0，最大线程数大（128），使用 SynchronousQueue。
     * 当有新任务时，如果没有空闲线程会立刻创建新线程，空闲线程超过 60s 会被回收。
     */
    val ioPool: ExecutorService by lazy {
        ThreadPoolExecutor(
            0, 128,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(),
            DefaultThreadFactory("Agro-IO"),
            ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程（提交任务的线程）执行
        )
    }

    /**
     * CPU 密集型线程池（复杂计算、图像处理、JSON解析）
     * 特点：核心线程数根据 CPU 核心数动态调整，使用有界队列 LinkedBlockingQueue。
     * 避免创建过多线程导致 CPU 频繁上下文切换。
     */
    val cpuPool: ExecutorService by lazy {
        ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue(128), // 有界队列，防止任务过多导致 OOM
            DefaultThreadFactory("Agro-CPU"),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    /**
     * 单线程池（需要保证顺序执行的任务，如日志写入本地、某些特殊的顺序队列）
     * 特点：只有一个核心线程，任务按 FIFO 顺序执行。
     */
    val singlePool: ExecutorService by lazy {
        Executors.newSingleThreadExecutor(DefaultThreadFactory("Agro-Single"))
    }

    /**
     * 调度线程池（用于延时任务或周期性任务）
     */
    val scheduledPool: ScheduledExecutorService by lazy {
        Executors.newScheduledThreadPool(
            CORE_POOL_SIZE,
            DefaultThreadFactory("Agro-Scheduled")
        )
    }

    /**
     * 自定义线程工厂，为线程命名，方便排查崩溃和性能分析
     */
    private class DefaultThreadFactory(private val namePrefix: String) : ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)

        init {
            val s = System.getSecurityManager()
            group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup!!
        }

        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r, "$namePrefix-Thread-${threadNumber.getAndIncrement()}", 0)
            // 确保不是守护线程
            if (t.isDaemon) t.isDaemon = false
            // 设置默认优先级
            if (t.priority != Thread.NORM_PRIORITY) {
                t.priority = Thread.NORM_PRIORITY
            }
            // 捕获未处理的异常，防止整个 App 崩溃
            t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, ex ->
                LogUtils.e(TAG, "Uncaught exception in thread ${thread.name}", ex)
            }
            return t
        }
    }
}
