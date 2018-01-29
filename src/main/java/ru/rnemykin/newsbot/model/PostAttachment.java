package ru.rnemykin.newsbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostAttachment extends Model<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	/*@ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "idPost")
    @JsonBackReference
    private Post post;*/

	@ManyToOne
	@JoinColumn(name = "postId", insertable = false, updatable = false)
	private Post post;

    /*@Enumerated(EnumType.STRING)
    private TypeAttachmentsEnum type;*/

    private Integer postId;
    private String photo75Url;
    private String photo130Url;
    private String photo604Url;
    private String photo807Url;
    private String photo1280Url;
    private String photo2560Url;

    public String getUrlPhoto() {
        if (photo2560Url != null) return photo2560Url;
        if (photo1280Url != null) return photo1280Url;
        if (photo807Url != null) return photo807Url;
        if (photo604Url != null) return photo604Url;
        if (photo130Url != null) return photo130Url;
        if (photo75Url != null) return photo75Url;
        return null;
    }
}
