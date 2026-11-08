package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BookBorrow;
import com.ruoyi.system.service.IBookBorrowService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/book/borrow")
public class BookBorrowController extends BaseController
{
    @Autowired
    private IBookBorrowService bookBorrowService;

    @PreAuthorize("@ss.hasPermi('book:borrow:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookBorrow bookBorrow)
    {
        startPage();
        // 读者只能看自己的记录、管理员/图书管理员看全部，统一在 Service 层按角色处理
        List<BookBorrow> list = bookBorrowService.selectBookBorrowList(bookBorrow);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:export')")
    @Log(title = "借阅记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BookBorrow bookBorrow)
    {
        List<BookBorrow> list = bookBorrowService.selectBookBorrowList(bookBorrow);
        ExcelUtil<BookBorrow> util = new ExcelUtil<BookBorrow>(BookBorrow.class);
        util.exportExcel(response, list, "借阅记录数据");
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bookBorrowService.selectBookBorrowById(id));
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:add')")
    @Log(title = "借阅记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BookBorrow bookBorrow)
    {
        return toAjax(bookBorrowService.insertBookBorrow(bookBorrow));
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:edit')")
    @Log(title = "借阅记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BookBorrow bookBorrow)
    {
        return toAjax(bookBorrowService.updateBookBorrow(bookBorrow));
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:remove')")
    @Log(title = "借阅记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bookBorrowService.deleteBookBorrowByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:edit')")
    @Log(title = "跨馆代还", businessType = BusinessType.UPDATE)
    @PostMapping("/crossReturn")
    public AjaxResult crossReturn(@RequestBody Map<String, String> params) {
        String msg = bookBorrowService.crossReturnBook(params.get("cardNo"), params.get("isbn"));
        return success(msg);
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:add')")
    @Log(title = "扫码借阅", businessType = BusinessType.INSERT)
    @PostMapping("/scan")
    public AjaxResult scanBorrow(@RequestBody Map<String, String> params) {
        String msg = bookBorrowService.scanBorrow(params.get("cardNo"), params.get("isbn"));
        return success(msg);
    }

    @PreAuthorize("@ss.hasPermi('book:borrow:edit')")
    @GetMapping("/crossReturn/list")
    public TableDataInfo crossReturnList(BookBorrow bookBorrow) {
        startPage();
        List<BookBorrow> list = bookBorrowService.selectCrossReturnList(bookBorrow.getCardNo(), bookBorrow.getBookName());
        return getDataTable(list);
    }

    /**
     * 原子修改借阅状态（前端必须传id、oldStatus原始状态、status新状态）
     */
    @PreAuthorize("@ss.hasPermi('book:borrow:edit')")
    @Log(title = "修改借阅状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        String oldStatus = params.get("oldStatus");
        String newStatus = params.get("status");
        bookBorrowService.updateBorrowStatus(id, oldStatus, newStatus, SecurityUtils.getUsername());
        return success();
    }

    /**
     * 读者自助续借（仅本人记录可续借，Service 内校验归属和状态）
     */
    @Log(title = "图书续借", businessType = BusinessType.UPDATE)
    @PostMapping("/renew/{id}")
    public AjaxResult renew(@PathVariable Long id) {
        java.util.Date newDueTime = bookBorrowService.renewBook(id);
        AjaxResult ajax = success("续借成功");
        ajax.put("dueTime", newDueTime);
        return ajax;
    }
}
