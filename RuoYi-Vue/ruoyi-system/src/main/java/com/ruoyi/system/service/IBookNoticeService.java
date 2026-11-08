package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookNotice;

/**
 * 站内提醒Service
 */
public interface IBookNoticeService
{
    List<BookNotice> selectMyNotices();

    int countUnread();

    int markRead(Long id);

    int markAllRead();

    /** 给读者发送提醒（按读者ID，内部转换为登录用户） */
    void notifyReader(Long readerId, String title, String content, String type, Long bizId);

    int noticeDueSoon();

    int noticeOverdue();
}
