package com.ruoyi.quartz.task;

import com.ruoyi.system.service.IBookBorrowService;
import com.ruoyi.system.service.IBookReservationService;
import com.ruoyi.system.service.IBookNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 图书管理定时任务：更新逾期借阅状态
 */
@Component("libraryTask")
public class LibraryTask {

    private static final Logger log = LoggerFactory.getLogger(LibraryTask.class);

    @Autowired
    private IBookBorrowService bookBorrowService;

    @Autowired
    private IBookReservationService bookReservationService;

    @Autowired
    private IBookNoticeService bookNoticeService;

    /**
     * Quartz后台调用方法名：libraryTask.updateOverdue()
     */
    public void updateOverdue() {
        try {
            int count = bookBorrowService.updateOverdueStatus();
            int notice = bookNoticeService.noticeOverdue();
            log.info("【图书逾期同步任务】执行完毕，更新逾期记录：{}，发送逾期提醒：{}", count, notice);
        } catch (Exception e) {
            log.error("【图书逾期同步任务】执行异常", e);
        }
    }

    /**
     * 预约超期释放：超过 7 天未处理自动取消
     * Quartz 调用方法名：libraryTask.expireReservation()
     */
    public void expireReservation() {
        try {
            int count = bookReservationService.expireReservations();
            log.info("【预约超期释放任务】执行完毕，本次自动取消预约数量：{}", count);
        } catch (Exception e) {
            log.error("【预约超期释放任务】执行异常", e);
        }
    }

    /**
     * 到期前 3 天提醒
     * Quartz 调用方法名：libraryTask.noticeDueSoon()
     */
    public void noticeDueSoon() {
        try {
            int count = bookNoticeService.noticeDueSoon();
            log.info("【到期提醒任务】执行完毕，发送提醒：{}", count);
        } catch (Exception e) {
            log.error("【到期提醒任务】执行异常", e);
        }
    }
}
