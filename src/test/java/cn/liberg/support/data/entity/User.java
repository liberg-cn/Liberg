package cn.liberg.support.data.entity;

import cn.liberg.annotation.dbmap;

public class User {
    public long id;
    @dbmap(isIndex = true, length = 63)
    public String name;
    public String password;
    public byte age;
    public long roleId;
    public long createTime;
    @dbmap(isMap = false)
    public Role role;
}