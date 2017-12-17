package ru.rnemykin.newsbot.model;

import lombok.Data;

@Data
public class PostAttachment extends Model<Long> {
    private Long id;
    private String type;
    private Long attachmentId;
    private String photo75Url;
    private String photo130Url;
    private String photo604Url;
    private String photo807Url;
    private String photo1280Url;
}
