package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.SysUserPost;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysPostMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserPostMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysUserService;

import com.ruoyi.system.domain.BookReader;
import com.ruoyi.system.mapper.BookReaderMapper;

@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;

    @Autowired
    private BookReaderMapper bookReaderMapper;

    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        return userMapper.selectUserList(user);
    }

    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return userMapper.selectUserByUserName(userName);
    }

    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SecurityUtils.isAdmin())
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息（包含自动生成图书读者档案）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUser user)
    {
        user.setCreateBy(SecurityUtils.getUsername());
        user.setCreateTime(DateUtils.getNowDate());

        int rows = userMapper.insertUser(user);
        insertUserPost(user);
        insertUserRole(user);

        // ★ 这里也加上 try-catch 兜底，防止后台新增时由于同步失败导致业务崩溃
        try {
            syncToBookReader(user);
        } catch (Exception e) {
            log.error("新增用户时自动生成读者档案失败，用户ID：" + user.getUserId(), e);
        }

        return rows;
    }

    /**
     * 注册用户信息
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        user.setCreateBy(user.getUserName());
        user.setCreateTime(DateUtils.getNowDate());
        // 自助注册的账号统一默认为「系统读者」身份，并归属到配置的默认分馆
        if (StringUtils.isNull(user.getDeptId())) {
            user.setDeptId(resolveDefaultReaderDeptId());
        }
        int rows = userMapper.insertUser(user);
        if (rows > 0) {
            // 授予「系统读者」角色（book_reader），否则注册完登录进来没有任何菜单权限
            try {
                assignDefaultReaderRole(user);
            } catch (Exception e) {
                log.error("注册时自动分配读者角色失败，用户ID：" + user.getUserId(), e);
            }
            // 生成读者档案（借书卡、可借数量等）
            try {
                syncToBookReader(user);
            } catch (Exception e) {
                // 即使生成读者档案失败，也不能让注册接口报错（避免前端弹窗 401）
                log.error("注册时自动生成读者档案失败，用户ID：" + user.getUserId(), e);
            }
            return true;
        }
        return false;
    }

    /**
     * 给自助注册用户授予「系统读者」角色，注册页不再让用户自己选身份
     */
    private void assignDefaultReaderRole(SysUser user) {
        SysRole query = new SysRole();
        query.setRoleKey("book_reader");
        List<SysRole> roles = roleMapper.selectRoleList(query);
        if (CollectionUtils.isEmpty(roles)) {
            log.warn("未找到 role_key=book_reader 的角色，注册用户将没有默认身份");
            return;
        }
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getUserId());
        ur.setRoleId(roles.get(0).getRoleId());
        List<SysUserRole> list = new ArrayList<>(1);
        list.add(ur);
        userRoleMapper.batchUserRole(list);
    }

    /**
     * 读取配置的读者默认分馆，未配置时返回 null（由管理员后续分配）
     */
    private Long resolveDefaultReaderDeptId() {
        String deptIdStr = configService.selectConfigByKey("book.default.reader.dept.id");
        if (StringUtils.isEmpty(deptIdStr)) {
            return null;
        }
        try {
            return Long.parseLong(deptIdStr.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 book.default.reader.dept.id 不是合法数字：" + deptIdStr);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(user);
        userPostMapper.deleteUserPostByUserId(userId);
        insertUserPost(user);

        BookReader reader = bookReaderMapper.selectBookReaderByUserId(user.getUserId());
        if (reader != null) {
            boolean needUpdate = false;
            if (StringUtils.isNotEmpty(user.getNickName())) {
                reader.setRealName(user.getNickName());
                needUpdate = true;
            }
            if (StringUtils.isNotEmpty(user.getPhonenumber())) {
                reader.setPhone(user.getPhonenumber());
                needUpdate = true;
            }
            if (needUpdate) {
                bookReaderMapper.updateBookReader(reader);
            }
        }

        return userMapper.updateUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateUserStatus(user.getUserId(), user.getStatus());
    }

    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    @Override
    public boolean updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    public void updateLoginInfo(Long userId, String loginIp, Date loginDate)
    {
        userMapper.updateLoginInfo(userId, loginIp, loginDate);
    }

    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.resetUserPwd(user.getUserId(), user.getPassword());
    }

    @Override
    public int resetUserPwd(Long userId, String password)
    {
        return userMapper.resetUserPwd(userId, password);
    }

    public void insertUserRole(SysUser user)
    {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        userRoleMapper.deleteUserRole(userIds);
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        // ... 导入代码保持不变 ...
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList)
        {
            try
            {
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    deptService.checkDeptDataScope(user.getDeptId());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    userMapper.insertUser(user);

                    syncToBookReader(user);

                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(u.getUserId());
                    user.setDeptId(u.getDeptId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getUserName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * ★ 核心逻辑：同步系统用户到图书读者表
     */
    private void syncToBookReader(SysUser user) {
        BookReader existReader = bookReaderMapper.selectBookReaderByUserId(user.getUserId());
        if (existReader != null) {
            return;
        }

        boolean needSync = false;

        if (user.getRoleIds() != null && user.getRoleIds().length > 0) {
            for (Long roleId : user.getRoleIds()) {
                SysRole role = roleMapper.selectRoleById(roleId);
                if (role != null && "book_reader".equals(role.getRoleKey())) {
                    needSync = true;
                    break;
                }
            }
        } else {
            needSync = true;
        }

        if (needSync) {
            BookReader reader = new BookReader();
            reader.setUserId(user.getUserId());

            // ★ 修复这里：如果昵称是空，就用用户名代替，防止数据库非空校验报错
            String realName = StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
            reader.setRealName(realName);

            if (StringUtils.isNotEmpty(user.getPhonenumber())) {
                reader.setPhone(user.getPhonenumber());
            }

            reader.setCardNo("RD" + user.getUserId() + String.valueOf(System.currentTimeMillis()).substring(9));
            reader.setCurrentBorrowNum(0L);
            // 最大可借数取系统配置，未配置时默认 5 本
            String maxStr = configService.selectConfigByKey("book.default.max.borrow.num");
            long maxNum = 5L;
            if (StringUtils.isNotEmpty(maxStr)) {
                try {
                    maxNum = Long.parseLong(maxStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            reader.setMaxBorrowNum(maxNum);
            reader.setStatus("0");
            reader.setDeptId(user.getDeptId());
            reader.setCreateTime(DateUtils.getNowDate());

            // 防止注册时无权限获取用户名报错，这里加个判断
            String createBy = "system_register";
            try {
                createBy = SecurityUtils.getUsername();
            } catch (Exception ignored) {}
            reader.setCreateBy(createBy);

            bookReaderMapper.insertBookReader(reader);
        }
    }
}