package com.example.projectwebmovie.dto;

public class ShowtimeDTO {
    private String time;
    private String label;
    private String room;
    private Integer cinemaRoomId;
    private Integer showDateId;
    private Integer scheduleId;

    public ShowtimeDTO(String time, String label, String room, Integer cinemaRoomId, Integer showDateId, Integer scheduleId) {
        this.time = time;
        this.label = label;
        this.room = room;
        this.cinemaRoomId = cinemaRoomId;
        this.showDateId = showDateId;
        this.scheduleId = scheduleId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getCinemaRoomId() {
        return cinemaRoomId;
    }

    public void setCinemaRoomId(Integer cinemaRoomId) {
        this.cinemaRoomId = cinemaRoomId;
    }

    public Integer getShowDateId() {
        return showDateId;
    }

    public void setShowDateId(Integer showDateId) {
        this.showDateId = showDateId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
}