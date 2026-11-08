package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookTransferRecord;

public interface BookTransferRecordMapper
{
    List<BookTransferRecord> selectBookTransferRecordList(BookTransferRecord record);

    int insertBookTransferRecord(BookTransferRecord record);

    /**
     * 批量删除调拨记录（物理删除）
     */
    int deleteBookTransferRecordByIds(Long[] ids);
}