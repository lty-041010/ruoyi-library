package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.service.IBookReaderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 读者扩展Controller
 *
 * @author LTY
 * @date 2026-11-04
 */
@RestController
@RequestMapping("/book/reader")
public class BookReaderController extends BaseController
{
    @Autowired
    private IBookReaderService bookReaderService;

    /**
     * 查询读者扩展列表
     */
    @PreAuthorize("@ss.hasPermi('book:reader:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookReader bookReader)
    {
        startPage();
        List<BookReader> list = bookReaderService.selectBookReaderList(bookReader);
        return getDataTable(list);
    }

    /**
     * 导出读者扩展列表
     */
    @PreAuthorize("@ss.hasPermi('book:reader:export')")
    @Log(title = "读者扩展", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BookReader bookReader)
    {
        List<BookReader> list = bookReaderService.selectBookReaderList(bookReader);
        ExcelUtil<BookReader> util = new ExcelUtil<BookReader>(BookReader.class);
        util.exportExcel(response, list, "读者扩展数据");
    }

    /**
     * 获取读者扩展详细信息
     */
    @PreAuthorize("@ss.hasPermi('book:reader:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bookReaderService.selectBookReaderById(id));
    }

    /**
     * 新增读者扩展
     */
    @PreAuthorize("@ss.hasPermi('book:reader:add')")
    @Log(title = "读者扩展", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BookReader bookReader)
    {
        return toAjax(bookReaderService.insertBookReader(bookReader));
    }

    /**
     * 修改读者扩展
     */
    @PreAuthorize("@ss.hasPermi('book:reader:edit')")
    @Log(title = "读者扩展", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BookReader bookReader)
    {
        return toAjax(bookReaderService.updateBookReader(bookReader));
    }

    /**
     * 删除读者扩展
     */
    @PreAuthorize("@ss.hasPermi('book:reader:remove')")
    @Log(title = "读者扩展", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bookReaderService.deleteBookReaderByIds(ids));
    }
}
