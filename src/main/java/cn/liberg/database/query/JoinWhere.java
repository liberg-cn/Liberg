
package cn.liberg.database.query;

import java.util.ArrayList;
import java.util.List;

public class JoinWhere {
    protected List<IWhereMeta> whereMetas = new ArrayList<>();

    public String build() {
        StringBuilder sb = new StringBuilder();
        for(IWhereMeta m : whereMetas) {
            sb.append(m.build());
            sb.append(" ");
        }
        return sb.toString();
    }

    public JoinWhere add(IWhereMeta meta) {
        whereMetas.add(meta);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getName()+"{"+build()+"}";
    }
}
