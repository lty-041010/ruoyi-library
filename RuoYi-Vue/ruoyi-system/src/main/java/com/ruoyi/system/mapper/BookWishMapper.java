package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.BookWish;

/**
 * 读者购书留言Mapper接口
 */
public interface BookWishMapper
{
    List<BookWish> selectWishList(BookWish wish);

    List<BookWish> selectMyWishes(@Param("readerId") Long readerId);

    BookWish selectWishById(Long id);

    int countActiveWish(@Param("readerId") Long readerId, @Param("title") String title);

    int insertWish(BookWish wish);

    int approveWish(@Param("id") Long id, @Param("reply") String reply, @Param("updateBy") String updateBy);

    int rejectWish(@Param("id") Long id, @Param("reply") String reply, @Param("updateBy") String updateBy);

    int onShelfWish(@Param("id") Long id, @Param("bookId") Long bookId, @Param("updateBy") String updateBy);

    int cancelWish(@Param("id") Long id, @Param("readerId") Long readerId, @Param("updateBy") String updateBy);
}
