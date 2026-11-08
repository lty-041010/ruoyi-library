package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BookWish;
import com.ruoyi.system.service.IBookWishService;

/**
 * 读者购书留言
 */
@RestController
@RequestMapping("/book/wish")
public class BookWishController extends BaseController
{
    @Autowired
    private IBookWishService wishService;

    /** 我的留言 */
    @GetMapping("/my")
    public AjaxResult my()
    {
        return success(wishService.selectMyWishes());
    }

    /** 提交留言 */
    @PostMapping
    public AjaxResult add(@RequestBody BookWish wish)
    {
        wishService.submitWish(wish);
        return success("留言已提交，馆员审核后会回复你");
    }

    /** 读者取消留言 */
    @DeleteMapping("/{id}")
    public AjaxResult cancel(@PathVariable Long id)
    {
        wishService.cancelWish(id);
        return success("已取消留言");
    }

    /** 后台留言列表 */
    @PreAuthorize("@ss.hasPermi('book:wish:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookWish wish)
    {
        startPage();
        List<BookWish> list = wishService.selectWishList(wish);
        return getDataTable(list);
    }

    /** 采纳（进入采购） */
    @PreAuthorize("@ss.hasPermi('book:wish:approve')")
    @Log(title = "购书留言采纳", businessType = BusinessType.UPDATE)
    @PutMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body)
    {
        String reply = body == null ? null : body.get("reply");
        if (StringUtils.isEmpty(reply)) {
            reply = "已采纳，图书馆将安排采购";
        }
        wishService.approveWish(id, reply);
        return success("已采纳，读者会看到采购进度");
    }

    /** 驳回 */
    @PreAuthorize("@ss.hasPermi('book:wish:reject')")
    @Log(title = "购书留言驳回", businessType = BusinessType.UPDATE)
    @PutMapping("/reject/{id}")
    public AjaxResult reject(@PathVariable Long id, @RequestBody Map<String, String> body)
    {
        wishService.rejectWish(id, body == null ? null : body.get("reply"));
        return success("已驳回并回复读者");
    }

    /** 标记已上架 */
    @PreAuthorize("@ss.hasPermi('book:wish:approve')")
    @Log(title = "购书留言上架", businessType = BusinessType.UPDATE)
    @PutMapping("/onshelf/{id}")
    public AjaxResult onShelf(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body)
    {
        Long bookId = null;
        if (body != null && body.get("bookId") != null && !"".equals(body.get("bookId"))) {
            bookId = Long.valueOf(body.get("bookId").toString());
        }
        wishService.onShelfWish(id, bookId);
        return success("已标记上架，读者端会提示可借阅");
    }
}
