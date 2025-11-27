package ru.mtsbank.soapdummy.mock.setting;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mock settings data cache storage
 */
@Slf4j
@Repository
@ParametersAreNonnullByDefault
public class DummyCacheStorage {

    private static final Lock LOAD_CACHE_LOCK = new ReentrantLock();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int CACHE_CONCURRENCY_LEVEL = 1;

    private final List<String> mockSettingsFileFoldersPath;
    private final Cache<DummyCashKey, EndpointMockSetting> cache;

    public DummyCacheStorage(@Value("#{'${setting.mock.file.dirs.path}'.split(',')}") List<String> mockSettingsFilePath,
                             @Value("${dummy.cache.duration.hours}") int durationHours) {
        this.mockSettingsFileFoldersPath = mockSettingsFilePath;
        this.cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Duration.ofHours(durationHours))
            .concurrencyLevel(CACHE_CONCURRENCY_LEVEL)
            .build();
        loadCacheScheduler();
    }

    /**
     * Load cache data from file by cron of schedule
     */
    @Scheduled(cron = "${dummy.load.cache.cron}")
    public void loadCacheScheduler() {
        try {
            if (!LOAD_CACHE_LOCK.tryLock()) {
                log.warn("Task is running now. Skipped...");
                return;
            }
            loadCache();
        } catch (IOException ex) {
            log.error("IO Error while loading cache", ex);
        } catch (Exception ex) {
            log.error("Another error while loading cache", ex);
        } finally {
            LOAD_CACHE_LOCK.unlock();
        }
    }

    /**
     * load cache of mocks
     * @throws IOException io exception when it parses json settings
     */
    private void loadCache() throws IOException {
        log.info("Loading settings directories with paths: {}", mockSettingsFileFoldersPath);
        Set<File> folders = mockSettingsFileFoldersPath
            .stream()
            .map(File::new)
            .collect(Collectors.toSet());
        log.info("Loaded mock settings directories: {}", folders);
        Set<File> settingFiles = new HashSet<>();
        folders.forEach(d -> settingFiles.addAll(
                Stream.of(Objects.requireNonNull(d.listFiles(), "mock settings dir cannot be null"))
                    .filter(File::isFile)
                    .filter(f -> f.getName().endsWith(".json"))
                    .collect(Collectors.toSet())
            )
        );
        log.info("Loaded mock settings files: {}", settingFiles);
        if (CollectionUtils.isEmpty(settingFiles)) {
            log.warn("Setting files doesn't exist. Skipping...");
            return;
        }
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        for (File file : settingFiles) {
            MockSettings mockSettings = OBJECT_MAPPER.readValue(
                file, MockSettings.class
            );
            addOrInvalidateCacheEntry(mockSettings);
        }
        log.info("Cache updated with size: {}", cache.size());
    }

    /**
     * Add or invalidate cache entry
     * @param mockSettings mock setting
     */
    private void addOrInvalidateCacheEntry(MockSettings mockSettings) {
        mockSettings.getEndpointMocks()
            .forEach(
                mock -> {
                    log.debug("Work around mock: {}", mock);
                    var key = new DummyCashKey(mock.getEndpointUrl());
                    if (mock.getEnable()) {
                        if (isCanBeAdded(mock)) {
                            cache.put(key, mock);
                            log.info("Entry added with key: {}", key);
                        } else {
                            cache.invalidate(key);
                        }
                    } else {
                        if (getServiceSetting(key).isPresent()) {
                            cache.invalidate(key);
                            log.info("Entry invalidated with key: {}", key);
                        }
                    }
                }
            );
    }

    /**
     * Check mock can be added to cash, by rules:
     *      either {@code requestMockConditions} != null or size of {@code specificMockSettings} == 1
     * @param mock Mock settings
     * @return true either {@code requestMockConditions} != null or size of {@code specificMockSettings} == 1
     *         false another ways
     */
    private static boolean isCanBeAdded(EndpointMockSetting mock) {
        if (mock.getSpecificMockSettings().stream().allMatch(f -> f.getRequestMockConditions() != null)
            || mock.getSpecificMockSettings()
                .stream()
                .filter(SpecificMockSetting::getEnable)
                .collect(Collectors.toSet())
                .size() == 1) {
            return true;
        }
        log.warn("requestMockConditions should be added for endpoint mocks which size more than 1");
        return false;
    }

    /**
     * Getting Optional of service mock setting from cache by endpointUrl (key of cached value)
     * @param endpointUrl requested mock endpoint url (key of cached value)
     * @return Optional of service mock setting
     */
    @Nonnull
    public Optional<EndpointMockSetting> getServiceSetting(String endpointUrl) {
        DummyCashKey dummyCashKey = new DummyCashKey(endpointUrl);
        return getServiceSetting(dummyCashKey);
    }

    /**
     * Getting Optional of service mock setting from cache by endpointUrl (key of cached value)
     * @param key key of cached value
     * @return Optional of service mock setting
     */
    public Optional<EndpointMockSetting> getServiceSetting(DummyCashKey key) {
        log.info("Try to find mock setting in cache by key: {}", key);
        return Optional.ofNullable(
            cache.getIfPresent(key)
        );
    }

    /**
     * Key of cached value
     */
    @lombok.Value
    @Getter
    @EqualsAndHashCode
    public static class DummyCashKey {
        @Nonnull
        String endpointUrl;
    }
}
