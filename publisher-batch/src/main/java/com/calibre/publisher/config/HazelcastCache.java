package com.calibre.publisher.config;

import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.util.Constants;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.MapUtil;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class HazelcastCache {
    private HazelcastInstance cacheInstance;

    @Autowired
    public void setCacheInstance(HazelcastInstance cacheInstance) {
        this.cacheInstance = cacheInstance;
    }

    public FxCurrencyRateCsvRow putCsvRow(String key, FxCurrencyRateCsvRow fxCurrencyRateCsvRow) {
        IMap<String, FxCurrencyRateCsvRow> map = cacheInstance.getMap(Constants.HAZELCAST_FX_RATE_CACHE_KEY_PREFIX);
        return map.put(key, fxCurrencyRateCsvRow);
    }

    public FxCurrencyRateCsvRow getCsvRow(String key) {
        IMap<String, FxCurrencyRateCsvRow> map = cacheInstance.getMap(Constants.HAZELCAST_FX_RATE_CACHE_KEY_PREFIX);
        if (MapUtil.isNullOrEmpty(map)) return null;
        return map.get(key);
    }


    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance(MapConfig mapConfig) {
        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
        Config config = hzInstance.getConfig();
        config.addMapConfig(mapConfig);


        JoinConfig joinConfig = new JoinConfig();
        joinConfig.getMulticastConfig().setEnabled(true);
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPortAutoIncrement(true);

        networkConfig.setJoin(joinConfig);

        config.setNetworkConfig(networkConfig);

        return hzInstance;
    }

    @Bean
    public MapConfig mapConfig() {
        MapConfig mapConfig = new MapConfig(Constants.HAZELCAST_FX_RATE_CACHE_KEY_PREFIX);
        mapConfig.setTimeToLiveSeconds(0);
        mapConfig.setMaxIdleSeconds(0);
        mapConfig.setBackupCount(2);

        return mapConfig;
    }

}
