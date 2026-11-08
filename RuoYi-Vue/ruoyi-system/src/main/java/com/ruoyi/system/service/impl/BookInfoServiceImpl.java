package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.domain.BookTransferRecord;
import com.ruoyi.system.mapper.BookTransferRecordMapper;
import com.ruoyi.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.mapper.BookInfoMapper;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.domain.BookInfo;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.service.IBookInfoService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class BookInfoServiceImpl implements IBookInfoService
{
    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookTransferRecordMapper bookTransferRecordMapper;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private BookReaderMapper bookReaderMapper;

    /**
     * 批量划拨分馆
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignDept(Long[] ids, Long deptId) {
        System.out.println("====== 开始执行调拨，目标部门ID: " + deptId + " ======");
        SysDept targetDept = deptService.selectDeptById(deptId);
        String targetDeptName = targetDept != null ? targetDept.getDeptName() : "未知分馆";
        String operator = SecurityUtils.getUsername();
        int count = 0;

        for (Long bookId : ids) {
            try {
                BookInfo book = bookInfoMapper.selectBookInfoById(bookId);
                if (book == null) {
                    System.out.println("❌ 图书不存在，ID: " + bookId);
                    continue;
                }
                System.out.println("👉 正在处理图书: " + book.getBookName() + " (原部门ID: " + book.getDeptId() + ")");

                // 如果是在同一个部门内划拨，直接跳过，不生成日志
                if (deptId.equals(book.getDeptId())) {
                    System.out.println("⏭️ 目标部门与原部门相同，跳过");
                    continue;
                }

                Long sourceId = book.getDeptId() != null ? book.getDeptId() : 0L;
                String sourceDeptName = "未分配分馆";

                if (sourceId > 0) {
                    SysDept sourceDept = deptService.selectDeptById(sourceId);
                    if (sourceDept != null) {
                        sourceDeptName = sourceDept.getDeptName();
                    }
                }

                BookTransferRecord record = new BookTransferRecord();
                record.setBookId(bookId);
                record.setBookName(book.getBookName());
                record.setSourceDeptId(sourceId);
                record.setSourceDeptName(sourceDeptName);
                record.setTargetDeptId(deptId);
                record.setTargetDeptName(targetDeptName);
                record.setOperator(operator);

                System.out.println("📝 准备插入调拨日志...");
                bookTransferRecordMapper.insertBookTransferRecord(record);
                count++;
                System.out.println("✅ 日志插入成功");

            } catch (Exception e) {
                System.err.println("💥 划拨过程中发生致命错误，图书ID: " + bookId);
                e.printStackTrace(); // 把真正的报错打印到控制台
                throw new ServiceException("划拨失败：" + e.getMessage()); // 抛出异常触发回滚
            }
        }

        System.out.println("====== 循环结束，准备更新图书部门表，共处理 " + count + " 本 ======");
        if (count > 0) {
            return bookInfoMapper.assignDept(ids, deptId);
        } else {
            // ★ 新增：如果一本书都没处理，说明全都在目标分馆
            throw new ServiceException("划拨失败：所选图书已经属于目标分馆，无需重复划拨");
        }
    }

    @Override
    public BookInfo selectBookInfoById(Long id)
    {
        BookInfo book = bookInfoMapper.selectBookInfoById(id);
        if (book != null) checkDeptPermission(book.getDeptId());
        return book;
    }

    @Override
    @DataScope(deptAlias = "i")
    public List<BookInfo> selectBookInfoList(BookInfo bookInfo)
    {
        // 读者只能检索本人所属分馆的馆藏，避免预约时提示“不属于你所在分馆”
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!user.isAdmin() && user.getRoles() != null
                && user.getRoles().stream().anyMatch(r -> "book_reader".equals(r.getRoleKey()))) {
            BookReader reader = bookReaderMapper.selectBookReaderByUserId(user.getUserId());
            if (reader != null && reader.getDeptId() != null) {
                bookInfo.setDeptId(reader.getDeptId());
            }
        }
        return bookInfoMapper.selectBookInfoList(bookInfo);
    }

    @Override
    public int insertBookInfo(BookInfo bookInfo)
    {
        if (bookInfo.getAvailableStock() == null && bookInfo.getTotalStock() != null) {
            bookInfo.setAvailableStock(bookInfo.getTotalStock());
        }
        bookInfo.setCreateBy(SecurityUtils.getUsername());
        bookInfo.setCreateTime(DateUtils.getNowDate());
        // 后台新增图书默认归属当前操作人所在分馆；超级管理员可显式指定分馆
        if (bookInfo.getDeptId() == null) {
            if (SecurityUtils.getLoginUser().getUser().isAdmin()) {
                throw new ServiceException("新增失败：超级管理员不属于具体分馆，请先选择所属分馆");
            }
            bookInfo.setDeptId(SecurityUtils.getLoginUser().getUser().getDeptId());
        }

        // 校验 ISBN 唯一性
        if (bookInfoMapper.checkIsbnUnique(bookInfo.getIsbn(), bookInfo.getDeptId(), null) > 0) {
            throw new ServiceException("新增失败：该部门下 ISBN 已存在");
        }

        return bookInfoMapper.insertBookInfo(bookInfo);
    }

    @Override
    public int updateBookInfo(BookInfo bookInfo)
    {
        BookInfo oldBookInfo = bookInfoMapper.selectBookInfoById(bookInfo.getId());
        if (oldBookInfo == null) throw new ServiceException("修改失败：图书不存在");
        checkDeptPermission(oldBookInfo.getDeptId());

        long oldTotal = oldBookInfo.getTotalStock() == null ? 0L : oldBookInfo.getTotalStock();
        long oldAvailable = oldBookInfo.getAvailableStock() == null ? 0L : oldBookInfo.getAvailableStock();
        long borrowedNum = oldTotal - oldAvailable;

        long newTotal = bookInfo.getTotalStock() == null ? 0L : bookInfo.getTotalStock();
        if (newTotal < borrowedNum) {
            throw new ServiceException("修改失败：总库存不能小于当前已借出的数量 (" + borrowedNum + "本)");
        }
        bookInfo.setAvailableStock(newTotal - borrowedNum);

        // 校验 ISBN 唯一性
        if (bookInfo.getIsbn() != null && bookInfoMapper.checkIsbnUnique(bookInfo.getIsbn(), oldBookInfo.getDeptId(), bookInfo.getId()) > 0) {
            throw new ServiceException("修改失败：该部门下 ISBN 已存在");
        }

        bookInfo.setUpdateBy(SecurityUtils.getUsername());
        bookInfo.setUpdateTime(DateUtils.getNowDate());
        return bookInfoMapper.updateBookInfo(bookInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBookInfoByIds(Long[] ids)
    {
        for (Long id : ids) {
            deleteBookInfoById(id);
        }
        return 1;
    }

    @Override
    public int deleteBookInfoById(Long id)
    {
        BookInfo book = bookInfoMapper.selectBookInfoById(id);
        if (book == null) return 0;
        checkDeptPermission(book.getDeptId());

        // 删除前校验是否有在借记录
        if (bookInfoMapper.checkBookHasActiveBorrow(id) > 0) {
            throw new ServiceException("删除失败：该图书尚有未归还的借阅记录，无法删除");
        }
        return bookInfoMapper.deleteBookInfoById(id);
    }

    /**
     * 校验部门权限
     */
    private void checkDeptPermission(Long targetDeptId) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user.isAdmin()) return;
        if (targetDeptId != null && !targetDeptId.equals(user.getDeptId())) {
            throw new ServiceException("无权访问或操作其他部门的图书数据");
        }
    }
}