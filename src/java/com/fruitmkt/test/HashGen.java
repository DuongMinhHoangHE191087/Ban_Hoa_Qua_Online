package com.fruitmkt.test;
import com.fruitmkt.util.HashUtil;
public class HashGen {
    public static void main(String[] args) {
        String dbHash = "$2a$10$j81o9.j.iQO0VwW04yWjOeM5v2G2Bihg.U1Beb1rF7u6nS7gq7m.rre";
        System.out.println("IsValid? " + com.fruitmkt.util.HashUtil.verify("123456", dbHash));
    }
}
