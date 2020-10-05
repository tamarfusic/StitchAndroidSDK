package com.fusit.stitchutils.utils;

import android.util.Log;

public class Character {
    public int id;
    public String date_created;
    public String name;
    public String video_path;
    public String frames_path;
    public String  audio_path;
    public int fps;
    public String orientation;
    public int width;
    public int height;
    public double length;
    public String seq_num;
    public int user_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        Log.e("tamar", "in setVideo_path="+video_path);
        this.video_path = video_path;
    }

    public String getFrames_path() {
        return frames_path;
    }

    public void setFrames_path(String frames_path) {
        this.frames_path = frames_path;
    }

    public String getAudio_path() {
        return audio_path;
    }

    public void setAudio_path(String audio_path) {
        this.audio_path = audio_path;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getSeq_num() {
        return seq_num;
    }

    public void setSeq_num(String seq_num) {
        this.seq_num = seq_num;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void print() {
        Log.e("tamar", "id="+id+" name="+name+" video_path="+video_path);
    }
}
