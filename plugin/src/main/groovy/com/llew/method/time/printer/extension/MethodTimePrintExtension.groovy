package com.llew.method.time.printer.extension;

/**
 * MethodTimePrint扩展类
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/15
 */

public class MethodTimePrintExtension {

    public boolean enable = true

    public boolean logEnable = true

    public String tag = "MethodTimePrinter"

    public int level = 4

    public List<String> excludes

    boolean getEnable() {
        return enable
    }

    void setEnable(boolean enable) {
        this.enable = enable
    }

    String getTag() {
        return tag
    }

    void setTag(String tag) {
        this.tag = tag
    }

    boolean getLogEnable() {
        return logEnable
    }

    void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable
    }

    int getLevel() {
        return level
    }

    void setLevel(int level) {
        this.level = level
    }

    List<String> getExcludes() {
        return excludes
    }

    void setExcludes(List<String> excludes) {
        this.excludes = excludes
    }
}
