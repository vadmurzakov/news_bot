package ru.rnemykin.newsbot.model;

import lombok.Data;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table
@Entity
public class Post extends Model<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CityEnum city;

    @Enumerated(EnumType.STRING)
    private PostStatusEnum status;
    private LocalDateTime createDate;
    private LocalDateTime sentDate;
    private LocalDateTime cancelDate;
    private LocalDateTime publishDate;
    private Integer sentAttemptsCount;

    private Long postId;
    private Long ownerId;
    private String type;

    @Lob
    @Column(name="column_name", length = 10000)
    private byte[] text;
    private long likesCount;
    private long viewsCount;
    private long repostsCount;
    private long commentsCount;
    private Boolean isPinned;
    private LocalDateTime postDate;

//    private List<PostAttachment> postAttachments;  //  todo
}

