package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface DashboardMapper {
    // 统计图书总数（种类）
    @Select("<script>select count(1) from book_info where del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    int countTotalBooks(@Param("deptId") Long deptId);

    // ================= ★ 新增：统计馆藏总册数 =================
    @Select("<script>select IFNULL(SUM(total_stock), 0) from book_info where del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    Long sumTotalStock(@Param("deptId") Long deptId);

    // ================= ★ 新增：统计可借总册数 =================
    @Select("<script>select IFNULL(SUM(available_stock), 0) from book_info where del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    Long sumAvailableStock(@Param("deptId") Long deptId);
    // =========================================================

    // 统计读者总数
    @Select("<script>select count(1) from book_reader where del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    int countTotalReaders(@Param("deptId") Long deptId);

    // 统计在借总数（借阅中 + 逾期）
    @Select("<script>select count(1) from book_borrow where status in ('0', '2') and del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    int countActiveBorrows(@Param("deptId") Long deptId);

    // 统计逾期总数
    @Select("<script>select count(1) from book_borrow where status = '2' and del_flag = '0' " +
            "<if test='deptId != null'> and dept_id = #{deptId} </if></script>")
    int countOverdueBorrows(@Param("deptId") Long deptId);

    // ================= 读者个人维度统计 =================
    @Select("select count(1) from book_borrow where reader_id = #{readerId} and status in ('0','2') and del_flag = '0'")
    int countReaderActiveBorrows(@Param("readerId") Long readerId);

    @Select("select count(1) from book_borrow where reader_id = #{readerId} and status = '2' and del_flag = '0'")
    int countReaderOverdueBorrows(@Param("readerId") Long readerId);

    // 图表数据保留全局
    @Select("select c.category_name as name, count(b.id) as value from book_category c " +
            "left join book_info b on c.id = b.category_id and b.del_flag = '0' " +
            "where c.del_flag = '0' group by c.id, c.category_name")
    List<Map<String, Object>> getCategoryStats();

    @Select("select d.dept_name as name, count(i.id) as value from sys_dept d " +
            "left join book_info i on d.dept_id = i.dept_id and i.del_flag = '0' " +
            "where d.del_flag = '0' group by d.dept_id, d.dept_name")
    List<Map<String, Object>> getDeptStats();
}