package com.innominds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 *
 * @author ThirupathiReddy V
 *
 */
@Configuration
public class CacheConfig {

    /** Reference to logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public HazelcastInstance hazelcastInstance() {

        final Config config = new Config();
        final GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName("dev");
        groupConfig.setPassword("dev");
        config.setGroupConfig(groupConfig);
        LOGGER.info("Creating hazelcast cluster node instance with groupName {} ", groupConfig.getName());
        final NetworkConfig networkConfig = config.getNetworkConfig();
        final MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        networkConfig.getJoin().setMulticastConfig(multicastConfig);
        networkConfig.getJoin().getAwsConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);

        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        return hazelcastInstance;
    }

    @Bean
    public ObjectMapper jacksonObjectMapper() {

        return new ObjectMapper();
    }
}
