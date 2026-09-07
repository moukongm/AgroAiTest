package com.common.timedetect;

import com.common.utils.LogUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Entry point for starting, marking, reporting and aggregating time-cost traces.
 */
public final class TimeCostTracker {
    public static final String TAG = "TimeCost";

    private static volatile TimeCostTracker instance;

    private final ConcurrentHashMap<String, TraceSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AggregatedStats> globalStats = new ConcurrentHashMap<>();
    private final AtomicLong sessionSequence = new AtomicLong();
    private final TimeSource timeSource;
    private final TrackerLogger logger;

    public TimeCostTracker() {
        this(TimeSource.SYSTEM, TrackerLogger.LOGCAT);
    }

    TimeCostTracker(TimeSource timeSource, TrackerLogger logger) {
        this.timeSource = timeSource;
        this.logger = logger;
    }

    public static TimeCostTracker getInstance() {
        if (instance == null) {
            synchronized (TimeCostTracker.class) {
                if (instance == null) {
                    instance = new TimeCostTracker();
                }
            }
        }
        return instance;
    }

    public String startSession(String sceneName) {
        if (sceneName == null || sceneName.trim().isEmpty()) {
            throw new IllegalArgumentException("sceneName must not be blank");
        }
        String normalizedSceneName = sceneName.trim();
        String sessionId = normalizedSceneName
                + "_"
                + timeSource.now()
                + "_"
                + sessionSequence.incrementAndGet();
        TraceSession session = new TraceSession(normalizedSceneName, sessionId, timeSource);
        sessions.put(sessionId, session);
        logger.debug("🚀 开始追踪 - 场景: " + normalizedSceneName + ", ID: " + sessionId);
        return sessionId;
    }

    public void markNode(String sessionId, String nodeName) {
        if (sessionId == null) {
            logger.warn("忽略节点，追踪 ID 为空: node=" + nodeName);
            return;
        }
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            logger.warn("忽略节点，追踪不存在或已结束: " + sessionId + ", node=" + nodeName);
            return;
        }
        if (session.addNode(nodeName)) {
            logger.debug("📍 节点记录 - " + nodeName + ", 耗时: "
                    + session.getNodeDuration(nodeName) + "ms");
        }
    }

    /** Records the measured request/response duration for the server phase. */
    public void setServerCost(String sessionId, long serverCostMs) {
        if (sessionId == null) {
            return;
        }
        TraceSession session = sessions.get(sessionId);
        if (session != null) {
            session.setServerCostMs(serverCostMs);
        }
    }

    public TraceReport endSession(String sessionId) {
        return endSession(sessionId, "session_end");
    }

    public TraceReport endSession(String sessionId, String finalNodeName) {
        if (sessionId == null) {
            logger.warn("忽略结束操作，追踪 ID 为空");
            return null;
        }
        TraceSession session = sessions.remove(sessionId);
        if (session == null) {
            logger.warn("忽略结束操作，追踪不存在或已结束: " + sessionId);
            return null;
        }

        session.finish(finalNodeName);
        TraceReport report = session.generateReport();
        aggregateStats(report.sceneName, report);
        logger.debug("✅ 追踪结束 - " + report);
        logger.debug(report.toDetailString());
        return report;
    }

    /** Removes an abandoned trace without adding it to aggregate statistics. */
    public void cancelSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        TraceSession removed = sessions.remove(sessionId);
        if (removed != null) {
            logger.debug("⏹ 取消追踪 - ID: " + sessionId);
        }
    }

    public TraceSession getSession(String sessionId) {
        return sessionId == null ? null : sessions.get(sessionId);
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public AggregatedStats getGlobalStats(String sceneName) {
        return sceneName == null ? null : globalStats.get(sceneName);
    }

    public Map<String, AggregatedStats> getAllGlobalStats() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(globalStats));
    }

    public void printGlobalStats() {
        if (globalStats.isEmpty()) {
            logger.debug("暂无耗时统计");
            return;
        }
        for (AggregatedStats stats : globalStats.values()) {
            logger.debug("📊 " + stats);
        }
    }

    public void clearGlobalStats() {
        globalStats.clear();
    }

    private void aggregateStats(String sceneName, TraceReport report) {
        globalStats.compute(sceneName, (key, existing) -> {
            AggregatedStats stats = existing == null ? new AggregatedStats(key) : existing;
            stats.addReport(report);
            return stats;
        });
    }
}

interface TrackerLogger {
    TrackerLogger LOGCAT = new TrackerLogger() {
        @Override
        public void debug(String message) {
            LogUtils.INSTANCE.d(TimeCostTracker.TAG, message);
        }

        @Override
        public void warn(String message) {
            LogUtils.INSTANCE.w(TimeCostTracker.TAG, message, null);
        }
    };

    TrackerLogger NONE = new TrackerLogger() {
        @Override
        public void debug(String message) {
        }

        @Override
        public void warn(String message) {
        }
    };

    void debug(String message);

    void warn(String message);
}
