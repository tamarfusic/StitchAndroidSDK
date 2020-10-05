package com.fusit.stitchutils.utils;

public class StitchedVideo {

    public String status_code;
    public String status;
    public String name;
    public String orientation;
    public String path;
    public String merged_video_seq_num;


    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMerged_video_seq_num() {
        return merged_video_seq_num;
    }

    public void setMerged_video_seq_num(String merged_video_seq_num) {
        this.merged_video_seq_num = merged_video_seq_num;
    }



    public class time {
//        public double download_frames adjust_user_video;
        public double stitch_time;
        public double merged_frames_to_video_time;
//        public double download audio;
        public double storage_and_database;
        public double total_time;
    }
}
