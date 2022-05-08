package cn.liberg.support.data.entity;

import cn.liberg.core.TenThousands;
import cn.liberg.annotation.cache;
import cn.liberg.annotation.dbmap;

public class User {
    @cache(cap = TenThousands.X1)
    public long id;
    @dbmap(isIndex = true, length = 63) @cache(cap = 4, group = "g1", groupCap = 6)
    public String name;
    public String password;
    public byte age;
    public long roleId;
    @cache(group = "g1", groupCap = 6)
    public long createTime;
    @dbmap(isMap=false)
    public Role role;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", createTime=" + createTime +
                '}';
    }
}