package com.ruoyi.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BookNotice;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.mapper.BookNoticeMapper;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.service.IBookNoticeService;

@Service
public class BookNoticeServiceImpl implements IBookNoticeService
{
    private static final Logger log = LoggerFactory.getLogger(BookNoticeServiceImpl.class);

    @Autowired
    private BookNoticeMapper noticeMapper;

    @Autowired
    private BookReaderMapper readerMapper;

    @Override
    public List<BookNotice> selectMyNotices()
    {
        return noticeMapper.selectMyNotices(SecurityUtils.getLoginUser().getUserId());
    }

    @Override
    public int countUnread()
    {
        return noticeMapper.countUnread(SecurityUtils.getLoginUser().getUserId());
    }

    @Override
    public int markRead(Long id)
    {
        return noticeMapper.markRead(id, SecurityUtils.getLoginUser().getUserId());
    }

    @Override
    public int markAllRead()
    {
        return noticeMapper.markAllRead(SecurityUtils.getLoginUser().getUserId());
    }

    @Override
    public void notifyReader(Long readerId, String title, String content, String type, Long bizId)
    {
        if (readerId == null) {
            return;
        }
        try {
            BookReader reader = readerMapper.selectBookReaderById(readerId);
            if (reader == null || reader.getUserId() == null) {
                return;
            }
            if (noticeMapper.countByBiz(reader.getUserId(), type, bizId) > 0) {
                return;
            }
            BookNotice notice = new BookNotice();
            notice.setUserId(reader.getUserId());
            notice.setTitle(title);
            notice.setContent(content);
            notice.setType(type);
            notice.setBizId(bizId);
            noticeMapper.insertNotice(notice);
        } catch (Exception e) {
            // 提醒失败不影响主业务
            log.warn("发送站内提醒失败：readerId={}, type={}, bizId={}", readerId, type, bizId, e);
        }
    }

    @Override
    public int noticeDueSoon()
    {
        List<BookNotice> candidates = noticeMapper.selectDueSoonCandidates(3);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int count = 0;
        for (BookNotice c : candidates) {
            if (c.getReaderUserId() == null) {
                continue;
            }
            if (noticeMapper.countByBiz(c.getReaderUserId(), "due", c.getBizId()) > 0) {
                continue;
            }
            String title = "《" + c.getBookName() + "》即将到期";
            String content = "该书应还时间为 " + (c.getDueTime() == null ? "—" : sdf.format(c.getDueTime()))
                    + "，可到读者门户自助续借。";
            noticeMapper.insertNotice(build(c, title, content, "due"));
            count++;
        }
        return count;
    }

    @Override
    public int noticeOverdue()
    {
        List<BookNotice> candidates = noticeMapper.selectOverdueCandidates();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int count = 0;
        for (BookNotice c : candidates) {
            if (c.getReaderUserId() == null) {
                continue;
            }
            if (noticeMapper.countByBiz(c.getReaderUserId(), "overdue", c.getBizId()) > 0) {
                continue;
            }
            String title = "《" + c.getBookName() + "》已逾期";
            String content = "应还时间为 " + (c.getDueTime() == null ? "—" : sdf.format(c.getDueTime()))
                    + "，请尽快归还，逾期会影响继续借阅。";
            noticeMapper.insertNotice(build(c, title, content, "overdue"));
            count++;
        }
        return count;
    }

    private BookNotice build(BookNotice c, String title, String content, String type) {
        BookNotice notice = new BookNotice();
        notice.setUserId(c.getReaderUserId());
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        notice.setBizId(c.getBizId());
        return notice;
    }
}
