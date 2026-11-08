package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookReader;

/**
 * 读者扩展Service接口
 *
 * @author LTY
 * @date 2026-11-04
 */
public interface IBookReaderService
{
    BookReader selectBookReaderByUserId(Long userId);

    /**
     * 查询读者扩展
     *
     * @param id 读者扩展主键
     * @return 读者扩展
     */
    public BookReader selectBookReaderById(Long id);

    /**
     * 查询读者扩展列表
     *
     * @param bookReader 读者扩展
     * @return 读者扩展集合
     */
    public List<BookReader> selectBookReaderList(BookReader bookReader);

    /**
     * 新增读者扩展
     *
     * @param bookReader 读者扩展
     * @return 结果
     */
    public int insertBookReader(BookReader bookReader);

    /**
     * 修改读者扩展
     *
     * @param bookReader 读者扩展
     * @return 结果
     */
    public int updateBookReader(BookReader bookReader);

    /**
     * 批量删除读者扩展
     *
     * @param ids 需要删除的读者扩展主键集合
     * @return 结果
     */
    public int deleteBookReaderByIds(Long[] ids);

    /**
     * 删除读者扩展信息
     *
     * @param id 读者扩展主键
     * @return 结果
     */
    public int deleteBookReaderById(Long id);
}
