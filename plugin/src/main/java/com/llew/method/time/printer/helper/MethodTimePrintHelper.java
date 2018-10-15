package com.llew.method.time.printer.helper;

import java.util.LinkedList;

/**
 * MethodTimePrint辅助类，该类会在打包的时候被打包进apk中
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/15
 */
public class MethodTimePrintHelper {

    private static final ThreadLocal<LinkedList<Long>> LINKED_LIST = new ThreadLocal<>();

    public static void start() {
        LinkedList<Long> linkedList = LINKED_LIST.get();
        if (null == linkedList) {
            linkedList = new LinkedList<>();
            LINKED_LIST.set(linkedList);
        }
        linkedList.add(System.currentTimeMillis());
    }

    public static long stop() {
        LinkedList<Long> linkedList = LINKED_LIST.get();
        if (null != linkedList) {
            Long longValue = linkedList.pop();
            if (null != longValue) {
                return longValue.longValue();
            }
        }
        return 0;
    }
}