package com.example.source11_api30.lyc;

/**
 * create by lichenxing
 * 字幕容器类
 */
public class Lyric {

    private long lastTime;

    private long startTime;

    private long nextTime;

    private String desc;

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    @Override
    public String toString() {
        return "RadioCaptions{" +
                "lastTime=" + lastTime +
                ", startTime=" + startTime +
                ", nextTime=" + nextTime +
                ", desc='" + desc + '\'' +
                '}';
    }
}
