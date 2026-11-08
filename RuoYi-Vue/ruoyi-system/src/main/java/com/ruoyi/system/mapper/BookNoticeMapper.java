package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.BookNotice;

/**
 * 站内提醒Mapper
 */
public interface BookNoticeMapper
{
    int insertNotice(BookNotice notice);

    List<BookNotice> selectMyNotices(@Param("userId") Long userId);

    int countUnread(@Param("userId") Long userId);

    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    int markAllRead(@Param("userId") Long userId);

    int countByBiz(@Param("userId") Long userId, @Param("type") String type, @Param("bizId") Long bizId);

    List<BookNotice> selectDueSoonCandidates(@Param("days") int days);

    List<BookNotice> selectOverdueCandidates();
}
