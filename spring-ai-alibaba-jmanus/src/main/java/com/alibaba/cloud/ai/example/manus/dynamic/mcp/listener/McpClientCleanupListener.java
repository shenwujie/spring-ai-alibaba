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

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.modelcontextprotocol.client.McpAsyncClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * MCP客户端清理监听器
 * 使用DelayQueue管理需要清理的McpAsyncClient，5分钟后自动关闭和销毁
 */
@Component
public class McpClientCleanupListener extends AbstractMcpListener {

	private final DelayQueue<ClientCleanupTask> cleanupQueue = new DelayQueue<>();
	private Thread cleanupThread;

	/**
	 * 客户端清理任务
	 */
	private static class ClientCleanupTask implements Delayed {
		private final McpAsyncClient client;
		private final String serverName;
		private final long cleanupTime;

		public ClientCleanupTask(McpAsyncClient client, String serverName, long delayMinutes) {
			this.client = client;
			this.serverName = serverName;
			this.cleanupTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(cleanupTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed other) {
			return Long.compare(cleanupTime, ((ClientCleanupTask) other).cleanupTime);
		}

		public McpAsyncClient getClient() {
			return client;
		}

		public String getServerName() {
			return serverName;
		}
	}

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
		return "MCP Client Cleanup Listener";
	}

	@Override
	protected boolean shouldStart() {
		return true; // 清理监听器总是启动
	}

	@Override
	protected void startListener() {
		cleanupThread = new Thread(this::cleanupLoop, "McpClientCleanupThread");
		cleanupThread.setDaemon(true);
		cleanupThread.start();
		logger.info("MCP client cleanup listener started");
	}

	@Override
	protected void stopListener() {
		if (cleanupThread != null) {
			cleanupThread.interrupt();
			try {
				cleanupThread.join(5000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		logger.info("MCP client cleanup listener shutdown");
	}

	/**
	 * 添加需要清理的客户端
	 * @param client 需要清理的McpAsyncClient
	 * @param serverName 服务器名称
	 * @param delayMinutes 延迟分钟数
	 */
	public void scheduleCleanup(McpAsyncClient client, String serverName, long delayMinutes) {
		ClientCleanupTask task = new ClientCleanupTask(client, serverName, delayMinutes);
		cleanupQueue.offer(task);
		logger.info("Scheduled cleanup for client of server: {} in {} minutes", serverName, delayMinutes);
	}

	/**
	 * 清理循环
	 */
	private void cleanupLoop() {
		while (running.get()) {
			try {
				ClientCleanupTask task = cleanupQueue.take();
				cleanupClient(task);
			}
			catch (InterruptedException e) {
				if (running.get()) {
					logger.warn("Cleanup thread interrupted", e);
				}
				break;
			}
			catch (Exception e) {
				logger.error("Error in cleanup loop", e);
			}
		}
	}

	/**
	 * 清理客户端
	 * @param task 清理任务
	 */
	private void cleanupClient(ClientCleanupTask task) {
		try {
			logger.info("Cleaning up client for server: {}", task.getServerName());
			
			// 关闭客户端
			if (task.getClient() != null) {
				task.getClient().close();
				logger.info("Successfully closed client for server: {}", task.getServerName());
			}
			
			// 清理引用，帮助GC
			logger.info("Client cleanup completed for server: {}", task.getServerName());
		}
		catch (Exception e) {
			logger.error("Error cleaning up client for server: {}", task.getServerName(), e);
		}
	}

	/**
	 * 获取当前队列大小
	 * @return 队列中的任务数量
	 */
	public int getQueueSize() {
		return cleanupQueue.size();
	}

	/**
	 * 清空队列（用于紧急情况）
	 */
	public void clearQueue() {
		cleanupQueue.clear();
		logger.warn("Cleanup queue cleared");
	}
} 