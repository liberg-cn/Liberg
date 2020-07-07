package cn.liberg.support.data.entity;

import cn.liberg.annotation.dbmap;

public class Role {
    public long id;
    @dbmap(length=31)
    public String name;
    @dbmap(length=5000)
    public String permissions;


}