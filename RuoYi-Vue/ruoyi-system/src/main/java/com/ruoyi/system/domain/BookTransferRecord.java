package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图书调拨记录对象 book_transfer_record
 */
public class BookTransferRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 图书ID */
    private Long bookId;

    /** 图书名称 */
    @Excel(name = "图书名称")
    private String bookName;

    /** 原部门ID */
    private Long sourceDeptId;

    /** 原部门名称 */
    @Excel(name = "原分馆")
    private String sourceDeptName;

    /** 目标部门ID */
    private Long targetDeptId;

    /** 目标部门名称 */
    @Excel(name = "目标分馆")
    private String targetDeptName;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operator;

    /** 调拨时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "调拨时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public Long getSourceDeptId() { return sourceDeptId; }
    public void setSourceDeptId(Long sourceDeptId) { this.sourceDeptId = sourceDeptId; }

    public String getSourceDeptName() { return sourceDeptName; }
    public void setSourceDeptName(String sourceDeptName) { this.sourceDeptName = sourceDeptName; }

    public Long getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }

    public String getTargetDeptName() { return targetDeptName; }
    public void setTargetDeptName(String targetDeptName) { this.targetDeptName = targetDeptName; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}