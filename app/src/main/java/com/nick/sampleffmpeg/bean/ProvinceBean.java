package com.nick.sampleffmpeg.bean;

/**
 * Created by Sai on 15/11/22.
 */
public class ProvinceBean {
    private long id;
    private String name;
    private String directoryId;
    private String others;

    public ProvinceBean(long id, String name, String directoryId, String others){
        this.id = id;
        this.name = name;
        this.directoryId = directoryId;
        this.others = others;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getPickerViewText() {
        return name;
    }
}
