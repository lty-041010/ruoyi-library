package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图书预约对象 book_reservation
 */
public class BookReservation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预约ID */
    private Long id;

    /** 读者ID */
    private Long readerId;

    /** 图书ID */
    private Long bookId;

    /** 所属分馆 */
    private Long deptId;

    /** 状态（0等待中 2已取消 3已完成） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    // ===== 非数据库字段，用于页面展示 =====
    private String bookName;
    private String isbn;
    private String author;
    private String coverImage;
    private Integer availableStock;

    /** 读者姓名 / 借书证 / 分馆名称（页面展示） */
    private String readerName;
    private String cardNo;
    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reserveTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Date getReserveTime() { return reserveTime; }
    public void setReserveTime(Date reserveTime) { this.reserveTime = reserveTime; }
}
