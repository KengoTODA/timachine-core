package com.worksap.timachine.migrations;

import com.worksap.timachine.model.Down;
import com.worksap.timachine.model.Migration;
import com.worksap.timachine.model.Up;

/**
 * Created by liuyang on 14-10-8.
 */
@Migration
public class M20141106171533WithSomeName {
    @Up
    public void up() {
        System.out.println("up 4");
    }

    @Down
    public void down() {
        System.out.println("down 4");

    }
}