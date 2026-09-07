package com.common.timedetect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class TimeCostTrackerTest {
    private AtomicLong now;
    private TimeCostTracker tracker;

    @Before
    public void setUp() {
        now = new AtomicLong(100L);
        tracker = new TimeCostTracker(now::get, TrackerLogger.NONE);
    }

    @Test
    public void endSessionBuildsReportAndUpdatesAggregateStats() {
        String sessionId = tracker.startSession("crop_detection");

        now.set(120L);
        tracker.markNode(sessionId, "compress_done");
        tracker.setServerCost(sessionId, 10L);
        now.set(150L);

        TraceReport report = tracker.endSession(sessionId, "result_delivered");

        assertNotNull(report);
        assertEquals(50L, report.totalCostMs);
        assertEquals(10L, report.serverCostMs);
        assertEquals(40L, report.clientCostMs);
        assertEquals(Long.valueOf(20L), report.nodeCosts.get("compress_done"));
        assertEquals(Long.valueOf(50L), report.nodeCosts.get("result_delivered"));
        assertEquals(0, tracker.getActiveSessionCount());

        AggregatedStats stats = tracker.getGlobalStats("crop_detection");
        assertNotNull(stats);
        assertEquals(1L, stats.getCount());
        assertEquals(40L, stats.getAvgClientCost());
        assertEquals(10L, stats.getAvgServerCost());
    }

    @Test
    public void reportTreatsMissingServerMeasurementAsUnknown() {
        TraceSession session = new TraceSession("local", "local_1", now::get);
        now.set(125L);
        assertTrue(session.addNode("inference_done"));
        assertTrue(session.finish("result_delivered"));

        TraceReport report = session.generateReport();

        assertEquals(-1L, report.serverCostMs);
        assertEquals(25L, report.totalCostMs);
        assertEquals(25L, report.clientCostMs);
        assertTrue(report.toString().contains("unknown"));
    }

    @Test
    public void aggregateIgnoresUnknownServerSamples() {
        String unknownServerId = tracker.startSession("mixed");
        now.set(130L);
        tracker.endSession(unknownServerId);

        now.set(200L);
        String measuredServerId = tracker.startSession("mixed");
        tracker.setServerCost(measuredServerId, 15L);
        now.set(250L);
        tracker.endSession(measuredServerId);

        AggregatedStats stats = tracker.getGlobalStats("mixed");
        assertNotNull(stats);
        assertEquals(2L, stats.getCount());
        assertEquals(1L, stats.getServerSampleCount());
        assertEquals(15L, stats.getAvgServerCost());
    }

    @Test
    public void duplicateStartsInSameMillisecondStillGetUniqueIds() {
        String first = tracker.startSession("same_scene");
        String second = tracker.startSession("same_scene");

        assertNotEquals(first, second);
        assertEquals(2, tracker.getActiveSessionCount());
    }

    @Test
    public void cancelDoesNotPolluteStatisticsAndClosedTraceRejectsNodes() {
        String cancelledId = tracker.startSession("cancelled");
        TraceSession cancelledSession = tracker.getSession(cancelledId);
        tracker.cancelSession(cancelledId);

        assertNull(tracker.getGlobalStats("cancelled"));
        assertEquals(0, tracker.getActiveSessionCount());
        assertNotNull(cancelledSession);

        String finishedId = tracker.startSession("finished");
        TraceSession finishedSession = tracker.getSession(finishedId);
        now.set(140L);
        tracker.endSession(finishedId);

        assertNotNull(finishedSession);
        assertTrue(finishedSession.isFinished());
        assertFalse(finishedSession.addNode("too_late"));
    }
}
