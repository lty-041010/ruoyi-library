package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookBorrow;
import org.apache.ibatis.annotations.Param;

/**
 * 借阅记录Mapper接口
 */
public interface BookBorrowMapper
{
    Long selectBookBorrowCount(BookBorrow bookBorrow);

    List<BookBorrow> selectCrossReturnList(@Param("cardNo") String cardNo,
                                           @Param("bookName") String bookName,
                                           @Param("currentDeptId") Long currentDeptId);

    BookBorrow selectActiveBorrowForCrossReturn(@Param("cardNo") String cardNo,
                                                @Param("isbn") String isbn);

    BookBorrow selectBookBorrowById(Long id);

    List<BookBorrow> selectBookBorrowList(BookBorrow bookBorrow);

    int insertBookBorrow(BookBorrow bookBorrow);

    int updateBookBorrow(BookBorrow bookBorrow);

    int deleteBookBorrowById(Long id);

    int deleteBookBorrowByIds(Long[] ids);

    int updateOverdueStatus();

    /**
     * 续借：更新应还时间并将续借次数 +1
     */
    int updateRenewInfo(@Param("id") Long id,
                        @Param("dueTime") java.util.Date dueTime,
                        @Param("updateBy") String updateBy);

    /**
     * 原子更新借阅状态：必须匹配原始oldStatus
     */
    int updateBorrowStatus(@Param("id") Long id,
                           @Param("oldStatus") String oldStatus,
                           @Param("newStatus") String newStatus,
                           @Param("updateBy") String updateBy);
}
