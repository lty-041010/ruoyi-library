package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.system.domain.BookBorrow;
import com.ruoyi.system.domain.BookInfo;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.domain.BookReservation;
import com.ruoyi.system.mapper.BookBorrowMapper;
import com.ruoyi.system.mapper.BookInfoMapper;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.mapper.BookReservationMapper;
import com.ruoyi.system.service.IBookReservationService;
import com.ruoyi.system.service.IBookNoticeService;

@Service
public class BookReservationServiceImpl implements IBookReservationService
{
    @Autowired
    private BookReservationMapper reservationMapper;
    @Autowired
    private BookReaderMapper bookReaderMapper;
    @Autowired
    private BookInfoMapper bookInfoMapper;
    @Autowired
    private BookBorrowMapper bookBorrowMapper;
    @Autowired
    private IBookNoticeService noticeService;

    @Override
    public List<BookReservation> selectMyReservations() {
        BookReader reader = currentReader();
        if (reader == null) {
            return new ArrayList<>();
        }
        return reservationMapper.selectMyReservations(reader.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reserveBook(Long bookId) {
        BookReader reader = currentReader();
        if (reader == null) {
            throw new ServiceException("预约失败：当前账号没有读者档案");
        }
        BookInfo book = bookInfoMapper.selectBookInfoById(bookId);
        if (book == null) {
            throw new ServiceException("预约失败：图书不存在");
        }
        if (!"0".equals(book.getStatus())) {
            throw new ServiceException("预约失败：该图书已下架");
        }
        if (reader.getDeptId() != null && book.getDeptId() != null
                && !reader.getDeptId().equals(book.getDeptId())) {
            throw new ServiceException("预约失败：该图书不属于你所在的分馆");
        }
        if (book.getAvailableStock() != null && book.getAvailableStock() > 0) {
            throw new ServiceException("该书当前有库存，可直接到馆借阅，无需预约");
        }
        BookBorrow active = new BookBorrow();
        active.setReaderId(reader.getId());
        active.setBookId(bookId);
        active.setStatus("0");
        List<BookBorrow> activeList = bookBorrowMapper.selectBookBorrowList(active);
        if (activeList != null && !activeList.isEmpty()) {
            throw new ServiceException("预约失败：你正在借阅这本书");
        }
        if (reservationMapper.countActiveReservation(reader.getId(), bookId) > 0) {
            throw new ServiceException("预约失败：你已预约过这本书，请勿重复预约");
        }

        BookReservation reservation = new BookReservation();
        reservation.setReaderId(reader.getId());
        reservation.setBookId(bookId);
        reservation.setDeptId(book.getDeptId());
        reservation.setStatus("0");
        reservation.setCreateBy(SecurityUtils.getUsername());
        reservation.setCreateTime(DateUtils.getNowDate());
        return reservationMapper.insertReservation(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelReservation(Long id) {
        BookReader reader = currentReader();
        if (reader == null) {
            throw new ServiceException("取消失败：当前账号没有读者档案");
        }
        int rows = reservationMapper.cancelReservation(id, reader.getId(), SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("取消失败：预约不存在或已处理");
        }
        return rows;
    }

    @Override
    @DataScope(deptAlias = "r")
    public List<BookReservation> selectReservationList(BookReservation reservation) {
        return reservationMapper.selectReservationList(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleReservation(Long id) {
        BookReservation reservation = reservationMapper.selectReservationById(id);
        if (reservation == null) {
            throw new ServiceException("核销失败：预约不存在");
        }
        checkDeptPermission(reservation);
        int rows = reservationMapper.handleReservation(id, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("核销失败：该预约已处理，请刷新后重试");
        }
        noticeService.notifyReader(reservation.getReaderId(),
                "预约到书：《" + reservation.getBookName() + "》",
                "你预约的图书已到馆，请到馆办理借阅。",
                "reserve", reservation.getId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelReservationByAdmin(Long id) {
        BookReservation reservation = reservationMapper.selectReservationById(id);
        if (reservation == null) {
            throw new ServiceException("取消失败：预约不存在");
        }
        checkDeptPermission(reservation);
        int rows = reservationMapper.cancelReservationById(id, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("取消失败：该预约已处理，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int expireReservations() {
        return reservationMapper.expireReservations(7);
    }

    private void checkDeptPermission(BookReservation reservation) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user.isAdmin()) {
            return;
        }
        if (reservation.getDeptId() == null || !reservation.getDeptId().equals(user.getDeptId())) {
            throw new ServiceException("无权处理其他分馆的预约");
        }
    }

    private BookReader currentReader() {
        return bookReaderMapper.selectBookReaderByUserId(SecurityUtils.getLoginUser().getUser().getUserId());
    }
}
