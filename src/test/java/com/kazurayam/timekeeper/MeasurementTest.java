package com.kazurayam.timekeeper;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class MeasurementTest {

    private final Logger logger = LoggerFactory.getLogger(TestHelper.getClassName());

    private Measurement m;

    @BeforeEach
    public void setup() {
        m = new Measurement.Builder("foo", TestHelper.getColumnNames()).build();
        m.add(TestHelper.makeRecord1());
        m.add(TestHelper.makeRecord2());
    }

    @Test
    public void test_recordDuration() throws InterruptedException {
        LocalDateTime startAt = LocalDateTime.now();
        Thread.sleep(1000L);
        LocalDateTime endAt = LocalDateTime.now();
        m.recordDuration(Collections.singletonMap("foo", "bar"), startAt, endAt);
        Record record = m.getLast();
        long duration = record.getDurationMillis();
        assertEquals(Math.floor(1000 / 1000), Math.floor(duration / 1000));
    }

    @Test
    public void test_before_after() throws InterruptedException {
        m.before(Collections.singletonMap("foo", "bar"));
        Thread.sleep(1000L);
        m.after();
        Record record = m.getLast();
        long duration = record.getDurationMillis();
        assertEquals(Math.floor(1000 / 1000), Math.floor(duration / 1000));
    }

    @Test
    public void test_getId() {
        assertEquals("foo", m.getId());
    }

    @Test
    public void test_size() {
        assertEquals(2, m.size());
    }

    @Test
    public void test_iterator() {
        m.forEach(record -> logger.debug(record.toString()));
    }

    @Test
    public void test_get() {
        Record r = m.get(0);
        assertEquals("Test Cases/printID", r.getAttributes().get("testCaseId"));
    }

    @Test
    public void test_newRecord() {
        Record record = m.newRecord();
        Set<String> keySet = record.getAttributes().keySet();
        assertTrue(keySet.size() > 0);
        assertTrue(keySet.contains("case"));
    }


    @Test
    public void test_getLatestRecordDurationMillis() throws InterruptedException {
        Measurement m = new Measurement.Builder("some", Collections.singletonList("URL")).build();
        Record r = new Record.Builder().attr("URL", "https://example.com").build();
        r.setStartAt(LocalDateTime.now());
        Thread.sleep(300);
        r.setEndAt(LocalDateTime.now());
        m.add(r);
        long millis = m.getLastRecordDurationMillis();
        assertTrue(millis < 1000);
        // System.out.println(millis);
    }

    @Test
    public void test_toString() {
        String json = m.toString();
        System.out.println(json);
        // make sure the Sting is a well-formed JSON by parsing it with Gson parser
        Gson gson = new Gson();
        Object obj = gson.fromJson(json, Object.class);
        assertNotNull(obj);
    }


    @Test
    public void test_toPrettyJson() {
        String json = m.toPrettyJson();
        //System.out.println(json);
        assertNotNull(json);
    }

    @Test
    public void test_fixture_3lines() throws IOException {
        Measurement m = TestHelper.makeMeasurementOf3lines();
        //System.out.println(m.toJson());
        assertNotNull(m);
    }

}