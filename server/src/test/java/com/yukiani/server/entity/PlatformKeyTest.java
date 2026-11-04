package com.yukiani.server.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlatformKeyTest {

    @Test
    void identifiesEntriesByBothPlatformAndPlatformId() {
        PlatformKey key = new PlatformKey(Platform.Bangumi, "12345");
        Set<PlatformKey> keys = new HashSet<>();
        keys.add(key);

        assertEquals(key, new PlatformKey(Platform.Bangumi, "12345"));
        assertTrue(keys.contains(new PlatformKey(Platform.Bangumi, "12345")));
        assertNotEquals(key, new PlatformKey(Platform.AniList, "12345"));
        assertNotEquals(key, new PlatformKey(Platform.Bangumi, "67890"));
    }
}
