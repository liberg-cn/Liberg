
package cn.liberg.database.join;

import cn.liberg.database.WhereMeta;

import java.util.ArrayList;
import java.util.List;

public class JoinWhere {
    protected List<WhereMeta> whereMetas = new ArrayList<>();

    public String build() {
        StringBuilder sb = new StringBuilder();
        for(WhereMeta m : whereMetas) {
            sb.append(m.value);
            sb.append(" ");
        }
        return sb.toString();
    }

    public JoinWhere add(WhereMeta meta) {
        whereMetas.add(meta);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getName()+"{"+build()+"}";
    }
}
