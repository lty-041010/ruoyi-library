package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookInfo;

/**
 * 图书信息Service接口
 * 
 * @author LTY
 * @date 2026-11-04
 */
public interface IBookInfoService 
{
    /**
     * 批量分配部门
     */
    int assignDept(Long[] ids, Long deptId);

    /**
     * 查询图书信息
     * 
     * @param id 图书信息主键
     * @return 图书信息
     */
    public BookInfo selectBookInfoById(Long id);

    /**
     * 查询图书信息列表
     * 
     * @param bookInfo 图书信息
     * @return 图书信息集合
     */
    public List<BookInfo> selectBookInfoList(BookInfo bookInfo);

    /**
     * 新增图书信息
     * 
     * @param bookInfo 图书信息
     * @return 结果
     */
    public int insertBookInfo(BookInfo bookInfo);

    /**
     * 修改图书信息
     * 
     * @param bookInfo 图书信息
     * @return 结果
     */
    public int updateBookInfo(BookInfo bookInfo);

    /**
     * 批量删除图书信息
     * 
     * @param ids 需要删除的图书信息主键集合
     * @return 结果
     */
    public int deleteBookInfoByIds(Long[] ids);

    /**
     * 删除图书信息信息
     * 
     * @param id 图书信息主键
     * @return 结果
     */
    public int deleteBookInfoById(Long id);
}
