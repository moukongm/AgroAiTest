package com.common.timedetect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Immutable, calculated view of a {@link TraceSession}. */
public final class TraceReport {
    public final String sceneName;
    public final String sessionId;
    public final long totalCostMs;
    public final long clientCostMs;
    public final long serverCostMs;
    public final Map<String, Long> nodeCosts;

    TraceReport(
            String sceneName,
            String sessionId,
            long startTimeMs,
            long serverCostMs,
            Map<String, Long> nodes
    ) {
        this.sceneName = sceneName;
        this.sessionId = sessionId;

        long lastNodeTimeMs = startTimeMs;
        LinkedHashMap<String, Long> calculatedNodeCosts = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : nodes.entrySet()) {
            long nodeTimeMs = entry.getValue();
            lastNodeTimeMs = Math.max(lastNodeTimeMs, nodeTimeMs);
            calculatedNodeCosts.put(entry.getKey(), Math.max(nodeTimeMs - startTimeMs, 0L));
        }

        this.totalCostMs = Math.max(lastNodeTimeMs - startTimeMs, 0L);
        this.serverCostMs = serverCostMs;
        this.clientCostMs = serverCostMs < 0L
                ? totalCostMs
                : Math.max(totalCostMs - serverCostMs, 0L);
        this.nodeCosts = Collections.unmodifiableMap(calculatedNodeCosts);
    }

    @Override
    public String toString() {
        String serverText = serverCostMs < 0L ? "unknown" : serverCostMs + "ms";
        return String.format(
                Locale.US,
                "场景: %s | 总耗时: %dms | 客户端: %dms | 服务端: %s",
                sceneName,
                totalCostMs,
                clientCostMs,
                serverText
        );
    }

    public String toDetailString() {
        StringBuilder builder = new StringBuilder();
        builder.append("========== 耗时详情 ==========\n")
                .append("场景: ").append(sceneName).append('\n')
                .append("追踪 ID: ").append(sessionId).append('\n')
                .append("总耗时: ").append(totalCostMs).append("ms\n")
                .append("客户端: ").append(clientCostMs).append("ms\n")
                .append("服务端: ")
                .append(serverCostMs < 0L ? "unknown" : serverCostMs + "ms")
                .append('\n');

        if (!nodeCosts.isEmpty()) {
            builder.append("节点:\n");
            for (Map.Entry<String, Long> entry : nodeCosts.entrySet()) {
                builder.append("  - ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("ms\n");
            }
        }
        return builder.toString().trim();
    }
}
