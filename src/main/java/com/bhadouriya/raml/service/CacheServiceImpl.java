package com.bhadouriya.raml.service;

import com.bhadouriya.raml.artifacts.WrapperApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CacheServiceImpl implements CacheService {

    private static final FileSystem fileSys = FileSystems.getDefault();

    @CacheEvict(value = "res", allEntries = true)
    public void clearAllCache() {

    }

    @Cacheable(value = "res", key = "#stubId", condition = "#stubId!=null || #stubId.length()>0", unless = "#result==null")
    public StubCacheEntity getStubById(String stubId) throws IOException {
        StubCacheEntity stubCacheEntity = null;

        Path stubPath = fileSys.getPath(stubId);
        final boolean isRamlOnClassPath = false;

        if (stubPath.toFile().exists()) {
            StubResourceService stubResourceService = new StubResourceService(isRamlOnClassPath, fileSys.getPath(stubPath.toString(), "raml", "api.raml").toString());
            StubValidationService validationService = new StubValidationService(stubResourceService);
            WrapperApi wrapperApi = new ObjectMapper().readValue(fileSys.getPath(stubPath.toString(), "wrapper,json").toFile(), WrapperApi.class);
            stubCacheEntity = new StubCacheEntity(wrapperApi, validationService);

        }
        return stubCacheEntity;

    }
}
