package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookCategory;

/**
 * 图书分类Service接口
 * 
 * @author LTY
 * @date 2026-11-04
 */
public interface IBookCategoryService 
{
    /**
     * 查询图书分类
     * 
     * @param id 图书分类主键
     * @return 图书分类
     */
    public BookCategory selectBookCategoryById(Long id);

    /**
     * 查询图书分类列表
     * 
     * @param bookCategory 图书分类
     * @return 图书分类集合
     */
    public List<BookCategory> selectBookCategoryList(BookCategory bookCategory);

    /**
     * 新增图书分类
     * 
     * @param bookCategory 图书分类
     * @return 结果
     */
    public int insertBookCategory(BookCategory bookCategory);

    /**
     * 修改图书分类
     * 
     * @param bookCategory 图书分类
     * @return 结果
     */
    public int updateBookCategory(BookCategory bookCategory);

    /**
     * 批量删除图书分类
     * 
     * @param ids 需要删除的图书分类主键集合
     * @return 结果
     */
    public int deleteBookCategoryByIds(Long[] ids);

    /**
     * 删除图书分类信息
     * 
     * @param id 图书分类主键
     * @return 结果
     */
    public int deleteBookCategoryById(Long id);
}
