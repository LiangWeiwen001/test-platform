package com.testplatform.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应体
 */
@Data
public class PageResult<T> {

    /** 当前页码（从 1 开始） */
    private long page;

    /** 每页条数 */
    private long size;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long page, long size, long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }
}