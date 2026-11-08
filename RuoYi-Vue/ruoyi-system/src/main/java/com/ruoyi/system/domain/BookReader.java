package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 读者扩展对象 book_reader
 *
 * @author LTY
 * @date 2026-11-04
 */
public class BookReader extends BaseEntity
{
    /** 所属部门ID */
    private Long deptId;

    /** 所属分馆名称（关联 sys_dept，仅展示用） */
    @Excel(name = "所属分馆")
    private String deptName;

    private static final long serialVersionUID = 1L;

    /** 读者ID */
    private Long id;

    /** 若依用户ID（逻辑关联 sys_user.user_id） */
    @Excel(name = "若依用户ID", readConverterExp = "逻辑关联,sys_user.user_id")
    private Long userId;

    /** 借书卡号 */
    @Excel(name = "借书卡号")
    private String cardNo;

    /** 读者姓名（冗余自 sys_user.nick_name） */
    @Excel(name = "读者姓名")
    private String realName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 最大可借数量 */
    @Excel(name = "最大可借数量")
    private Long maxBorrowNum;

    /** 当前已借数量 */
    @Excel(name = "当前已借数量")
    private Long currentBorrowNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public void setId(Long id)
    {
        this.id = id;
    }
    public Long getId()
    {
        return id;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public Long getUserId()
    {
        return userId;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }
    public String getCardNo()
    {
        return cardNo;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }
    public String getRealName()
    {
        return realName;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    public String getPhone()
    {
        return phone;
    }

    public void setMaxBorrowNum(Long maxBorrowNum)
    {
        this.maxBorrowNum = maxBorrowNum;
    }
    public Long getMaxBorrowNum()
    {
        return maxBorrowNum;
    }

    public void setCurrentBorrowNum(Long currentBorrowNum)
    {
        this.currentBorrowNum = currentBorrowNum;
    }
    public Long getCurrentBorrowNum()
    {
        return currentBorrowNum;
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
                .append("deptId", getDeptId())
                .append("id", getId())
                .append("userId", getUserId())
                .append("cardNo", getCardNo())
                .append("realName", getRealName())
                .append("phone", getPhone())
                .append("maxBorrowNum", getMaxBorrowNum())
                .append("currentBorrowNum", getCurrentBorrowNum())
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
