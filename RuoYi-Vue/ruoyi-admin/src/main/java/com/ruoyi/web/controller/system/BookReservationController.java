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
import com.ruoyi.system.domain.BookReservation;
import com.ruoyi.system.service.IBookReservationService;

/**
 * 读者图书预约（含我的预约、预约、取消），仅处理当前登录读者本人数据
 */
@RestController
@RequestMapping("/book/reservation")
public class BookReservationController extends BaseController
{
    @Autowired
    private IBookReservationService reservationService;

    @GetMapping("/my")
    public AjaxResult my() {
        List<BookReservation> list = reservationService.selectMyReservations();
        return success(list);
    }

    @Log(title = "图书预约", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> body) {
        Object bookId = body.get("bookId");
        if (bookId == null) {
            return error("请选择要预约的图书");
        }
        reservationService.reserveBook(Long.valueOf(String.valueOf(bookId)));
        return success("预约成功，图书可借后将通知你");
    }

    @Log(title = "取消预约", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult cancel(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return success("已取消预约");
    }

    /**
     * 后台预约列表（馆员按分馆数据权限）
     */
    @PreAuthorize("@ss.hasPermi('book:reservation:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookReservation reservation) {
        startPage();
        List<BookReservation> list = reservationService.selectReservationList(reservation);
        return getDataTable(list);
    }

    /**
     * 到书核销
     */
    @PreAuthorize("@ss.hasPermi('book:reservation:handle')")
    @Log(title = "预约到书核销", businessType = BusinessType.UPDATE)
    @PutMapping("/handle/{id}")
    public AjaxResult handle(@PathVariable Long id) {
        reservationService.handleReservation(id);
        return success("已核销，读者可到馆取书");
    }

    /**
     * 后台取消预约
     */
    @PreAuthorize("@ss.hasPermi('book:reservation:cancel')")
    @Log(title = "后台取消预约", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public AjaxResult cancelByAdmin(@PathVariable Long id) {
        reservationService.cancelReservationByAdmin(id);
        return success("已取消该预约");
    }
}
