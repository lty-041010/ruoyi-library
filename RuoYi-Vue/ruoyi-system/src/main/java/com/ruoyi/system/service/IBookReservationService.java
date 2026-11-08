package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookReservation;

/**
 * 图书预约Service接口
 */
public interface IBookReservationService
{
    /** 我的预约 */
    List<BookReservation> selectMyReservations();

    /** 预约图书 */
    int reserveBook(Long bookId);

    /** 取消预约 */
    int cancelReservation(Long id);

    /** 后台预约列表（按分馆数据权限） */
    List<BookReservation> selectReservationList(BookReservation reservation);

    /** 到书核销 */
    int handleReservation(Long id);

    /** 后台取消预约 */
    int cancelReservationByAdmin(Long id);

    /** 超期未处理自动取消 */
    int expireReservations();
}
