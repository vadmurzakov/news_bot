package ru.rnemykin.newsbot.model.enums;

public enum PublicEnum {
    BEELIVE(36378934L, "https://vk.com/belgorod");

    private Long id;
    private String url;

    PublicEnum(Long id, String url) {
        this.id = id;
        this.url = url;
    }

    public String url() {
        return url;
    }
    public Long id() {
        return id;
    }

    public static PublicEnum fromId(Long id) {
        for (PublicEnum publicEnum : PublicEnum.values()) {
            if (publicEnum.id.equals(id)) {
                return publicEnum;
            }
        }
        throw new RuntimeException("Not found PublicEnum with id = " + id);
    }
}
