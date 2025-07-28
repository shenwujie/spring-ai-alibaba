需求的背景
1、目前的设计是MCP的缓存服务是按planId做为Key来reload的。生效时间是10分钟。所有的plan以及查询agent列表都会block。要等所有的MCP服务注册完毕，如果MCP列表多或者部分失败等待时间更久。
2、因为SSE有缺陷，所以缓存的服务是按planId为Key设计的。

方案是：
1. 启动加载所有mcp服务到cache。
2. 针对sse增加心跳功能，初步通过ping探测服务是否可用，每30秒ping一次，有问题加锁，关闭旧连接，重新建立新连接并行merge到缓存。然后释放锁。
3. 每10分钟自动全量刷新全量sse和streamable，同样通过锁控制新老链接等待（兜底逻辑，担心ping不管用）
4. 每次get 缓存时，判断是否有锁，有锁等待3秒钟，重试3次。
5. 增加3个配置开关。全量加载缓存（yes/no），心跳开关（0代表无心跳，其他代表多少秒ping一次），全量自动刷新开关（0代表不刷新 其他代表间隔多少秒）

## 需求澄清与详细技术方案

### 当前架构分析

#### 现有问题
1. **缓存策略问题**：
   - 当前使用Guava Cache，按planId作为key进行缓存
   - 缓存过期时间为10分钟（`cacheExpireAfterAccess = Duration.ofMinutes(10)`）
   - 每次缓存失效时，会阻塞所有plan和agent列表查询
   - 缓存加载过程是同步的，MCP服务多或部分失败时等待时间过长

2. **连接管理问题**：
   - 当前没有心跳检测机制
   - SSE连接可能因为网络问题断开，但缓存中仍保留无效连接
   - 没有自动重连机制
   - 连接状态不可见

3. **性能问题**：
   - 缓存失效时全量重新加载，影响用户体验
   - 没有连接池管理
   - 缺乏并发控制机制

### 详细技术方案

#### 1. 启动时全量加载机制
```java
// 新增配置项
@ConfigurationProperties("mcp.cache")
public class McpCacheProperties {
    // 是否启动时全量加载缓存
    private boolean preloadOnStartup = true;
    
    // 心跳检测间隔（秒），0表示不启用心跳
    private int heartbeatInterval = 30;
    
    // 全量自动刷新间隔（秒），0表示不启用自动刷新
    private int autoRefreshInterval = 600; // 10分钟
    
    // 缓存获取时的锁等待时间（秒）
    private int lockWaitTimeout = 3;
    
    // 缓存获取重试次数
    private int retryCount = 3;
}
```

#### 2. 心跳检测机制
```java
@Component
public class McpHeartbeatService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final McpCacheManager cacheManager;
    private final McpConnectionFactory connectionFactory;
    
    @PostConstruct
    public void startHeartbeat() {
        if (properties.getHeartbeatInterval() > 0) {
            scheduler.scheduleAtFixedRate(
                this::performHeartbeat,
                0,
                properties.getHeartbeatInterval(),
                TimeUnit.SECONDS
            );
        }
    }
    
    private void performHeartbeat() {
        // 对每个SSE连接进行ping检测
        // 发现问题时加锁，重建连接，更新缓存
    }
}
```

#### 3. 锁机制与并发控制
```java
public class McpCacheManager {
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
    
    public Map<String, McpServiceEntity> getOrLoadServices(String planId) {
        // 检查是否有刷新锁
        if (isRefreshing.get()) {
            return waitForRefresh(planId);
        }
        
        cacheLock.readLock().lock();
        try {
            return toolCallbackMapCache.get(planId != null ? planId : "DEFAULT");
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    private Map<String, McpServiceEntity> waitForRefresh(String planId) {
        for (int i = 0; i < properties.getRetryCount(); i++) {
            try {
                Thread.sleep(properties.getLockWaitTimeout() * 1000);
                if (!isRefreshing.get()) {
                    return getOrLoadServices(planId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // 超时后返回空结果或抛出异常
        return new ConcurrentHashMap<>();
    }
}
```

#### 4. 自动刷新机制
```java
@Component
public class McpAutoRefreshService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void startAutoRefresh() {
        if (properties.getAutoRefreshInterval() > 0) {
            scheduler.scheduleAtFixedRate(
                this::performFullRefresh,
                properties.getAutoRefreshInterval(),
                properties.getAutoRefreshInterval(),
                TimeUnit.SECONDS
            );
        }
    }
    
    private void performFullRefresh() {
        // 加锁，关闭所有旧连接
        // 重新建立所有SSE和Streamable连接
        // 更新缓存
        // 释放锁
    }
}
```

