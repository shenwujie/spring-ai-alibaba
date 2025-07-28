/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.cloud.ai.example.manus.dynamic.mcp.config.McpCacheProperties;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.config.McpProperties;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener.AbstractMcpListener;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.po.McpConfigEntity;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.model.vo.McpServiceEntity;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.repository.McpConfigRepository;
import com.alibaba.cloud.ai.example.manus.dynamic.mcp.service.McpConnectionFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

/**
 * MCP缓存管理器
 */
@Component
public class McpCacheManager {

	private static final Logger logger = LoggerFactory.getLogger(McpCacheManager.class);

	private final McpConnectionFactory connectionFactory;

	private final McpConfigRepository mcpConfigRepository;

	private final McpProperties mcpProperties;

	private final McpCacheProperties cacheProperties;

	@Autowired
	private AbstractMcpListener refreshTokenManager;

	@Autowired
	private AbstractMcpListener connectionMonitor;

	private final LoadingCache<String, Map<String, McpServiceEntity>> toolCallbackMapCache;

	// 锁机制相关字段
	private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
	private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

	public McpCacheManager(McpConnectionFactory connectionFactory, McpConfigRepository mcpConfigRepository,
			McpProperties mcpProperties, McpCacheProperties cacheProperties, AbstractMcpListener connectionMonitor) {
		this.connectionFactory = connectionFactory;
		this.mcpConfigRepository = mcpConfigRepository;
		this.mcpProperties = mcpProperties;
		this.cacheProperties = cacheProperties;
		this.connectionMonitor = connectionMonitor;

		this.toolCallbackMapCache = buildCache();
		
		// 启动时预加载缓存
		if (cacheProperties.isPreloadOnStartup()) {
			logger.info("Preloading MCP cache on startup (preload enabled)");
			preloadCache();
		}
		else {
			logger.info("Skipping MCP cache preload on startup (preload disabled)");
		}
	}

	/**
	 * 构建缓存
	 * @return 加载缓存
	 */
	private LoadingCache<String, Map<String, McpServiceEntity>> buildCache() {
		return CacheBuilder.newBuilder()
			.expireAfterAccess(mcpProperties.getCacheExpireAfterAccess().toMinutes(), TimeUnit.MINUTES)
			.removalListener((RemovalListener<String, Map<String, McpServiceEntity>>) notification -> {
				Map<String, McpServiceEntity> mcpServiceEntityMap = notification.getValue();
				if (mcpServiceEntityMap == null) {
					return;
				}
				for (McpServiceEntity mcpServiceEntity : mcpServiceEntityMap.values()) {
					try {
						mcpServiceEntity.getMcpAsyncClient().close();
					}
					catch (Throwable t) {
						logger.error("Failed to close MCP client", t);
					}
				}
			})
			.build(new CacheLoader<>() {
				@Override
				public Map<String, McpServiceEntity> load(String key) throws Exception {
					return loadMcpServices(mcpConfigRepository.findAll());
				}
			});
	}

	/**
	 * 加载MCP服务
	 * @param mcpConfigEntities MCP配置实体列表
	 * @return MCP服务实体映射
	 * @throws IOException 加载失败时抛出异常
	 */
	private Map<String, McpServiceEntity> loadMcpServices(List<McpConfigEntity> mcpConfigEntities) throws IOException {
		Map<String, McpServiceEntity> toolCallbackMap = new ConcurrentHashMap<>();

		if (mcpConfigEntities == null || mcpConfigEntities.isEmpty()) {
			logger.info("No MCP server configurations found");
			return toolCallbackMap;
		}

		logger.info("Loading {} MCP server configurations", mcpConfigEntities.size());

		for (McpConfigEntity mcpConfigEntity : mcpConfigEntities) {
			String serverName = mcpConfigEntity.getMcpServerName();

			try {
				// 更新连接状态为连接中
				connectionMonitor.updateConnectionStatus(serverName, AbstractMcpListener.ConnectionStatus.CONNECTING);
				
				McpServiceEntity mcpServiceEntity = connectionFactory.createConnection(mcpConfigEntity);
				
				if (mcpServiceEntity != null) {
					toolCallbackMap.put(serverName, mcpServiceEntity);
					// 更新连接状态为已连接
					connectionMonitor.updateConnectionStatus(serverName, AbstractMcpListener.ConnectionStatus.CONNECTED);
					logger.info("Successfully loaded MCP server: {} with type: {}", serverName,
							mcpConfigEntity.getConnectionType());
				}
				else {
					// 更新连接状态为错误
					connectionMonitor.updateConnectionStatus(serverName, AbstractMcpListener.ConnectionStatus.ERROR);
					logger.warn("Failed to create MCP service entity for server: {}", serverName);
				}
			}
			catch (Exception e) {
				// 更新连接状态为错误
				connectionMonitor.updateConnectionStatus(serverName, AbstractMcpListener.ConnectionStatus.ERROR);
				logger.error("Failed to load MCP server configuration for: {}, error: {}", serverName, e.getMessage(),
						e);
			}
		}

		logger.info("Successfully loaded {} out of {} MCP servers", toolCallbackMap.size(), mcpConfigEntities.size());
		return toolCallbackMap;
	}

