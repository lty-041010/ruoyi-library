package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.mapper.BookCategoryMapper;
import com.ruoyi.system.domain.BookCategory;
import com.ruoyi.system.service.IBookCategoryService;

@Service
public class BookCategoryServiceImpl implements IBookCategoryService
{
    @Autowired
    private BookCategoryMapper bookCategoryMapper;

    @Override
    public BookCategory selectBookCategoryById(Long id)
    {
        return bookCategoryMapper.selectBookCategoryById(id);
    }

    @Override
    // ★ 去掉了 @DataScope(deptAlias = "c")
    public List<BookCategory> selectBookCategoryList(BookCategory bookCategory) {
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        boolean isManager = currentUser.isAdmin() ||
                currentUser.getRoles().stream().anyMatch(r -> "book_manager".equals(r.getRoleKey()));

        // 如果不是管理员，只能看到正常状态的分类
        if (!isManager) {
            bookCategory.setStatus("0");
        }
        return bookCategoryMapper.selectBookCategoryList(bookCategory);
    }

    @Override
    public int insertBookCategory(BookCategory bookCategory)
    {
        checkCategoryPermission(); // ★ 校验维护权限
        bookCategory.setCreateBy(SecurityUtils.getUsername());
        bookCategory.setCreateTime(DateUtils.getNowDate());
        bookCategory.setDeptId(0L); // ★ 全局分类，部门统一设为 0

        if (bookCategoryMapper.checkCategoryNameUnique(bookCategory.getCategoryName(), bookCategory.getParentId(), null) > 0) {
            throw new ServiceException("新增失败：同级下分类名称已存在");
        }

        return bookCategoryMapper.insertBookCategory(bookCategory);
    }

    @Override
    public int updateBookCategory(BookCategory bookCategory)
    {
        checkCategoryPermission(); // ★ 校验维护权限

        BookCategory old = bookCategoryMapper.selectBookCategoryById(bookCategory.getId());
        if (old == null) throw new ServiceException("修改失败：分类不存在");

        if (bookCategory.getCategoryName() != null && bookCategoryMapper.checkCategoryNameUnique(bookCategory.getCategoryName(), old.getParentId(), bookCategory.getId()) > 0) {
            throw new ServiceException("修改失败：同级下分类名称已存在");
        }

        bookCategory.setUpdateBy(SecurityUtils.getUsername());
        bookCategory.setUpdateTime(DateUtils.getNowDate());
        return bookCategoryMapper.updateBookCategory(bookCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBookCategoryByIds(Long[] ids)
    {
        checkCategoryPermission(); // ★ 校验维护权限
        for (Long id : ids) {
            deleteBookCategoryById(id);
        }
        return 1;
    }

    @Override
    public int deleteBookCategoryById(Long id)
    {
        checkCategoryPermission(); // ★ 校验维护权限

        BookCategory category = bookCategoryMapper.selectBookCategoryById(id);
        if (category == null) return 0;

        if (bookCategoryMapper.hasChildByCategoryId(id) > 0) {
            throw new ServiceException("删除失败：存在子分类，请先删除子分类");
        }
        if (bookCategoryMapper.checkCategoryExistBook(id) > 0) {
            throw new ServiceException("删除失败：该分类下存在图书，无法删除");
        }

        return bookCategoryMapper.deleteBookCategoryById(id);
    }

    /**
     * ★ 新增：校验全局分类维护权限
     */
    private void checkCategoryPermission() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user.isAdmin()) return;
        boolean isManager = user.getRoles().stream().anyMatch(r -> "book_manager".equals(r.getRoleKey()));
        if (!isManager) {
            throw new ServiceException("权限不足：只有总馆管理员才能维护全局图书分类");
        }
    }
}