#### 5. 连接状态监控
```java
@Component
public class McpConnectionMonitor {
    private final Map<String, ConnectionStatus> connectionStatuses = new ConcurrentHashMap<>();
    
    public enum ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }
    
    public void updateStatus(String serverName, ConnectionStatus status) {
        connectionStatuses.put(serverName, status);
    }
    
    public ConnectionStatus getStatus(String serverName) {
        return connectionStatuses.getOrDefault(serverName, ConnectionStatus.DISCONNECTED);
    }
}
```

### 实现优先级

#### 第一阶段：基础优化
1. 添加配置开关
2. 实现锁机制和并发控制
3. 添加连接状态监控

#### 第二阶段：心跳机制
1. 实现SSE连接的心跳检测
2. 添加自动重连机制
3. 优化连接池管理

#### 第三阶段：自动刷新
1. 实现定时全量刷新
2. 添加刷新策略配置
3. 性能优化和监控

### 配置示例
```yaml
mcp:
  cache:
    preload-on-startup: true
    heartbeat-interval: 30
    auto-refresh-interval: 600
    lock-wait-timeout: 3
    retry-count: 3
```

### 预期效果
1. **性能提升**：减少缓存失效时的阻塞时间
2. **稳定性增强**：通过心跳检测及时发现连接问题
3. **可用性提高**：自动重连和刷新机制保证服务可用
4. **可观测性**：连接状态监控便于问题排查

## 实现完成情况

### 第一阶段：基础优化 ✅
- [x] 创建McpCacheProperties配置类
- [x] 创建McpConnectionMonitor连接监控类
- [x] 增强McpCacheManager，添加锁机制和并发控制
- [x] 实现启动时预加载功能
- [x] 添加连接状态更新机制

### 第二阶段：心跳检测机制 ✅
- [x] 创建McpHeartbeatService心跳检测服务
- [x] 实现SSE和STREAMING连接的健康检查
- [x] 添加自动重连机制
- [x] 支持心跳统计和监控
- [x] 实现心跳任务的启动和停止控制

### 第三阶段：自动刷新机制 ✅
- [x] 创建McpAutoRefreshService自动刷新服务
- [x] 实现定时全量刷新功能
- [x] 添加手动触发刷新接口
- [x] 支持刷新状态监控
- [x] 集成锁机制确保刷新安全

### 第四阶段：管理接口和监控面板 ✅
- [x] 创建McpCacheManagementController管理控制器
- [x] 提供缓存统计信息接口
- [x] 提供连接状态查询接口
- [x] 提供心跳检测管理接口
- [x] 提供手动刷新和自动刷新控制接口

### 核心功能特性

1. **并发安全**: 使用读写锁确保缓存操作的线程安全
2. **自动恢复**: 心跳检测失败时自动尝试重连
3. **状态透明**: 实时监控所有连接状态和统计信息
4. **灵活配置**: 支持通过配置文件调整各种参数
5. **管理友好**: 提供完整的REST API进行监控和管理
6. **性能优化**: 启动预加载、锁等待、重试机制等多重优化

### API接口列表

- `GET /api/mcp/cache/stats` - 获取缓存统计信息
- `GET /api/mcp/cache/connections` - 获取所有连接状态
- `GET /api/mcp/cache/connections/{serverName}` - 获取指定服务器连接状态
- `GET /api/mcp/cache/heartbeat/{serverName}` - 获取心跳时间
- `POST /api/mcp/cache/refresh` - 手动刷新缓存
- `POST /api/mcp/cache/refresh/{planId}` - 刷新指定计划缓存
- `POST /api/mcp/cache/auto-refresh` - 触发自动刷新
- `POST /api/mcp/cache/heartbeat/{serverName}/start` - 启动心跳检测
- `POST /api/mcp/cache/heartbeat/{serverName}/stop` - 停止心跳检测
- `GET /api/mcp/cache/refreshing` - 检查刷新状态
- `GET /api/mcp/cache/auto-refresh/running` - 检查自动刷新状态

### 使用说明

1. **配置启用**: 在application.yml中配置相关参数
2. **启动监控**: 应用启动后自动开始心跳检测和自动刷新
3. **状态查看**: 通过API接口查看缓存和连接状态
4. **手动控制**: 可通过API接口手动触发刷新或控制心跳检测
5. **问题排查**: 通过连接状态监控快速定位问题

### 注意事项

1. 心跳检测仅对SSE和STREAMING连接有效
2. 自动刷新会暂时阻塞缓存访问，建议在生产环境中谨慎配置刷新间隔
3. 锁等待超时后会自动返回空结果，避免长时间阻塞
4. 建议根据实际网络环境调整心跳间隔和重试次数

## 优化更新

### 优化内容

#### 1. 启动条件优化
- **心跳检测服务**: 只有当`preloadOnStartup=true`且`heartbeatInterval>0`时才启动
- **自动刷新服务**: 只有当`preloadOnStartup=true`且`autoRefreshInterval>0`时才启动
- **缓存管理器**: 只有当`preloadOnStartup=true`时才进行启动时预加载

