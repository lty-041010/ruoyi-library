package com.ruoyi.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.domain.BookWish;
import com.ruoyi.system.mapper.BookReaderMapper;
import com.ruoyi.system.mapper.BookWishMapper;
import com.ruoyi.system.service.IBookWishService;
import com.ruoyi.system.service.IBookNoticeService;

@Service
public class BookWishServiceImpl implements IBookWishService
{
    @Autowired
    private BookWishMapper wishMapper;

    @Autowired
    private BookReaderMapper readerMapper;

    @Autowired
    private IBookNoticeService noticeService;

    @Override
    @DataScope(deptAlias = "w")
    public List<BookWish> selectWishList(BookWish wish)
    {
        return wishMapper.selectWishList(wish);
    }

    @Override
    public List<BookWish> selectMyWishes()
    {
        BookReader reader = currentReader();
        if (reader == null) {
            return java.util.Collections.emptyList();
        }
        return wishMapper.selectMyWishes(reader.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitWish(BookWish wish)
    {
        BookReader reader = currentReader();
        if (reader == null) {
            throw new ServiceException("留言失败：当前账号没有读者档案");
        }
        if (StringUtils.isEmpty(wish.getTitle()) || wish.getTitle().trim().isEmpty()) {
            throw new ServiceException("留言失败：请填写想读的书名");
        }
        if (wish.getTitle().length() > 200) {
            throw new ServiceException("留言失败：书名不能超过 200 字");
        }
        String title = wish.getTitle().trim();
        if (wishMapper.countActiveWish(reader.getId(), title) > 0) {
            throw new ServiceException("你已经提交过这本书的留言，馆员正在处理中");
        }
        wish.setReaderId(reader.getId());
        wish.setDeptId(reader.getDeptId());
        wish.setTitle(title);
        wish.setCreateBy(SecurityUtils.getUsername());
        return wishMapper.insertWish(wish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveWish(Long id, String reply)
    {
        BookWish wish = getWishOrThrow(id);
        checkDeptPermission(wish);
        int rows = wishMapper.approveWish(id, reply, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("处理失败：该留言已处理，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectWish(Long id, String reply)
    {
        if (StringUtils.isEmpty(reply)) {
            throw new ServiceException("驳回时请填写原因，方便读者了解");
        }
        BookWish wish = getWishOrThrow(id);
        checkDeptPermission(wish);
        int rows = wishMapper.rejectWish(id, reply, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("处理失败：该留言已处理，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int onShelfWish(Long id, Long bookId)
    {
        BookWish wish = getWishOrThrow(id);
        checkDeptPermission(wish);
        int rows = wishMapper.onShelfWish(id, bookId, SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("操作失败：只有已采纳的留言才能标记上架");
        }
        noticeService.notifyReader(wish.getReaderId(),
                "想读的书已上架：《" + wish.getTitle() + "》",
                "图书馆已采购上架，可到读者门户查看并借阅。",
                "wish", wish.getId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelWish(Long id)
    {
        BookReader reader = currentReader();
        if (reader == null) {
            throw new ServiceException("取消失败：当前账号没有读者档案");
        }
        int rows = wishMapper.cancelWish(id, reader.getId(), SecurityUtils.getUsername());
        if (rows <= 0) {
            throw new ServiceException("取消失败：只有待审核的留言可以取消");
        }
        return rows;
    }

    private BookWish getWishOrThrow(Long id) {
        BookWish wish = wishMapper.selectWishById(id);
        if (wish == null) {
            throw new ServiceException("留言不存在");
        }
        return wish;
    }

    private void checkDeptPermission(BookWish wish) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user.isAdmin()) {
            return;
        }
        if (wish.getDeptId() == null || !wish.getDeptId().equals(user.getDeptId())) {
            throw new ServiceException("无权处理其他分馆的留言");
        }
    }

    private BookReader currentReader() {
        return readerMapper.selectBookReaderByUserId(SecurityUtils.getLoginUser().getUserId());
    }
}
