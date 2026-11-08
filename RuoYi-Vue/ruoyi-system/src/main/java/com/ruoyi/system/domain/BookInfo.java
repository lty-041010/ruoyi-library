package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图书信息对象 book_info
 * 
 * @author LTY
 * @date 2026-11-04
 */
public class BookInfo extends BaseEntity
{
    /** 所属分馆名称（非数据库字段，联表查询用） */
    private String deptName;

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    /** 所属部门ID */
    private Long deptId;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    private static final long serialVersionUID = 1L;

    /** 图书ID */
    private Long id;

    /** ISBN 编号 */
    @Excel(name = "ISBN 编号")
    private String isbn;

    /** 书名 */
    @Excel(name = "书名")
    private String bookName;

    /** 作者 */
    @Excel(name = "作者")
    private String author;

    /** 出版社 */
    @Excel(name = "出版社")
    private String publisher;

    /** 出版日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出版日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishDate;

    /** 价格（元） */
    @Excel(name = "价格", readConverterExp = "元=")
    private BigDecimal price;

    /** 总库存 */
    @Excel(name = "总库存")
    private Long totalStock;

    /** 可用库存（可借数量） */
    @Excel(name = "可用库存", readConverterExp = "可=借数量")
    private Long availableStock;

    /** 封面图片地址 */
    @Excel(name = "封面图片地址")
    private String coverImage;

    /** 图书简介 */
    @Excel(name = "图书简介")
    private String description;

    /** 所属分类ID（逻辑关联 book_category.id） */
    @Excel(name = "所属分类ID", readConverterExp = "逻=辑关联,b=ook_category.id")
    private Long categoryId;

    /** 状态（0上架 1下架） */
    @Excel(name = "状态", readConverterExp = "0=上架,1=下架")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setIsbn(String isbn) 
    {
        this.isbn = isbn;
    }

    public String getIsbn() 
    {
        return isbn;
    }

    public void setBookName(String bookName) 
    {
        this.bookName = bookName;
    }

    public String getBookName() 
    {
        return bookName;
    }

    public void setAuthor(String author) 
    {
        this.author = author;
    }

    public String getAuthor() 
    {
        return author;
    }

    public void setPublisher(String publisher) 
    {
        this.publisher = publisher;
    }

    public String getPublisher() 
    {
        return publisher;
    }

    public void setPublishDate(Date publishDate) 
    {
        this.publishDate = publishDate;
    }

    public Date getPublishDate() 
    {
        return publishDate;
    }

    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }

    public void setTotalStock(Long totalStock) 
    {
        this.totalStock = totalStock;
    }

    public Long getTotalStock() 
    {
        return totalStock;
    }

    public void setAvailableStock(Long availableStock) 
    {
        this.availableStock = availableStock;
    }

    public Long getAvailableStock() 
    {
        return availableStock;
    }

    public void setCoverImage(String coverImage) 
    {
        this.coverImage = coverImage;
    }

    public String getCoverImage() 
    {
        return coverImage;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("isbn", getIsbn())
            .append("bookName", getBookName())
            .append("author", getAuthor())
            .append("publisher", getPublisher())
            .append("publishDate", getPublishDate())
            .append("price", getPrice())
            .append("totalStock", getTotalStock())
            .append("availableStock", getAvailableStock())
            .append("coverImage", getCoverImage())
            .append("description", getDescription())
            .append("categoryId", getCategoryId())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
