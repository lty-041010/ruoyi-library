package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookWish;

/**
 * 读者购书留言Service接口
 */
public interface IBookWishService
{
    List<BookWish> selectWishList(BookWish wish);

    List<BookWish> selectMyWishes();

    int submitWish(BookWish wish);

    int approveWish(Long id, String reply);

    int rejectWish(Long id, String reply);

    int onShelfWish(Long id, Long bookId);

    int cancelWish(Long id);
}
