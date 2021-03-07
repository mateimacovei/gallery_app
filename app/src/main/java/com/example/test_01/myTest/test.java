package com.example.test_01.myTest;

import com.example.test_01.cub;

public class test {
    public Integer p1;

    public test(Integer p1) {
        System.out.println("sunt in constructor java, voi construi un obiect kotlin");
        cub cub = new cub();
        cub.print_all(111);

        this.p1 = p1;
    }
}
