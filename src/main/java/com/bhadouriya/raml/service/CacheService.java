package com.bhadouriya.raml.service;

import java.io.IOException;

public interface CacheService {
    void clearAllCache();

    StubCacheEntity getStubById(String stubId) throws IOException;
}
