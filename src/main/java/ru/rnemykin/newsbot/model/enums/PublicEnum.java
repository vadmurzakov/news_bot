package ru.rnemykin.newsbot.model.enums;

public enum PublicEnum {
    BEELIVE("https://vk.com/belgorod");

    private String url;

    PublicEnum(String url) {
        this.url = url;
    }


    public String url() {
        return url;
    }
}
