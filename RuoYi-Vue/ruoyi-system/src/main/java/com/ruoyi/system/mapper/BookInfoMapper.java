package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 图书信息Mapper接口
 *
 * @author LTY
 * @date 2026-11-04
 */
public interface BookInfoMapper
{
    /**
     * 跨馆还书时，扣减原馆的总库存
     */
    int decreaseTotalStock(@Param("bookId") Long bookId);
    // 统计总册数
    Long selectTotalStock();

    // 统计可用册数
    Long selectAvailableStock();
    /**
     * 根据 ISBN 和 部门 增加库存
     */
    int addStockByIsbnAndDept(@Param("isbn") String isbn, @Param("deptId") Long deptId);

    /** 按 ISBN 查询在架图书（扫码借阅） */
    BookInfo selectBookInfoByIsbn(@Param("isbn") String isbn);
    /**
     * 批量分配部门
     */
    int assignDept(@Param("ids") Long[] ids, @Param("deptId") Long deptId);

    /**
     * 查询图书信息
     *
     * @param id 图书信息主键
     * @return 图书信息
     */
    BookInfo selectBookInfoById(Long id);

    /**
     * 查询图书信息列表
     *
     * @param bookInfo 图书信息
     * @return 图书信息集合
     */
    List<BookInfo> selectBookInfoList(BookInfo bookInfo);

    /**
     * 新增图书信息
     *
     * @param bookInfo 图书信息
     * @return 结果
     */
    int insertBookInfo(BookInfo bookInfo);

    /**
     * 修改图书信息
     *
     * @param bookInfo 图书信息
     * @return 结果
     */
    int updateBookInfo(BookInfo bookInfo);

    /**
     * 删除图书信息
     *
     * @param id 图书信息主键
     * @return 结果
     */
    int deleteBookInfoById(Long id);

    /**
     * 批量删除图书信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteBookInfoByIds(Long[] ids);

    // ================= 以下为新增方法 =================

    /**
     * 扣减可用库存（借书用）
     * @param bookId 图书ID
     * @return 影响行数，0表示库存不足或已删除
     */
    int decreaseAvailableStock(@Param("bookId") Long bookId);

    /**
     * 归还可用库存（还书用）
     * @param bookId 图书ID
     * @return 影响行数，0表示数据异常
     */
    int increaseAvailableStock(@Param("bookId") Long bookId);

    /**
     * 校验同一部门下 ISBN 是否唯一
     */
    int checkIsbnUnique(@Param("isbn") String isbn, @Param("deptId") Long deptId, @Param("id") Long id);

    /**
     * 校验图书是否有在借记录（删除前校验）
     */
    int checkBookHasActiveBorrow(@Param("bookId") Long bookId);
}