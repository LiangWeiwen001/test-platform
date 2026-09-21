package com.testplatform.service.testcase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import com.testplatform.common.PageResult;
import com.testplatform.dto.testcase.*;
import com.testplatform.entity.TcCase;
import com.testplatform.entity.TcCaseTag;
import com.testplatform.mapper.TcCaseMapper;
import com.testplatform.mapper.TcCaseTagMapper;
import com.testplatform.security.LoginUser;
import com.testplatform.service.testcase.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用例管理服务实现（REQ-006）
 */
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final TcCaseMapper caseMapper;
    private final TcCaseTagMapper tagMapper;

    private static final String DEFAULT_VERSION = "v1.0";

    @Override
    public PageResult<CaseVO> page(CaseQueryRequest request) {
        long page = request.getPage() == null ? 1 : request.getPage();
        long size = request.getSize() == null ? 10 : Math.min(request.getSize(), 100);
        Page<TcCase> p = new Page<>(page, size);
        LambdaQueryWrapper<TcCase> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            qw.and(w -> w.like(TcCase::getCaseName, kw).or().like(TcCase::getDescription, kw));
        }
        if (StringUtils.hasText(request.getCaseType())) {
            qw.eq(TcCase::getCaseType, request.getCaseType());
        }
        if (StringUtils.hasText(request.getCaseLevel())) {
            qw.eq(TcCase::getCaseLevel, request.getCaseLevel());
        }
        if (request.getStatus() != null) {
            qw.eq(TcCase::getStatus, request.getStatus());
        }
        if (request.getTagId() != null) {
            // tagId 关联查询：tag_ids 逗号分隔，用 LIKE 匹配
            qw.like(TcCase::getTagIds, request.getTagId());
        }
        qw.orderByDesc(TcCase::getId);
        Page<TcCase> result = caseMapper.selectPage(p, qw);
        List<CaseVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page, size, result.getTotal(), records);
    }

    @Override
    public CaseVO detail(Long id) {
        TcCase entity = requireCase(id);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CaseCreateRequest request) {
        TcCase entity = new TcCase();
        entity.setCaseName(request.getCaseName().trim());
        entity.setCaseType(request.getCaseType());
        entity.setCaseLevel(request.getCaseLevel());
        entity.setDescription(request.getDescription());
        entity.setRequestConfig(request.getRequestConfig());
        entity.setExpectedResult(request.getExpectedResult());
        entity.setTagIds(request.getTagIds());
        entity.setVersion(DEFAULT_VERSION);
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setCreatedBy(currentUserId());
        entity.setUpdatedBy(currentUserId());
        caseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CaseUpdateRequest request) {
        TcCase entity = requireCase(id);
        entity.setCaseName(request.getCaseName().trim());
        entity.setCaseType(request.getCaseType());
        entity.setCaseLevel(request.getCaseLevel());
        entity.setDescription(request.getDescription());
        entity.setRequestConfig(request.getRequestConfig());
        entity.setExpectedResult(request.getExpectedResult());
        entity.setTagIds(request.getTagIds());
        // 编辑后版本递增：v1.0 -> v1.1
        entity.setVersion(nextVersion(entity.getVersion()));
        entity.setUpdatedBy(currentUserId());
        caseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireCase(id);
        caseMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        TcCase entity = requireCase(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "状态值非法");
        }
        entity.setStatus(status);
        entity.setUpdatedBy(currentUserId());
        caseMapper.updateById(entity);
    }

    @Override
    public List<CaseTagVO> listTags() {
        List<TcCaseTag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<TcCaseTag>().orderByDesc(TcCaseTag::getId));
        List<TcCase> cases = caseMapper.selectList(null);
        // 统计每个标签关联用例数
        Map<Long, Long> countMap = new java.util.HashMap<>();
        for (TcCase c : cases) {
            if (c == null || !StringUtils.hasText(c.getTagIds())) {
                continue;
            }
            for (String tid : c.getTagIds().split(",")) {
                try {
                    Long tagId = Long.valueOf(tid.trim());
                    countMap.merge(tagId, 1L, Long::sum);
                } catch (NumberFormatException ignored) {
                    // 忽略非法标签 ID
                }
            }
        }
        return tags.stream().map(t -> {
            CaseTagVO vo = new CaseTagVO();
            vo.setId(t.getId());
            vo.setTagName(t.getTagName());
            vo.setTagColor(t.getTagColor());
            vo.setCaseCount(countMap.getOrDefault(t.getId(), 0L));
            vo.setCreatedAt(t.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(CaseTagCreateRequest request) {
        String tagName = request.getTagName().trim();
        Long exists = tagMapper.selectCount(
                new LambdaQueryWrapper<TcCaseTag>().eq(TcCaseTag::getTagName, tagName));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "标签已存在");
        }
        TcCaseTag tag = new TcCaseTag();
        tag.setTagName(tagName);
        tag.setTagColor(StringUtils.hasText(request.getTagColor()) ? request.getTagColor() : "#409EFF");
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        tagMapper.deleteById(requireTag(id).getId());
        // 清理用例中引用该标签的 tagIds
        List<TcCase> cases = caseMapper.selectList(
                new LambdaQueryWrapper<TcCase>().like(TcCase::getTagIds, id));
        for (TcCase c : cases) {
            String[] ids = c.getTagIds().split(",");
            List<String> remain = new ArrayList<>();
            for (String tid : ids) {
                if (!tid.trim().equals(String.valueOf(id))) {
                    remain.add(tid.trim());
                }
            }
            c.setTagIds(String.join(",", remain));
            caseMapper.updateById(c);
        }
    }

    // ===== 私有方法 =====

    private TcCase requireCase(Long id) {
        TcCase entity = caseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用例不存在");
        }
        return entity;
    }

    private TcCaseTag requireTag(Long id) {
        TcCaseTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "标签不存在");
        }
        return tag;
    }

    private CaseVO toVO(TcCase entity) {
        CaseVO vo = new CaseVO();
        vo.setId(entity.getId());
        vo.setCaseName(entity.getCaseName());
        vo.setCaseType(entity.getCaseType());
        vo.setCaseLevel(entity.getCaseLevel());
        vo.setDescription(entity.getDescription());
        vo.setRequestConfig(entity.getRequestConfig());
        vo.setExpectedResult(entity.getExpectedResult());
        vo.setTagIds(entity.getTagIds());
        vo.setTagNames(resolveTagNames(entity.getTagIds()));
        vo.setVersion(entity.getVersion());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private List<String> resolveTagNames(String tagIds) {
        if (!StringUtils.hasText(tagIds)) {
            return List.of();
        }
        List<Long> ids = Arrays.stream(tagIds.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> {
                    try {
                        return Long.valueOf(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectBatchIds(ids).stream().map(TcCaseTag::getTagName).toList();
    }

    private String nextVersion(String version) {
        if (!StringUtils.hasText(version) || !version.startsWith("v")) {
            return DEFAULT_VERSION;
        }
        try {
            double v = Double.parseDouble(version.substring(1));
            return "v" + String.format("%.1f", v + 0.1);
        } catch (NumberFormatException e) {
            return DEFAULT_VERSION;
        }
    }

    private Long currentUserId() {
        try {
            return LoginUser.get().getUserId();
        } catch (Exception e) {
            return 0L;
        }
    }
}