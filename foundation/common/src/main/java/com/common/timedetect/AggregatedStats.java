package com.common.timedetect;

import java.util.Locale;

/** Thread-safe aggregate statistics for repeated executions of the same scene. */
public final class AggregatedStats {
    public final String sceneName;

    private long totalCount;
    private long serverSampleCount;
    private long totalClientCost;
    private long totalServerCost;
    private long maxClientCost;
    private long maxServerCost;
    private long minClientCost = Long.MAX_VALUE;
    private long minServerCost = Long.MAX_VALUE;

    AggregatedStats(String sceneName) {
        this.sceneName = sceneName;
    }

    synchronized void addReport(TraceReport report) {
        totalCount++;
        totalClientCost += report.clientCostMs;
        maxClientCost = Math.max(maxClientCost, report.clientCostMs);
        minClientCost = Math.min(minClientCost, report.clientCostMs);

        if (report.serverCostMs >= 0L) {
            serverSampleCount++;
            totalServerCost += report.serverCostMs;
            maxServerCost = Math.max(maxServerCost, report.serverCostMs);
            minServerCost = Math.min(minServerCost, report.serverCostMs);
        }
    }

    public synchronized long getAvgClientCost() {
        return totalCount == 0L ? 0L : totalClientCost / totalCount;
    }

    public synchronized long getAvgServerCost() {
        return serverSampleCount == 0L ? 0L : totalServerCost / serverSampleCount;
    }

    public synchronized long getCount() {
        return totalCount;
    }

    public synchronized long getServerSampleCount() {
        return serverSampleCount;
    }

    public synchronized long getMaxClientCost() {
        return maxClientCost;
    }

    public synchronized long getMaxServerCost() {
        return maxServerCost;
    }

    public synchronized long getMinClientCost() {
        return minClientCost == Long.MAX_VALUE ? 0L : minClientCost;
    }

    public synchronized long getMinServerCost() {
        return minServerCost == Long.MAX_VALUE ? 0L : minServerCost;
    }

    @Override
    public synchronized String toString() {
        String serverStats = serverSampleCount == 0L
                ? "无样本"
                : String.format(
                        Locale.US,
                        "%dms (max: %d, min: %d)",
                        getAvgServerCost(),
                        maxServerCost,
                        getMinServerCost()
                );
        return String.format(
                Locale.US,
                "场景: %s | 次数: %d | 客户端平均: %dms (max: %d, min: %d) | 服务端平均: %s",
                sceneName,
                totalCount,
                getAvgClientCost(),
                maxClientCost,
                getMinClientCost(),
                serverStats
        );
    }
}
