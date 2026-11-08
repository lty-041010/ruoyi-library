package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookBorrow;

/**
 * 借阅记录Service接口
 *
 * @author LTY
 * @date 2026‑11‑04
 */
public interface IBookBorrowService
{
    /**
     * 查询借阅记录
     *
     * @param id 借阅记录主键
     * @return 借阅记录
     */
    BookBorrow selectBookBorrowById(Long id);

    /**
     * 查询借阅记录列表
     *
     * @param bookBorrow 借阅记录
     * @return 借阅记录集合
     */
    List<BookBorrow> selectBookBorrowList(BookBorrow bookBorrow);

    /**
     * 新增借阅记录
     *
     * @param bookBorrow 借阅记录
     * @return 结果
     */
    int insertBookBorrow(BookBorrow bookBorrow);

    /**
     * 修改借阅记录
     *
     * @param bookBorrow 借阅记录
     * @return 结果
     */
    int updateBookBorrow(BookBorrow bookBorrow);

    /**
     * 批量删除借阅记录
     *
     * @param ids 需要删除的借阅记录主键集合
     * @return 结果
     */
    int deleteBookBorrowByIds(Long[] ids);

    /**
     * 删除借阅记录信息
     *
     * @param id 借阅记录主键
     * @return 结果
     */
    int deleteBookBorrowById(Long id);

    /**
     * 批量更新逾期的借阅记录（定时任务调用）
     * @return 更新条数
     */
    int updateOverdueStatus();

    /**
     * 跨馆代还
     */
    String crossReturnBook(String cardNo, String isbn);

    /** 扫码借阅：借书证号 + ISBN */
    String scanBorrow(String cardNo, String isbn);

    /**
     * 查询跨馆代还列表
     */
    List<BookBorrow> selectCrossReturnList(String cardNo, String bookName);

    /**
     * 原子修改借阅状态（校验原始状态，防止并发，不刷新页面重复操作失效）
     * @param id 记录ID
     * @param oldStatus 数据库原始状态
     * @param newStatus 目标新状态
     * @param updateBy 操作人
     * @return true成功
     */
    boolean updateBorrowStatus(Long id, String oldStatus, String newStatus, String updateBy);

    /**
     * 读者续借：仅本人、借阅中、未逾期、续借次数未超上限
     * @param id 借阅记录ID
     * @return 新的应还时间
     */
    java.util.Date renewBook(Long id);
}
