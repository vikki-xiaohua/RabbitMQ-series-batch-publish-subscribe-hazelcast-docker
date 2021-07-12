package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.MapUtil;
import com.hazelcast.map.IMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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

    public void putMessage(String key, boolean exist) {
        if (StringUtils.isBlank(key)) return;
        IMap<String, Boolean> map = cacheInstance.getMap(Constants.HAZELCAST_CACHE_KEY_PREFIX);
        map.put(key, exist);
    }

    public Boolean getMessage(String key) {
        if (StringUtils.isBlank(key)) return false;

        IMap<String, Boolean> map = cacheInstance.getMap(Constants.HAZELCAST_CACHE_KEY_PREFIX);
        if (MapUtil.isNullOrEmpty(map)) return false;

        return !ObjectUtils.isEmpty(map.get(key));
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
        MapConfig mapConfig = new MapConfig(Constants.HAZELCAST_CACHE_KEY_PREFIX);

        mapConfig.setTimeToLiveSeconds(180);
        mapConfig.setMaxIdleSeconds(240);
        mapConfig.setBackupCount(2);
        return mapConfig;
    }
}
