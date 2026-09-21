package com.testplatform.service.search.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.dto.search.CaseHitVO;
import com.testplatform.dto.search.EnvHitVO;
import com.testplatform.dto.search.SearchResultVO;
import com.testplatform.dto.search.UserHitVO;
import com.testplatform.entity.SysUser;
import com.testplatform.entity.TcCase;
import com.testplatform.entity.TdEnvironment;
import com.testplatform.mapper.SysUserMapper;
import com.testplatform.mapper.TcCaseMapper;
import com.testplatform.mapper.TdEnvironmentMapper;
import com.testplatform.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局搜索实现（REQ-008）
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int LIMIT = 10;

    private final TcCaseMapper caseMapper;
    private final TdEnvironmentMapper environmentMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public SearchResultVO search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        SearchResultVO result = new SearchResultVO();
        result.setKeyword(kw);
        result.setCases(searchCases(kw));
        result.setEnvs(searchEnvs(kw));
        result.setUsers(searchUsers(kw));
        return result;
    }

    private List<CaseHitVO> searchCases(String kw) {
        List<CaseHitVO> hits = new ArrayList<>();
        if (kw.isEmpty()) {
            return hits;
        }
        // 按 case_name LIKE 优先
        List<TcCase> byName = caseMapper.selectList(new LambdaQueryWrapper<TcCase>()
                .like(TcCase::getCaseName, kw).last("LIMIT " + LIMIT));
        for (TcCase c : byName) {
            CaseHitVO vo = new CaseHitVO();
            vo.setId(c.getId());
            vo.setName(c.getCaseName());
            vo.setType(c.getCaseType());
            vo.setLevel(c.getCaseLevel());
            vo.setMatchField("case_name");
            hits.add(vo);
        }
        if (hits.size() >= LIMIT) {
            return hits;
        }
        // 再按 description LIKE 补足
        List<TcCase> byDesc = caseMapper.selectList(new LambdaQueryWrapper<TcCase>()
                .like(TcCase::getDescription, kw).last("LIMIT " + (LIMIT - hits.size())));
        for (TcCase c : byDesc) {
            // 避免与 byName 重复
            boolean dup = hits.stream().anyMatch(h -> h.getId().equals(c.getId()));
            if (dup) {
                continue;
            }
            CaseHitVO vo = new CaseHitVO();
            vo.setId(c.getId());
            vo.setName(c.getCaseName());
            vo.setType(c.getCaseType());
            vo.setLevel(c.getCaseLevel());
            vo.setMatchField("description");
            hits.add(vo);
            if (hits.size() >= LIMIT) {
                break;
            }
        }
        return hits;
    }

    private List<EnvHitVO> searchEnvs(String kw) {
        List<EnvHitVO> hits = new ArrayList<>();
        if (kw.isEmpty()) {
            return hits;
        }
        List<TdEnvironment> byName = environmentMapper.selectList(new LambdaQueryWrapper<TdEnvironment>()
                .like(TdEnvironment::getEnvName, kw).last("LIMIT " + LIMIT));
        for (TdEnvironment e : byName) {
            EnvHitVO vo = new EnvHitVO();
            vo.setId(e.getId());
            vo.setName(e.getEnvName());
            vo.setCode(e.getEnvCode());
            vo.setMatchField("env_name");
            hits.add(vo);
        }
        if (hits.size() >= LIMIT) {
            return hits;
        }
        List<TdEnvironment> byCode = environmentMapper.selectList(new LambdaQueryWrapper<TdEnvironment>()
                .like(TdEnvironment::getEnvCode, kw).last("LIMIT " + LIMIT));
        for (TdEnvironment e : byCode) {
            boolean dup = hits.stream().anyMatch(h -> h.getId().equals(e.getId()));
            if (dup) {
                continue;
            }
            EnvHitVO vo = new EnvHitVO();
            vo.setId(e.getId());
            vo.setName(e.getEnvName());
            vo.setCode(e.getEnvCode());
            vo.setMatchField("env_code");
            hits.add(vo);
            if (hits.size() >= LIMIT) {
                break;
            }
        }
        return hits;
    }

    private List<UserHitVO> searchUsers(String kw) {
        List<UserHitVO> hits = new ArrayList<>();
        if (kw.isEmpty()) {
            return hits;
        }
        List<SysUser> byUsername = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .like(SysUser::getUsername, kw).last("LIMIT " + LIMIT));
        for (SysUser u : byUsername) {
            UserHitVO vo = new UserHitVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setMatchField("username");
            hits.add(vo);
        }
        if (hits.size() >= LIMIT) {
            return hits;
        }
        List<SysUser> byNickname = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .like(SysUser::getNickname, kw).last("LIMIT " + (LIMIT - hits.size())));
        for (SysUser u : byNickname) {
            boolean dup = hits.stream().anyMatch(h -> h.getId().equals(u.getId()));
            if (dup) {
                continue;
            }
            UserHitVO vo = new UserHitVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setMatchField("nickname");
            hits.add(vo);
            if (hits.size() >= LIMIT) {
                break;
            }
        }
        return hits;
    }
}