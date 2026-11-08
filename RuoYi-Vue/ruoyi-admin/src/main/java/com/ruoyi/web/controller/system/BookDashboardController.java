package com.ruoyi.web.controller.system;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.DashboardMapper;
import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.mapper.BookReaderMapper;

@RestController
@RequestMapping("/book/dashboard")
public class BookDashboardController extends BaseController
{
    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private BookReaderMapper bookReaderMapper;

    /**
     * 获取数据看板统计信息
     */
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        Map<String, Object> data = new HashMap<>();

        SysUser user = SecurityUtils.getLoginUser().getUser();
        Long queryDeptId = user.isAdmin() ? null : user.getDeptId();

        // 读者身份：在借/逾期按本人统计，避免首页显示整个分馆的数字
        boolean isReader = !user.isAdmin() && user.getRoles().stream()
                .anyMatch(role -> "book_reader".equals(role.getRoleKey()));
        BookReader currentReader = isReader ? bookReaderMapper.selectBookReaderByUserId(user.getUserId()) : null;

        // 原有的：书目种类数
        data.put("totalBooks", dashboardMapper.countTotalBooks(queryDeptId));

        // ★ 新增：总册数 和 可借总册数
        data.put("totalCopies", dashboardMapper.sumTotalStock(queryDeptId));
        data.put("availableCopies", dashboardMapper.sumAvailableStock(queryDeptId));

        data.put("totalReaders", dashboardMapper.countTotalReaders(queryDeptId));
        if (currentReader != null) {
            data.put("activeBorrows", dashboardMapper.countReaderActiveBorrows(currentReader.getId()));
            data.put("overdueBorrows", dashboardMapper.countReaderOverdueBorrows(currentReader.getId()));
            data.put("myCardNo", currentReader.getCardNo());
            data.put("myMaxBorrowNum", currentReader.getMaxBorrowNum());
            data.put("myReaderName", currentReader.getRealName());
        } else {
            data.put("activeBorrows", dashboardMapper.countActiveBorrows(queryDeptId));
            data.put("overdueBorrows", dashboardMapper.countOverdueBorrows(queryDeptId));
        }
        data.put("isReader", currentReader != null);
        data.put("categoryStats", dashboardMapper.getCategoryStats());
        data.put("deptStats", dashboardMapper.getDeptStats());

        return success(data);
    }
}