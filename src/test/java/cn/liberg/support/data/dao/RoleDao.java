package cn.liberg.support.data.dao;

import cn.liberg.support.data.dao.impl.RoleDaoImpl;

public class RoleDao extends RoleDaoImpl {
    private static volatile RoleDao _instance;

    public static RoleDao self() {
		if (_instance == null) {
		    synchronized (RoleDao.class) {
                if (_instance == null) {
                    _instance = new RoleDao();
                }
            }
		}
		return _instance;
    }

}