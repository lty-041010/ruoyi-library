package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.BookReservation;

/**
 * 图书预约Mapper接口
 */
public interface BookReservationMapper
{
    List<BookReservation> selectReservationList(BookReservation reservation);

    List<BookReservation> selectMyReservations(@Param("readerId") Long readerId);

    BookReservation selectReservationById(Long id);

    int countActiveReservation(@Param("readerId") Long readerId,
                               @Param("bookId") Long bookId);

    int insertReservation(BookReservation reservation);

    int cancelReservation(@Param("id") Long id,
                          @Param("readerId") Long readerId,
                          @Param("updateBy") String updateBy);

    /** 某本书当前有效预约数量（用于续借拦截） */
    int countActiveReservationByBook(@Param("bookId") Long bookId);

    /** 后台到书核销：等待中 -> 已完成 */
    int handleReservation(@Param("id") Long id,
                          @Param("updateBy") String updateBy);

    /** 后台取消：等待中 -> 已取消 */
    int cancelReservationById(@Param("id") Long id,
                              @Param("updateBy") String updateBy);

    /** 超期未处理预约自动取消，返回条数 */
    int expireReservations(@Param("days") int days);
}
