package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookReader;
import org.apache.ibatis.annotations.Param;

public interface BookReaderMapper
{

    /**
     * 减少当前借阅数量（还书）
     */
    int decreaseCurrentBorrowNum(Long readerId);

    /**
     * 增加读者当前借阅数量
     * @param readerId 读者ID
     * @return 影响行数
     */
    int increaseCurrentBorrowNum(Long readerId);

    BookReader selectBookReaderByUserId(Long userId);

    BookReader selectBookReaderById(Long id);

    /** 按借书证号查询（扫码借还） */
    BookReader selectBookReaderByCardNo(@Param("cardNo") String cardNo);

    List<BookReader> selectBookReaderList(BookReader bookReader);

    int selectBookReaderCount(BookReader bookReader);

    int insertBookReader(BookReader bookReader);

    int updateBookReader(BookReader bookReader);

    int deleteBookReaderById(Long id);

    int deleteBookReaderByIds(Long[] ids);

    /** 校验卡号唯一 */
    int checkCardNoUnique(@Param("cardNo") String cardNo, @Param("deptId") Long deptId, @Param("id") Long id);

    /** 校验sys_user userId唯一 */
    int checkUserIdUnique(@Param("userId") Long userId, @Param("id") Long id);

    /** 判断读者是否存在未归还图书 */
    int checkReaderHasUnreturnedBorrow(@Param("readerId") Long readerId);
}
