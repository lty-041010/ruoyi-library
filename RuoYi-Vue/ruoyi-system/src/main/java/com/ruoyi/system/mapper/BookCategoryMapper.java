package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookCategory;
import org.apache.ibatis.annotations.Param;

public interface BookCategoryMapper
{
    BookCategory selectBookCategoryById(Long id);
    List<BookCategory> selectBookCategoryList(BookCategory bookCategory);
    int insertBookCategory(BookCategory bookCategory);
    int updateBookCategory(BookCategory bookCategory);
    int deleteBookCategoryById(Long id);
    int deleteBookCategoryByIds(Long[] ids);
    int checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId, @Param("id") Long id);
    int hasChildByCategoryId(@Param("categoryId") Long categoryId);
    int checkCategoryExistBook(@Param("categoryId") Long categoryId);
}