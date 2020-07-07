package cn.liberg.database.query;

public class JoinFields {
    private StringBuilder sb = new StringBuilder();

    private JoinFields() {}

    public static JoinFields of(JoinDao dao, String[] fields) {
        JoinFields jf = new JoinFields();
        jf.add(dao, fields);
        return jf;
    }

    public static JoinFields of(JoinDao dao) {
        JoinFields jf = new JoinFields();
        jf.addAll(dao);
        return jf;
    }


    public void add(JoinDao dao, String[] fields) {
        String a = dao.alias;
        for(String f : fields) {
            sb.append(a + "." + f);
            sb.append(" as " + a+f);
            sb.append(",");
        }
    }

    public void addAll(JoinDao dao) {
        String a = dao.alias;
        sb.append(a + ".*");
        sb.append(",");
    }

    public String build() {
        if(sb.charAt(sb.length()-1) == ',') {
            sb.setCharAt(sb.length()-1, ' ');
        }
        return sb.toString();
    }
}
