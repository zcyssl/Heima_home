package com.hmdp.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

// 这个工具类可以直接问豆包，很简单
@Component
public class RedisIdWorker {
    // 起始时间戳（2023-01-01 00:00:00）
    private static final long BEGIN_TIMESTAMP = 1672531200L;
    // 序列号部分的位数
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成全局唯一 ID
     * ID 结构：符号位(1) + 时间戳部分(31) + 序列号部分(32)
     * @param keyPrefix 业务前缀，如 "order"、"user"
     * @return 全局唯一 ID
     */
    public long nextId(String keyPrefix) {
        // 1. 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 生成序列号
        // 获取当前日期，精确到天，用于 Redis 键的一部分
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // Redis 键格式：icr:{业务前缀}:{日期}，如 icr:order:2023:06:01
        String key = "icr:" + keyPrefix + ":" + date;
        // 自增长操作，原子性保证唯一性
        Long count = stringRedisTemplate.opsForValue().increment(key);

        // 3. 拼接并返回 ID
        // 时间戳部分左移 32 位，为序列号部分留出空间
        return timestamp << COUNT_BITS | count;
    }
}
