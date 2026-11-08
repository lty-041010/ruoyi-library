package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 读者购书留言对象 book_wish
 */
public class BookWish extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long readerId;
    private Long deptId;
    private String title;
    private String author;
    private String isbn;
    private String reason;
    /** 0待审核 1已采纳 2已驳回 3已上架 */
    private String status;
    private String reply;
    private Long bookId;
    private String delFlag;

    /** 页面展示字段 */
    private String readerName;
    private String cardNo;
    private String deptName;
    private String linkedBookName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getLinkedBookName() { return linkedBookName; }
    public void setLinkedBookName(String linkedBookName) { this.linkedBookName = linkedBookName; }
}
