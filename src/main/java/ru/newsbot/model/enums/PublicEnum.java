package ru.newsbot.model.enums;

public enum PublicEnum {
    BEELIVE(36378934, "https://vk.com/belgorod"),
    BEL_INTER(35961352, "https://vk.com/belinter"),
    BELGOROD1(2044704, "https://vk.com/belgorod1");

    private Integer id;
    private String url;

    PublicEnum(Integer id, String url) {
        this.id = id;
        this.url = url;
    }

    public String url() {
        return url;
    }
    public Integer id() {
        return id;
    }

    public static PublicEnum from(Integer id) {
        for (PublicEnum publicEnum : PublicEnum.values()) {
            if (publicEnum.id.equals(id)) {
                return publicEnum;
            }
        }
        throw new RuntimeException("Not found PublicEnum with id = " + id);
    }
}