	/**
	 * 获取或加载MCP服务
	 * @param planId 计划ID
	 * @return MCP服务实体映射
	 */
	public Map<String, McpServiceEntity> getOrLoadServices(String planId) {
		// 检查是否有刷新锁
		if (isRefreshing.get()) {
			return waitForRefresh(planId);
		}

		cacheLock.readLock().lock();
		try {
			return toolCallbackMapCache.get(planId != null ? planId : "DEFAULT");
		}
		catch (Exception e) {
			logger.error("Failed to get or load MCP services for plan: {}", planId, e);
			return new ConcurrentHashMap<>();
		}
		finally {
			cacheLock.readLock().unlock();
		}
	}

	/**
	 * 等待刷新完成
	 * @param planId 计划ID
	 * @return MCP服务实体映射
	 */
	private Map<String, McpServiceEntity> waitForRefresh(String planId) {
		logger.debug("Cache is refreshing, waiting for completion...");
		
		for (int i = 0; i < cacheProperties.getRetryCount(); i++) {
			try {
				Thread.sleep(cacheProperties.getLockWaitTimeout() * 1000L);
				if (!isRefreshing.get()) {
					logger.debug("Refresh completed, retrying to get services for plan: {}", planId);
					return getOrLoadServices(planId);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Thread interrupted while waiting for cache refresh");
				break;
			}
		}
		
		logger.warn("Cache refresh timeout, returning empty result for plan: {}", planId);
		return new ConcurrentHashMap<>();
	}

	/**
	 * 获取MCP服务实体列表
	 * @param planId 计划ID
	 * @return MCP服务实体列表
	 */
	public List<McpServiceEntity> getServiceEntities(String planId) {
		try {
			return new ArrayList<>(getOrLoadServices(planId).values());
		}
		catch (Exception e) {
			logger.error("Failed to get MCP service entities for plan: {}", planId, e);
			return new ArrayList<>();
		}
	}

	/**
	 * 清除缓存
	 * @param planId 计划ID
	 */
	public void invalidateCache(String planId) {
		toolCallbackMapCache.invalidate(planId != null ? planId : "DEFAULT");
		logger.info("Invalidated cache for plan: {}", planId);
	}

	/**
	 * 清除所有缓存
	 */
	public void invalidateAllCache() {
		toolCallbackMapCache.invalidateAll();
		logger.info("Invalidated all MCP service caches");
	}

	/**
	 * 刷新缓存
	 * @param planId 计划ID
	 */
	public void refreshCache(String planId) {
		toolCallbackMapCache.refresh(planId != null ? planId : "DEFAULT");
		logger.info("Refreshed cache for plan: {}", planId);
	}

	/**
	 * 获取缓存统计信息
	 * @return 缓存统计信息
	 */
	public String getCacheStats() {
		return toolCallbackMapCache.stats().toString();
	}

	/**
	 * 启动时预加载缓存
	 */
	private void preloadCache() {
		logger.info("Preloading MCP cache on startup...");
		
		// 尝试获取刷新令牌
		if (!refreshTokenManager.tryAcquireToken("McpCacheManager")) {
			logger.info("Skipping preload - refresh token held by: {}", refreshTokenManager.getCurrentTokenHolder());
			return;
		}
		
		try {
			// 预加载默认缓存
			toolCallbackMapCache.get("DEFAULT");
			logger.info("MCP cache preloaded successfully");
		}
		catch (Exception e) {
			logger.error("Failed to preload MCP cache", e);
		}
		finally {
			// 释放刷新令牌
			refreshTokenManager.releaseToken("McpCacheManager");
		}
	}

	/**
	 * 设置刷新状态
	 * @param refreshing 是否正在刷新
	 */
	public void setRefreshing(boolean refreshing) {
		isRefreshing.set(refreshing);
		logger.debug("Cache refresh status set to: {}", refreshing);
	}

	/**
	 * 检查是否正在刷新
	 * @return true if refreshing, false otherwise
	 */
	public boolean isRefreshing() {
		return isRefreshing.get();
	}

	/**
	 * 获取写锁（用于刷新操作）
	 */
	public void acquireWriteLock() {
		cacheLock.writeLock().lock();
	}

	/**
	 * 释放写锁
	 */
	public void releaseWriteLock() {
		cacheLock.writeLock().unlock();
	}

	/**
	 * 强制刷新缓存（带锁控制）
	 * @param planId 计划ID
	 */
	public void forceRefreshCache(String planId) {
		logger.info("Force refreshing cache for plan: {}", planId);
		setRefreshing(true);
		
		try {
			acquireWriteLock();
			toolCallbackMapCache.refresh(planId != null ? planId : "DEFAULT");
			logger.info("Cache refreshed successfully for plan: {}", planId);
		}
		finally {
			releaseWriteLock();
			setRefreshing(false);
		}
	}

	/**
	 * 获取连接监控器
	 * @return 连接监控器
	 */
	public AbstractMcpListener getConnectionMonitor() {
		return connectionMonitor;
	}
}
