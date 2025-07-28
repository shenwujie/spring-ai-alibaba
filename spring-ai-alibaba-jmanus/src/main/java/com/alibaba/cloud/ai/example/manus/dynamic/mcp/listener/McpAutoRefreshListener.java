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
package com.alibaba.cloud.ai.example.manus.dynamic.mcp.listener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * MCP自动刷新监听器
 */
@Component
public class McpAutoRefreshListener extends AbstractMcpListener {

	private final ScheduledExecutorService refreshExecutor = java.util.concurrent.Executors.newScheduledThreadPool(1);

	private ScheduledFuture<?> refreshTask;

	@PostConstruct
	public void initListener() {
		init();
	}

	@PreDestroy
	public void shutdownListener() {
		shutdown();
	}

	@Override
	protected String getListenerName() {
		return "MCP Auto-Refresh Listener";
	}

	@Override
	protected boolean shouldStart() {
		return cacheProperties.isPreloadOnStartup() && cacheProperties.getAutoRefreshInterval() > 0;
	}

	@Override
	protected void startListener() {
		startAutoRefresh();
	}

	@Override
	protected void stopListener() {
		stopAutoRefresh();
		refreshExecutor.shutdown();
		try {
			if (!refreshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				refreshExecutor.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			refreshExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 启动自动刷新
	 */
	public void startAutoRefresh() {
		if (refreshTask != null && !refreshTask.isCancelled()) {
			logger.debug("Auto-refresh task already running");
			return;
		}

		refreshTask = refreshExecutor.scheduleAtFixedRate(() -> {
			try {
				performAutoRefresh();
			}
			catch (Exception e) {
				logger.error("Auto-refresh failed", e);
			}
		}, cacheProperties.getAutoRefreshInterval(), cacheProperties.getAutoRefreshInterval(), TimeUnit.SECONDS);

		logger.info("Started auto-refresh task with interval: {} seconds", cacheProperties.getAutoRefreshInterval());
	}

	/**
	 * 停止自动刷新
	 */
	public void stopAutoRefresh() {
		if (refreshTask != null) {
			refreshTask.cancel(false);
			refreshTask = null;
			logger.info("Stopped auto-refresh task");
		}
	}

	/**
	 * 执行自动刷新
	 */
	private void performAutoRefresh() {
		logger.info("Starting scheduled auto-refresh of MCP cache...");
		
		// 尝试获取刷新令牌
		if (!tryAcquireRefreshToken()) {
			logTokenAcquisitionFailed();
			return;
		}
		
		try {
			// 设置刷新状态
			cacheManager.setRefreshing(true);
			
			// 获取写锁
			cacheManager.acquireWriteLock();
			
			try {
				// 强制刷新所有缓存
				cacheManager.forceRefreshCache("DEFAULT");
				
				// 更新连接状态统计
				String connectionStats = getConnectionStatistics();
				logger.info("Auto-refresh completed. Connection status: {}", connectionStats);
				
			}
			finally {
				// 释放写锁
				cacheManager.releaseWriteLock();
				// 清除刷新状态
				cacheManager.setRefreshing(false);
			}
		}
		catch (Exception e) {
			logger.error("Auto-refresh failed", e);
			// 确保刷新状态被清除
			cacheManager.setRefreshing(false);
		}
		finally {
			// 释放刷新令牌
			releaseRefreshToken();
		}
	}

	/**
	 * 手动触发刷新
	 */
	public void triggerManualRefresh() {
		logger.info("Manual refresh triggered");
		performAutoRefresh();
	}

	/**
	 * 检查自动刷新是否正在运行
	 * @return true if running, false otherwise
	 */
	public boolean isAutoRefreshRunning() {
		return refreshTask != null && !refreshTask.isCancelled();
	}

	/**
	 * 获取自动刷新统计信息
	 * @return 统计信息字符串
	 */
	public String getAutoRefreshStats() {
		if (refreshTask == null || refreshTask.isCancelled()) {
			return "Auto-refresh: Disabled";
		}
		
		long delay = refreshTask.getDelay(TimeUnit.SECONDS);
		return String.format("Auto-refresh: Running (next refresh in %d seconds)", delay);
	}
} 