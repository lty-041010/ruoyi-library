package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IBookNoticeService;

/**
 * 站内提醒
 */
@RestController
@RequestMapping("/book/notice")
public class BookNoticeController extends BaseController
{
    @Autowired
    private IBookNoticeService noticeService;

    @GetMapping("/my")
    public AjaxResult my()
    {
        return success(noticeService.selectMyNotices());
    }

    @GetMapping("/unread")
    public AjaxResult unread()
    {
        return success(noticeService.countUnread());
    }

    @PutMapping("/read/{id}")
    public AjaxResult read(@PathVariable Long id)
    {
        noticeService.markRead(id);
        return success();
    }

    @PutMapping("/readAll")
    public AjaxResult readAll()
    {
        noticeService.markAllRead();
        return success();
    }
}
