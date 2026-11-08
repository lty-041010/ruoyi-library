package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.service.IBookReaderService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class BookReaderServiceImpl implements IBookReaderService
{
    @Autowired
    private BookReaderMapper bookReaderMapper;

    @Autowired
    private ISysConfigService configService;

    @Override
    public BookReader selectBookReaderByUserId(Long userId)
    {
        return bookReaderMapper.selectBookReaderByUserId(userId);
    }

    @Override
    public BookReader selectBookReaderById(Long id)
    {
        BookReader reader = bookReaderMapper.selectBookReaderById(id);
        if (reader != null) checkDeptPermission(reader.getDeptId());
        return reader;
    }

    @Override
    @DataScope(deptAlias = "r")
    public List<BookReader> selectBookReaderList(BookReader bookReader)
    {
        return bookReaderMapper.selectBookReaderList(bookReader);
    }

    @Override
    public int insertBookReader(BookReader bookReader)
    {
        bookReader.setCreateBy(SecurityUtils.getUsername());
        bookReader.setCreateTime(DateUtils.getNowDate());
        // 后台新增读者时默认归属到当前操作人所在分馆；若显式指定了分馆则保留
        if (bookReader.getDeptId() == null) {
            if (SecurityUtils.getLoginUser().getUser().isAdmin()) {
                throw new ServiceException("新增失败：超级管理员不属于具体分馆，请先选择所属分馆");
            }
            bookReader.setDeptId(SecurityUtils.getLoginUser().getUser().getDeptId());
        }

        if (bookReader.getMaxBorrowNum() == null) {
            String maxStr = configService.selectConfigByKey("book.default.max.borrow.num");
            long defaultMax = StringUtils.isNotEmpty(maxStr) ? Long.parseLong(maxStr) : 5L;
            bookReader.setMaxBorrowNum(defaultMax);
        }

        if (bookReaderMapper.checkCardNoUnique(bookReader.getCardNo(), bookReader.getDeptId(), null) > 0) {
            throw new ServiceException("新增失败：借书卡号已存在");
        }
        if (bookReader.getUserId() != null && bookReaderMapper.checkUserIdUnique(bookReader.getUserId(), null) > 0) {
            throw new ServiceException("新增失败：该若依用户已绑定读者账号");
        }
        return bookReaderMapper.insertBookReader(bookReader);
    }

    @Override
    public int updateBookReader(BookReader bookReader)
    {
        BookReader oldReader = bookReaderMapper.selectBookReaderById(bookReader.getId());
        if (oldReader == null) throw new ServiceException("修改失败：读者不存在");
        checkDeptPermission(oldReader.getDeptId());

        Long currentNum = oldReader.getCurrentBorrowNum() == null ? 0L : oldReader.getCurrentBorrowNum();
        Long newMaxNum = bookReader.getMaxBorrowNum() == null ? 0L : bookReader.getMaxBorrowNum();
        if (newMaxNum < currentNum) {
            throw new ServiceException("修改失败：该读者当前已借阅 " + currentNum + " 本书，最大可借数不能低于此数量。");
        }
        bookReader.setCurrentBorrowNum(currentNum);
        bookReader.setUpdateBy(SecurityUtils.getUsername());
        bookReader.setUpdateTime(DateUtils.getNowDate());

        if (bookReader.getCardNo() != null && bookReaderMapper.checkCardNoUnique(bookReader.getCardNo(), oldReader.getDeptId(), bookReader.getId()) > 0) {
            throw new ServiceException("修改失败：借书卡号已存在");
        }
        return bookReaderMapper.updateBookReader(bookReader);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBookReaderByIds(Long[] ids)
    {
        for (Long id : ids) {
            deleteBookReaderById(id);
        }
        return 1;
    }

    @Override
    public int deleteBookReaderById(Long id)
    {
        BookReader reader = bookReaderMapper.selectBookReaderById(id);
        if (reader == null) return 0;
        checkDeptPermission(reader.getDeptId());
        if (bookReaderMapper.checkReaderHasUnreturnedBorrow(id) > 0) {
            throw new ServiceException("删除失败：该读者尚有未归还的图书，无法删除");
        }
        return bookReaderMapper.deleteBookReaderById(id);
    }

    private void checkDeptPermission(Long targetDeptId) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user.isAdmin()) return;
        if (targetDeptId != null && !targetDeptId.equals(user.getDeptId())) {
            throw new ServiceException("无权访问或操作其他部门的读者数据");
        }
    }
}
