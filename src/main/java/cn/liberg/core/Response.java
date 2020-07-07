package cn.liberg.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    private int code = StatusCode.OK.code();
    private String message = "";
    private List datas;
    public Map metas;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List getDatas() {
        return datas;
    }

    public Map getMetas() {
        return metas;
    }

    public Response setDatas(List datas) {
        this.datas = datas;
        return this;
    }

    public Response putData(Object data) {
        if(datas == null) {
            datas = new ArrayList<>();
        }
        datas.add(data);
        return this;
    }

    public Response setMetas(Map metas) {
        this.metas = metas;
        return this;
    }

    public Response putMeta(String key, Object value) {
        if(metas == null) {
            metas = new HashMap<>();
        }
        metas.put(key,value);
        return this;
    }

    public static Response ok() {
        return new Response();
    }

    public static Response of(IStatusCode sc) {
        Response res = new Response();
        res.code = sc.code();
        res.message = sc.desc();
        return res;
    }

    public static Response of(int code) {
        Response res = new Response();
        res.code = code;
        return res;
    }

    public static Response of(String message) {
        Response res = new Response();
        res.message = message;
        return res;
    }

    public static Response of(int code, String message) {
        Response res = new Response();
        res.code = code;
        res.message = message;
        return res;
    }
}
