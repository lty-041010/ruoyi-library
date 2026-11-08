package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.BookTransferRecord;
import com.ruoyi.system.mapper.BookTransferRecordMapper;

@RestController
@RequestMapping("/book/transfer")
public class BookTransferRecordController extends BaseController
{
    @Autowired
    private BookTransferRecordMapper bookTransferRecordMapper;

    /**
     * 查询调拨记录列表（仅限管理员查看）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(BookTransferRecord record)
    {
        startPage();
        List<BookTransferRecord> list = bookTransferRecordMapper.selectBookTransferRecordList(record);
        return getDataTable(list);
    }

    /**
     * 导出调拨记录（仅限管理员）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "图书调拨记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BookTransferRecord record)
    {
        List<BookTransferRecord> list = bookTransferRecordMapper.selectBookTransferRecordList(record);
        ExcelUtil<BookTransferRecord> util = new ExcelUtil<BookTransferRecord>(BookTransferRecord.class);
        util.exportExcel(response, list, "图书调拨记录");
    }

    /**
     * 删除调拨记录
     */
    @PreAuthorize("@ss.hasPermi('book:transfer:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bookTransferRecordMapper.deleteBookTransferRecordByIds(ids));
    }

}