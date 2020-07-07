package cn.liberg.support.data.entity;

import cn.liberg.annotation.dbmap;

public class User {
    public long id;
    @dbmap(isIndex = true)
    public String name;
    public String password;
    public long roleId;
    public long createTime;
    @dbmap(isMap = false)
    public Role role;

}