#### 2. 令牌机制优化
- **新增McpRefreshTokenManager**: 用于避免多个服务同时刷新缓存
- **单个令牌key**: 使用一个令牌管理McpHeartbeatService、McpAutoRefreshService、McpCacheManager之间的争用
- **令牌获取**: 使用`tryAcquireToken()`方法尝试获取刷新令牌
- **令牌释放**: 使用`releaseToken()`方法释放刷新令牌
- **跳过机制**: 如果令牌被其他服务持有，则跳过当前刷新操作，等待下次执行

#### 3. 服务集成
- **McpHeartbeatService**: 在重连时使用令牌机制
- **McpAutoRefreshService**: 在自动刷新时使用令牌机制
- **McpCacheManager**: 在启动预加载时使用令牌机制

#### 4. 关闭逻辑优化
- **McpHeartbeatService**: shutdown时检查是否已启动，未启动则跳过关闭处理
- **McpAutoRefreshService**: shutdown时检查是否已启动，未启动则跳过关闭处理
- **避免无效操作**: 防止对未启动的服务执行关闭操作

#### 5. Ping失败处理逻辑优化
- **新增McpClientCleanupManager**: 使用DelayQueue管理需要清理的McpAsyncClient
- **智能重连机制**: ping失败时重新创建McpAsyncClient并测试连接
- **延迟清理**: 将失败的McpAsyncClient放入DelayQueue，5分钟后自动关闭和销毁
- **缓存更新**: 成功重连后更新缓存中的McpServiceEntity实例
- **资源管理**: 避免内存泄漏，确保失败的客户端被及时清理

#### 6. Listener模式重构
- **抽象基类**: 创建AbstractMcpListener抽象基类，提供公共方法和变量
- **统一命名**: 将所有Service和Manager后缀的类改为Listener后缀
- **目录重构**: 将所有Listener类移动到listener目录下
- **依赖注入**: 更新所有相关类的依赖引用
- **代码复用**: 通过继承AbstractMcpListener减少重复代码
- **统一管理**: 提供统一的初始化和关闭机制
- **功能集成**: 将McpRefreshTokenListener和McpConnectionMonitorListener的功能集成到AbstractMcpListener中
- **简化架构**: 减少类的数量，提高代码的内聚性

### 令牌机制工作流程

1. **令牌获取**: 服务尝试获取刷新令牌（单个key）
2. **成功获取**: 执行刷新操作，完成后释放令牌
3. **获取失败**: 记录日志并跳过当前操作，等待下次执行
4. **令牌释放**: 确保在finally块中释放令牌，避免死锁
5. **服务争用**: McpHeartbeatService、McpAutoRefreshService、McpCacheManager共享同一个令牌

### 优化效果

1. **避免重复刷新**: 防止多个服务同时刷新缓存造成资源浪费
2. **提高性能**: 减少不必要的缓存刷新操作
3. **增强稳定性**: 通过令牌机制确保刷新操作的原子性
4. **改善日志**: 详细记录令牌获取和释放过程，便于问题排查
5. **统一管理**: 使用单个令牌key统一管理三个服务之间的争用关系
6. **优化关闭**: 避免对未启动的服务执行关闭操作，提高应用关闭效率
7. **智能重连**: ping失败时自动重新创建客户端并测试连接，提高连接成功率
8. **资源清理**: 使用DelayQueue自动清理失败的客户端，避免内存泄漏
9. **代码复用**: 通过抽象基类减少重复代码，提高代码质量
10. **统一架构**: 采用Listener模式，提供一致的初始化和关闭机制
11. **架构简化**: 将工具类功能集成到抽象基类中，减少类的数量
12. **提高内聚性**: 相关功能集中在AbstractMcpListener中，提高代码的内聚性

### 配置示例

```yaml
# 启用所有功能的配置
mcp:
  cache:
    preload-on-startup: true      # 启用启动预加载
    heartbeat-interval: 30        # 启用心跳检测（30秒间隔）
    auto-refresh-interval: 600    # 启用自动刷新（10分钟间隔）
    lock-wait-timeout: 3
    retry-count: 3

# 仅启用心跳检测的配置
mcp:
  cache:
    preload-on-startup: true      # 启用启动预加载
    heartbeat-interval: 30        # 启用心跳检测
    auto-refresh-interval: 0      # 禁用自动刷新
    lock-wait-timeout: 3
    retry-count: 3

# 仅启用自动刷新的配置
mcp:
  cache:
    preload-on-startup: true      # 启用启动预加载
    heartbeat-interval: 0         # 禁用心跳检测
    auto-refresh-interval: 600    # 启用自动刷新
    lock-wait-timeout: 3
    retry-count: 3

# 禁用所有功能的配置
mcp:
  cache:
    preload-on-startup: false     # 禁用启动预加载
    heartbeat-interval: 0         # 禁用心跳检测
    auto-refresh-interval: 0      # 禁用自动刷新
    lock-wait-timeout: 3
    retry-count: 3
```