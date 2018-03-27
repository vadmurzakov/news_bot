package ru.newsbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
	private Long postId;

	@Column(name = "photo_75_url")
    private String photo75Url;

    @Column(name = "photo_130_url")
    private String photo130Url;

    @Column(name = "photo_604_url")
    private String photo604Url;

    @Column(name = "photo_807_url")
    private String photo807Url;

    @Column(name = "photo_1280_url")
    private String photo1280Url;

    @Column(name = "photo_2560_url")
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
