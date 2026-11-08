package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.system.domain.BookBorrow;
import com.ruoyi.system.domain.BookInfo;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.mapper.BookBorrowMapper;
import com.ruoyi.system.mapper.BookInfoMapper;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.mapper.BookReservationMapper;
import com.ruoyi.system.service.IBookBorrowService;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.system.domain.BookTransferRecord;
import com.ruoyi.system.mapper.BookTransferRecordMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.common.core.domain.entity.SysDept;

@Service
public class BookBorrowServiceImpl implements IBookBorrowService
{
    private static final String STATUS_BORROWING = "0";
    private static final String STATUS_RETURNED = "1";
    private static final String STATUS_OVERDUE = "2";
    private static final String BOOK_STATUS_ON_SHELF = "0";

    @Autowired
    private BookTransferRecordMapper bookTransferRecordMapper;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private BookBorrowMapper bookBorrowMapper;
    @Autowired
    private BookInfoMapper bookInfoMapper;
    @Autowired
    private BookReaderMapper bookReaderMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private BookReservationMapper bookReservationMapper;

    @Override
    public List<BookBorrow> selectCrossReturnList(String cardNo, String bookName) {
        Long currentDeptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        return bookBorrowMapper.selectCrossReturnList(cardNo, bookName, currentDeptId);
    }

    @Override
    public BookBorrow selectBookBorrowById(Long id)
    {
        BookBorrow borrow = bookBorrowMapper.selectBookBorrowById(id);
        if (borrow == null) return null;
        checkBorrowPermission(borrow);
        checkOverdue(borrow);
        return borrow;
    }

