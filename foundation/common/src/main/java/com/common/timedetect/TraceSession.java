package com.common.timedetect;

import android.os.SystemClock;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single time-cost trace. The session only records data; reporting and aggregation
 * are intentionally handled by {@link TraceReport} and {@link TimeCostTracker}.
 */
public final class TraceSession {
    private final String sceneName;
    private final String sessionId;
    private final long startTimeMs;
    private final TimeSource timeSource;
    private final Map<String, Long> nodes = new LinkedHashMap<>();

    private long serverCostMs = -1L;
    private boolean finished;

    TraceSession(String sceneName, String sessionId, TimeSource timeSource) {
        this.sceneName = sceneName;
        this.sessionId = sessionId;
        this.timeSource = timeSource;
        this.startTimeMs = timeSource.now();
    }

    /** Records a named node and returns whether it was accepted. */
    synchronized boolean addNode(String nodeName) {
        if (finished || nodeName == null || nodeName.trim().isEmpty()) {
            return false;
        }
        nodes.put(nodeName, timeSource.now());
        return true;
    }

    synchronized void setServerCostMs(long serverCostMs) {
        if (!finished) {
            this.serverCostMs = Math.max(serverCostMs, 0L);
        }
    }

    /** Closes the trace with an explicit final node so total time is deterministic. */
    synchronized boolean finish(String finalNodeName) {
        if (finished) {
            return false;
        }
        String nodeName = finalNodeName == null || finalNodeName.trim().isEmpty()
                ? "session_end"
                : finalNodeName;
        nodes.put(nodeName, timeSource.now());
        finished = true;
        return true;
    }

    public synchronized Long getNodeDurationMs(String nodeName) {
        Long nodeTime = nodes.get(nodeName);
        return nodeTime == null ? null : Math.max(nodeTime - startTimeMs, 0L);
    }

    public synchronized String getNodeDuration(String nodeName) {
        Long durationMs = getNodeDurationMs(nodeName);
        return durationMs == null ? "没有该节点" : String.valueOf(durationMs);
    }

    public synchronized TraceReport generateReport() {
        return new TraceReport(
                sceneName,
                sessionId,
                startTimeMs,
                serverCostMs,
                new LinkedHashMap<>(nodes)
        );
    }

    public String getSceneName() {
        return sceneName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public synchronized long getServerCostMs() {
        return serverCostMs;
    }

    public synchronized boolean isFinished() {
        return finished;
    }
}

interface TimeSource {
    TimeSource SYSTEM = SystemClock::uptimeMillis;

    long now();
}
