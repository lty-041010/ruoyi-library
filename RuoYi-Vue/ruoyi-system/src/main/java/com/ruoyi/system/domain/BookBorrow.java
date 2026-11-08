package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 借阅记录对象 book_borrow
 *
 * @author LTY
 * @date 2026-11-04
 */
public class BookBorrow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 是否属于当前登录人的分馆（1是 0否） */
    private Integer isLocal;

    /** 借书卡号（非数据库字段） */
    private String cardNo;

    /** 所属部门ID */
    private Long deptId;

    /** 读者姓名（非数据库字段） */
    private String readerName;

    /** 图书名称（非数据库字段） */
    private String bookName;

    /** ISBN（非数据库字段） */
    private String isbn;

    /** 借阅记录ID */
    private Long id;

    /** 读者ID */
    private Long readerId;

    /** 图书ID */
    private Long bookId;

    /** 借出时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "借出时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date borrowTime;

    /** 应还时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "应还时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dueTime;

    /** 实际归还时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "实际归还时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date returnTime;

    /** 状态（0借阅中 1已归还 2已逾期） */
    @Excel(name = "状态", readConverterExp = "0=借阅中,1=已归还,2=已逾期")
    private String status;

    /** 已续借次数 */
    @Excel(name = "续借次数")
    private Integer renewCount;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 逾期天数（非数据库字段） */
    private Integer overdueDays;

    /** 滞纳金（非数据库字段，元） */
    private java.math.BigDecimal fineAmount;

    // 非数据库字段 Getter Setter
    public Integer getIsLocal() {
        return isLocal;
    }
    public void setIsLocal(Integer isLocal) {
        this.isLocal = isLocal;
    }

    public String getCardNo() {
        return cardNo;
    }
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Long getDeptId() {
        return deptId;
    }
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getReaderName() {
        return readerName;
    }
    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getBookName() {
        return bookName;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    // 数据库字段 Getter Setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getReaderId() {
        return readerId;
    }
    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }

    public Long getBookId() {
        return bookId;
    }
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Date getBorrowTime() {
        return borrowTime;
    }
    public void setBorrowTime(Date borrowTime) {
        this.borrowTime = borrowTime;
    }

    public Date getDueTime() {
        return dueTime;
    }
    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime;
    }

    public Date getReturnTime() {
        return returnTime;
    }
    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRenewCount() {
        return renewCount;
    }
    public void setRenewCount(Integer renewCount) {
        this.renewCount = renewCount;
    }

    public String getDelFlag() {
        return delFlag;
    }
    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }
    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public java.math.BigDecimal getFineAmount() {
        return fineAmount;
    }
    public void setFineAmount(java.math.BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("readerId", getReaderId())
                .append("bookId", getBookId())
                .append("borrowTime", getBorrowTime())
                .append("dueTime", getDueTime())
                .append("returnTime", getReturnTime())
                .append("status", getStatus())
                .append("renewCount", getRenewCount())
                .append("delFlag", getDelFlag())
                .append("readerName", getReaderName())
                .append("bookName", getBookName())
                .append("isbn", getIsbn())
                .append("cardNo", getCardNo())
                .append("deptId", getDeptId())
                .append("isLocal", getIsLocal())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
