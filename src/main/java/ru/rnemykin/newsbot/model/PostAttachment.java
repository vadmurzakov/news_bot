package ru.rnemykin.newsbot.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rnemykin.newsbot.model.enums.TypeAttachmentsEnum;

import javax.persistence.*;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PostAttachment extends Model<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "idPost")
    @JsonBackReference
    private Post post;

    @Enumerated(EnumType.STRING)
    private TypeAttachmentsEnum type;

    private Long attachmentId;
    private String photo75Url;
    private String photo130Url;
    private String photo604Url;
    private String photo807Url;
    private String photo1280Url;
}