    @Override
    @DataScope(deptAlias = "b")
    public List<BookBorrow> selectBookBorrowList(BookBorrow bookBorrow)
    {
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        if (!currentUser.isAdmin()) {
            boolean isManager = currentUser.getRoles().stream()
                    .anyMatch(role -> "book_manager".equals(role.getRoleKey()));
            if (!isManager) {
                BookReader currentReader = bookReaderMapper.selectBookReaderByUserId(currentUser.getUserId());
                if (currentReader != null) {
                    bookBorrow.setReaderId(currentReader.getId());
                } else {
                    return new ArrayList<>();
                }
            }
        }

        List<BookBorrow> list = bookBorrowMapper.selectBookBorrowList(bookBorrow);
        if (list != null && !list.isEmpty()) {
            for (BookBorrow borrow : list) {
                checkOverdue(borrow);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBookBorrow(BookBorrow bookBorrow)
    {
        // ============ 新增：时间校验 start ============
        Date borrowTime = bookBorrow.getBorrowTime();
        Date dueTime = bookBorrow.getDueTime();
        if(borrowTime != null && dueTime != null){
            // dueTime <= borrowTime 直接抛异常
            if (dueTime.before(borrowTime) || dueTime.equals(borrowTime)) {
                throw new ServiceException("应还时间必须晚于借出时间！");
            }
        }
        BookInfo bookInfo = bookInfoMapper.selectBookInfoById(bookBorrow.getBookId());
        if (bookInfo == null) {
            throw new ServiceException("借阅失败：图书不存在");
        }
        if (!BOOK_STATUS_ON_SHELF.equals(bookInfo.getStatus())) {
            throw new ServiceException("借阅失败：该图书已下架，无法借阅");
        }

        BookReader reader = bookReaderMapper.selectBookReaderById(bookBorrow.getReaderId());
        if (reader == null) {
            throw new ServiceException("借阅失败：读者不存在");
        }

        String maxBorrowStr = configService.selectConfigByKey("book.default.max.borrow.num");
        long globalMaxNum = StringUtils.isNotEmpty(maxBorrowStr) ? Long.parseLong(maxBorrowStr) : 5L;
        long readerMaxNum = reader.getMaxBorrowNum() == null ? globalMaxNum : reader.getMaxBorrowNum();

        long currentNum = reader.getCurrentBorrowNum() == null ? 0L : reader.getCurrentBorrowNum();
        if (currentNum >= readerMaxNum) {
            throw new ServiceException("借阅失败：该读者已达到最大可借数量 (" + readerMaxNum + " 本)");
        }

        Date now = DateUtils.getNowDate();
        // 保留前端选择的借出时间；未选择时才用当前时间
        if (bookBorrow.getBorrowTime() == null) {
            bookBorrow.setBorrowTime(now);
        }
        bookBorrow.setStatus(STATUS_BORROWING);
        bookBorrow.setCreateTime(now);
        bookBorrow.setCreateBy(SecurityUtils.getUsername());
        bookBorrow.setDeptId(bookInfo.getDeptId() != null ? bookInfo.getDeptId() : SecurityUtils.getLoginUser().getUser().getDeptId());

        // 应还时间未填、或早于/等于借出时间时，按配置的借期自动计算
        if (bookBorrow.getDueTime() == null || !bookBorrow.getDueTime().after(bookBorrow.getBorrowTime())) {
            String daysStr = configService.selectConfigByKey("book.default.borrow.days");
            int borrowDays = StringUtils.isNotEmpty(daysStr) ? Integer.parseInt(daysStr) : 30;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bookBorrow.getBorrowTime());
            calendar.add(Calendar.DAY_OF_MONTH, borrowDays);
            bookBorrow.setDueTime(calendar.getTime());
        }

        if (bookInfoMapper.decreaseAvailableStock(bookBorrow.getBookId()) == 0) {
            throw new ServiceException("借阅失败：该书库存不足");
        }
        if (bookReaderMapper.increaseCurrentBorrowNum(bookBorrow.getReaderId()) == 0) {
            throw new ServiceException("借阅失败：该读者已达到最大可借数量或账号被停用");
        }

        try {
            return bookBorrowMapper.insertBookBorrow(bookBorrow);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("借阅失败：该读者已借阅此书且尚未归还，请勿重复借阅。");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBookBorrow(BookBorrow bookBorrow)
    {
        BookBorrow oldBorrow = bookBorrowMapper.selectBookBorrowById(bookBorrow.getId());
        if (oldBorrow == null) {
            throw new ServiceException("借阅记录不存在");
        }
        checkBorrowPermission(oldBorrow);
        // ============ 新增：时间校验 start ============
        Date borrowTime = bookBorrow.getBorrowTime();
        Date dueTime = bookBorrow.getDueTime();
        if(borrowTime != null && dueTime != null){
            if (dueTime.before(borrowTime) || dueTime.equals(borrowTime)) {
                throw new ServiceException("应还时间必须晚于借出时间！");
            }
        }

        bookBorrow.setUpdateTime(DateUtils.getNowDate());
        bookBorrow.setUpdateBy(SecurityUtils.getUsername());

        // 借阅记录登记后不允许再改读者/图书，否则库存和借阅数会对不上
        bookBorrow.setReaderId(oldBorrow.getReaderId());
        bookBorrow.setBookId(oldBorrow.getBookId());

        boolean wasUnreturned = STATUS_BORROWING.equals(oldBorrow.getStatus()) || STATUS_OVERDUE.equals(oldBorrow.getStatus());
        boolean isReturnAction = STATUS_RETURNED.equals(bookBorrow.getStatus()) && wasUnreturned;

        if (isReturnAction) {
            bookBorrow.setReturnTime(DateUtils.getNowDate());
            bookInfoMapper.increaseAvailableStock(oldBorrow.getBookId());
            bookReaderMapper.decreaseCurrentBorrowNum(oldBorrow.getReaderId());
        } else if (!wasUnreturned && !STATUS_RETURNED.equals(bookBorrow.getStatus())) {
            // 已归还的记录只允许改备注/时间，不允许再改回「借阅中」
            throw new ServiceException("该记录已归还，不能改回借阅中状态");
        } else if (STATUS_OVERDUE.equals(oldBorrow.getStatus())
                && bookBorrow.getDueTime() != null
                && bookBorrow.getDueTime().after(DateUtils.getNowDate())) {
            bookBorrow.setStatus(STATUS_BORROWING);
        }
        return bookBorrowMapper.updateBookBorrow(bookBorrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBookBorrowByIds(Long[] ids)
    {
        int count = 0;
        for (Long id : ids) {
            BookBorrow borrow = bookBorrowMapper.selectBookBorrowById(id);
            if (borrow == null) continue;
            checkBorrowPermission(borrow);
            if (STATUS_BORROWING.equals(borrow.getStatus()) || STATUS_OVERDUE.equals(borrow.getStatus())) {
                throw new ServiceException("删除失败：记录ID " + id + " 尚未归还，请先归还再删除。");
            }
            count += bookBorrowMapper.deleteBookBorrowById(id);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBookBorrowById(Long id)
    {
        BookBorrow borrow = bookBorrowMapper.selectBookBorrowById(id);
        if (borrow == null) return 0;
        checkBorrowPermission(borrow);
        if (STATUS_BORROWING.equals(borrow.getStatus()) || STATUS_OVERDUE.equals(borrow.getStatus())) {
            throw new ServiceException("删除失败：该记录尚未归还，请先归还再删除。");
        }
        return bookBorrowMapper.deleteBookBorrowById(id);
    }

    private void checkBorrowPermission(BookBorrow borrow) {
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        if (!currentUser.isAdmin()) {
            boolean isManager = currentUser.getRoles().stream().anyMatch(r -> "book_manager".equals(r.getRoleKey()));
            if (!isManager) {
                BookReader currentReader = bookReaderMapper.selectBookReaderByUserId(currentUser.getUserId());
                if (currentReader == null || !borrow.getReaderId().equals(currentReader.getId())) {
                    throw new ServiceException("无权操作他人的借阅记录");
                }
            }
        }
    }

    /**
     * 内存层面标记逾期，仅用于页面展示兜底，不更新数据库
     * 数据库逾期状态由Quartz定时任务 updateOverdueStatus 统一批量更新
     */
    private void checkOverdue(BookBorrow borrow) {
        if (borrow == null) return;
        Date now = DateUtils.getNowDate();
        if (STATUS_BORROWING.equals(borrow.getStatus())
                && borrow.getDueTime() != null
                && borrow.getDueTime().before(now)) {
            borrow.setStatus(STATUS_OVERDUE);
        }
        if (STATUS_OVERDUE.equals(borrow.getStatus()) && borrow.getDueTime() != null) {
            long diff = now.getTime() - borrow.getDueTime().getTime();
            int days = (int) Math.max(1, diff / (1000L * 60 * 60 * 24));
            borrow.setOverdueDays(days);
            String rateStr = configService.selectConfigByKey("book.fine.per.day");
            BigDecimal rate = new BigDecimal(StringUtils.isNotEmpty(rateStr) ? rateStr : "0.5");
            borrow.setFineAmount(rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP));
        } else {
            borrow.setOverdueDays(0);
            borrow.setFineAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String scanBorrow(String cardNo, String isbn) {
        if (StringUtils.isEmpty(cardNo) || StringUtils.isEmpty(isbn)) {
            throw new ServiceException("扫码借阅需要同时提供借书证号和图书 ISBN");
        }
        BookReader reader = bookReaderMapper.selectBookReaderByCardNo(cardNo.trim());
        if (reader == null) {
            throw new ServiceException("未找到借书证号为 " + cardNo + " 的读者");
        }
        BookInfo book = bookInfoMapper.selectBookInfoByIsbn(isbn.trim());
        if (book == null) {
            throw new ServiceException("未找到 ISBN 为 " + isbn + " 的在架图书");
        }
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!user.isAdmin() && book.getDeptId() != null && !book.getDeptId().equals(user.getDeptId())) {
            throw new ServiceException("该图书属于其他分馆，请到所属分馆办理借阅");
        }
        BookBorrow borrow = new BookBorrow();
        borrow.setReaderId(reader.getId());
        borrow.setBookId(book.getId());
        insertBookBorrow(borrow);
        return "借阅成功：《" + book.getBookName() + "》 → " + reader.getRealName()
                + (reader.getCardNo() != null ? "（" + reader.getCardNo() + "）" : "");
    }

    @Override
    public int updateOverdueStatus() {
        return bookBorrowMapper.updateOverdueStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String crossReturnBook(String cardNo, String isbn) {
        if (StringUtils.isEmpty(cardNo) && StringUtils.isEmpty(isbn)) {
            throw new ServiceException("请至少输入借书卡号或图书ISBN");
        }
        BookBorrow borrow = bookBorrowMapper.selectActiveBorrowForCrossReturn(cardNo, isbn);
        if (borrow == null) {
            throw new ServiceException("未找到该读者或该图书的在借记录，请检查输入是否正确");
        }

        Long currentDeptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        BookInfo originalBook = bookInfoMapper.selectBookInfoById(borrow.getBookId());
        if (originalBook == null) {
            throw new ServiceException("图书信息不存在，无法办理代还");
        }

        // 超级管理员/总公司账号不隶属具体分馆，直接按普通还书处理，避免把图书调拨到公司节点
        SysDept currentDept = currentDeptId == null ? null : deptService.selectDeptById(currentDeptId);
        if (currentDept == null || currentDept.getParentId() == null || currentDept.getParentId() == 0L) {
            BookBorrow update = new BookBorrow();
            update.setId(borrow.getId());
            update.setStatus(STATUS_RETURNED);
            updateBookBorrow(update);
            return "还书成功（当前账号不隶属具体分馆，未执行图书调拨）";
        }

        if (currentDeptId.equals(originalBook.getDeptId())) {
            BookBorrow update = new BookBorrow();
            update.setId(borrow.getId());
            update.setStatus(STATUS_RETURNED);
            updateBookBorrow(update);
            return "同馆还书成功";
        }

        BookBorrow update = new BookBorrow();
        update.setId(borrow.getId());
        update.setStatus(STATUS_RETURNED);
        update.setReturnTime(DateUtils.getNowDate());
        update.setUpdateTime(DateUtils.getNowDate());
        update.setUpdateBy(SecurityUtils.getUsername());
        bookBorrowMapper.updateBookBorrow(update);

        bookReaderMapper.decreaseCurrentBorrowNum(borrow.getReaderId());

        BookTransferRecord record = new BookTransferRecord();
        record.setBookId(originalBook.getId());
        record.setBookName(originalBook.getBookName());
        record.setSourceDeptId(originalBook.getDeptId());
        SysDept sourceDept = deptService.selectDeptById(originalBook.getDeptId());
        record.setSourceDeptName(sourceDept != null ? sourceDept.getDeptName() : "未知A馆");
        record.setTargetDeptId(currentDeptId);
        SysDept targetDept = deptService.selectDeptById(currentDeptId);
        record.setTargetDeptName(targetDept != null ? targetDept.getDeptName() : "未知B馆");
        record.setOperator(SecurityUtils.getUsername());
        bookTransferRecordMapper.insertBookTransferRecord(record);

        // 先记住原图书ID，下面如果新增副本会把 originalBook.id 置空
        Long sourceBookId = originalBook.getId();
        int existCount = bookInfoMapper.checkIsbnUnique(originalBook.getIsbn(), currentDeptId, null);
        if (existCount > 0) {
            bookInfoMapper.addStockByIsbnAndDept(originalBook.getIsbn(), currentDeptId);
        } else {
            originalBook.setId(null);
            originalBook.setDeptId(currentDeptId);
            originalBook.setTotalStock(1L);
            originalBook.setAvailableStock(1L);
            originalBook.setCreateBy(SecurityUtils.getUsername());
            originalBook.setCreateTime(DateUtils.getNowDate());
            originalBook.setUpdateBy(null);
            originalBook.setUpdateTime(null);
            bookInfoMapper.insertBookInfo(originalBook);
        }
        // 原馆藏总册数 -1，这里必须用原图书ID
        bookInfoMapper.decreaseTotalStock(sourceBookId);
        return "跨馆代还成功！图书已自动调拨至本馆库存。";
    }

    /**
     * 原子状态修改，where条件带上oldStatus，防止前端缓存状态与数据库不一致
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBorrowStatus(Long id, String oldStatus, String newStatus, String updateBy) {
        int rows = bookBorrowMapper.updateBorrowStatus(id, oldStatus, newStatus, updateBy);
        if (rows <= 0) {
            throw new ServiceException("当前借阅状态已发生变化，请刷新页面后重试");
        }
        return true;
    }

    /**
     * 读者自助续借：仅本人、借阅中、未逾期、续借次数未超上限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Date renewBook(Long id) {
        BookBorrow borrow = bookBorrowMapper.selectBookBorrowById(id);
        if (borrow == null) {
            throw new ServiceException("续借失败：借阅记录不存在");
        }
        checkBorrowPermission(borrow);
        if (!STATUS_BORROWING.equals(borrow.getStatus())) {
            throw new ServiceException("续借失败：只有借阅中的记录可以续借");
        }
        if (borrow.getDueTime() != null && borrow.getDueTime().before(DateUtils.getNowDate())) {
            throw new ServiceException("续借失败：该记录已逾期，请先归还");
        }
        String maxRenewStr = configService.selectConfigByKey("book.default.max.renew.num");
        int maxRenew = StringUtils.isNotEmpty(maxRenewStr) ? Integer.parseInt(maxRenewStr) : 2;
        int renewCount = borrow.getRenewCount() == null ? 0 : borrow.getRenewCount();
        if (renewCount >= maxRenew) {
            throw new ServiceException("续借失败：每本图书最多续借 " + maxRenew + " 次");
        }
        if (bookReservationMapper.countActiveReservationByBook(borrow.getBookId()) > 0) {
            throw new ServiceException("续借失败：该书已有其他读者在排队预约，请归还后再借");
        }
        String daysStr = configService.selectConfigByKey("book.default.borrow.days");
        int borrowDays = StringUtils.isNotEmpty(daysStr) ? Integer.parseInt(daysStr) : 30;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(borrow.getDueTime() == null ? DateUtils.getNowDate() : borrow.getDueTime());
        calendar.add(Calendar.DAY_OF_MONTH, borrowDays);
        Date newDueTime = calendar.getTime();

        int rows = bookBorrowMapper.updateRenewInfo(id, newDueTime, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("续借失败：记录状态已变化，请刷新后重试");
        }
        return newDueTime;
    }
}
