package com.testplatform.service.env;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import com.testplatform.common.PageResult;
import com.testplatform.dto.env.EnvironmentCreateRequest;
import com.testplatform.dto.env.EnvironmentQueryRequest;
import com.testplatform.dto.env.EnvironmentUpdateRequest;
import com.testplatform.dto.env.EnvironmentVO;
import com.testplatform.dto.env.HealthRecordVO;
import com.testplatform.entity.TdEnvironment;
import com.testplatform.entity.TdEnvironmentHealth;
import com.testplatform.mapper.TdEnvironmentHealthMapper;
import com.testplatform.mapper.TdEnvironmentMapper;
import com.testplatform.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 环境管理服务实现（REQ-005）
 */
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    /** 分页默认值与上限（见 接口设计 1.3） */
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 10L;
    private static final long MAX_SIZE = 100L;

    /** 健康自检超时（秒） */
    private static final Duration HEALTH_TIMEOUT = Duration.ofSeconds(3);

    /** 健康记录查询条数上限 */
    private static final int HEALTH_RECORD_LIMIT = 10;

    /** 失败原因存储上限（表字段 varchar(500)） */
    private static final int ERROR_MSG_MAX = 500;

    private final TdEnvironmentMapper environmentMapper;

    private final TdEnvironmentHealthMapper healthMapper;

    @Override
    public PageResult<EnvironmentVO> page(EnvironmentQueryRequest query) {
        long page = query.getPage() == null || query.getPage() < 1 ? DEFAULT_PAGE : query.getPage();
        long size = query.getSize() == null || query.getSize() < 1 ? DEFAULT_SIZE : Math.min(query.getSize(), MAX_SIZE);
        String keyword = query.getKeyword();

        LambdaQueryWrapper<TdEnvironment> wrapper = Wrappers.<TdEnvironment>lambdaQuery()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TdEnvironment::getEnvName, keyword)
                        .or().like(TdEnvironment::getEnvCode, keyword)
                        .or().like(TdEnvironment::getDescription, keyword))
                .orderByAsc(TdEnvironment::getSort)
                .orderByAsc(TdEnvironment::getId);

        Page<TdEnvironment> result = environmentMapper.selectPage(new Page<>(page, size), wrapper);
        List<EnvironmentVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page, size, result.getTotal(), records);
    }

    @Override
    public List<EnvironmentVO> listAllEnabled() {
        return environmentMapper.selectList(
                        Wrappers.<TdEnvironment>lambdaQuery()
                                .eq(TdEnvironment::getStatus, 1)
                                .orderByAsc(TdEnvironment::getSort)
                                .orderByAsc(TdEnvironment::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public EnvironmentVO create(EnvironmentCreateRequest request) {
        Long exists = environmentMapper.selectCount(
                Wrappers.<TdEnvironment>lambdaQuery().eq(TdEnvironment::getEnvCode, request.getEnvCode()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "环境编码已存在");
        }

        TdEnvironment env = new TdEnvironment();
        env.setEnvCode(request.getEnvCode());
        env.setEnvName(request.getEnvName());
        env.setBaseUrl(request.getBaseUrl());
        env.setDescription(request.getDescription());
        env.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        env.setCreatedBy(currentUserId());
        environmentMapper.insert(env);
        return toVO(environmentMapper.selectById(env.getId()));
    }

    @Override
    public void update(Long id, EnvironmentUpdateRequest request) {
        TdEnvironment env = requireEnv(id);

        // env_code 唯一性检查（排除自身）
        Long exists = environmentMapper.selectCount(
                Wrappers.<TdEnvironment>lambdaQuery()
                        .eq(TdEnvironment::getEnvCode, request.getEnvCode())
                        .ne(TdEnvironment::getId, id));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "环境编码已存在");
        }

        env.setEnvCode(request.getEnvCode());
        env.setEnvName(request.getEnvName());
        env.setBaseUrl(request.getBaseUrl());
        env.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            env.setStatus(request.getStatus());
        }
        environmentMapper.updateById(env);
    }

    @Override
    public void delete(Long id) {
        requireEnv(id);
        // 逻辑删除（MyBatis-Plus 自动 UPDATE deleted=1），健康记录保留
        environmentMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "状态值只能为 0 或 1");
        }
        TdEnvironment env = requireEnv(id);
        env.setStatus(status);
        environmentMapper.updateById(env);
    }

    @Override
    public HealthRecordVO healthCheck(Long id) {
        TdEnvironment env = requireEnv(id);
        String target = env.getBaseUrl();
        String status = "DOWN";
        Integer latencyMs = 0;
        String errorMsg = null;

        if (StringUtils.hasText(target)) {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(HEALTH_TIMEOUT)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(target))
                    .timeout(HEALTH_TIMEOUT)
                    .GET()
                    .build();
            long start = System.currentTimeMillis();
            try {
                HttpResponse<Void> resp = client.send(request, HttpResponse.BodyHandlers.discarding());
                latencyMs = (int) (System.currentTimeMillis() - start);
                int code = resp.statusCode();
                if (code >= 200 && code < 400) {
                    status = "UP";
                } else {
                    errorMsg = "HTTP " + code;
                }
            } catch (Exception e) {
                latencyMs = (int) (System.currentTimeMillis() - start);
                errorMsg = truncate(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            }
        } else {
            errorMsg = "未配置 base_url";
        }

        TdEnvironmentHealth record = new TdEnvironmentHealth();
        record.setEnvId(id);
        record.setCheckType("HTTP");
        record.setTarget(target);
        record.setStatus(status);
        record.setLatencyMs(latencyMs);
        record.setErrorMsg(errorMsg);
        record.setCheckedBy(currentUserId());
        healthMapper.insert(record);
        return toHealthVO(record);
    }

    @Override
    public List<HealthRecordVO> healthRecords(Long id) {
        requireEnv(id);
        return healthMapper.selectList(
                        Wrappers.<TdEnvironmentHealth>lambdaQuery()
                                .eq(TdEnvironmentHealth::getEnvId, id)
                                .orderByDesc(TdEnvironmentHealth::getId)
                                .last("LIMIT " + HEALTH_RECORD_LIMIT))
                .stream().map(this::toHealthVO).toList();
    }

    /**
     * 查询环境，不存在抛 40400
     */
    private TdEnvironment requireEnv(Long id) {
        TdEnvironment env = environmentMapper.selectById(id);
        if (env == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "环境不存在");
        }
        return env;
    }

    /**
     * 当前登录用户 ID（未登录兜底 0）
     */
    private Long currentUserId() {
        LoginUser loginUser = LoginUser.get();
        return loginUser != null ? loginUser.getUserId() : 0L;
    }

    /**
     * 超长失败原因截断（表字段 varchar(500)）
     */
    private String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() > ERROR_MSG_MAX ? msg.substring(0, ERROR_MSG_MAX) : msg;
    }

    /**
     * 环境实体转 VO
     */
    private EnvironmentVO toVO(TdEnvironment env) {
        EnvironmentVO vo = new EnvironmentVO();
        vo.setId(env.getId());
        vo.setEnvCode(env.getEnvCode());
        vo.setEnvName(env.getEnvName());
        vo.setBaseUrl(env.getBaseUrl());
        vo.setDescription(env.getDescription());
        vo.setSort(env.getSort());
        vo.setStatus(env.getStatus());
        vo.setCreatedBy(env.getCreatedBy());
        vo.setCreatedAt(env.getCreatedAt());
        return vo;
    }

    /**
     * 健康记录实体转 VO
     */
    private HealthRecordVO toHealthVO(TdEnvironmentHealth record) {
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(record.getId());
        vo.setEnvId(record.getEnvId());
        vo.setCheckType(record.getCheckType());
        vo.setTarget(record.getTarget());
        vo.setStatus(record.getStatus());
        vo.setLatencyMs(record.getLatencyMs());
        vo.setErrorMsg(record.getErrorMsg());
        vo.setCheckedBy(record.getCheckedBy());
        vo.setCheckedAt(record.getCheckedAt());
        return vo;
    }
}